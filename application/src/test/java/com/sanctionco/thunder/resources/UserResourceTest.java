package com.sanctionco.thunder.resources;

import com.sanctionco.thunder.authentication.basic.Key;
import com.sanctionco.thunder.crypto.HashAlgorithm;
import com.sanctionco.thunder.crypto.HashService;
import com.sanctionco.thunder.dao.DatabaseException;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.User;
import com.sanctionco.thunder.validation.PropertyValidator;
import com.sanctionco.thunder.validation.RequestValidator;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("UserResource")
class UserResourceTest {
  private static final Email BAD_EMAIL = new Email("badEmail", false, "");
  private static final Email EMAIL = new Email("test@test.com", false, "");
  private static final User USER = new User(EMAIL, "password", Collections.emptyMap());
  private static final User UPDATED_USER = new User(EMAIL, "newPassword", Collections.emptyMap());

  private static final HashService HASH_SERVICE = HashAlgorithm.SIMPLE
      .newHashService(false, false);

  private final UsersDao usersDao = mock(UsersDao.class);
  private final Key key = mock(Key.class);
  private final PropertyValidator propertyValidator = mock(PropertyValidator.class);
  private final RequestValidator validator
      = new RequestValidator(propertyValidator, HASH_SERVICE, true);
  private final UserResource resource
      = new UserResource(usersDao, validator, HASH_SERVICE);

  @BeforeEach
  void setup() {
    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(true);
  }

  // Provide invalid user objects for parameterized tests
  @SuppressWarnings("unused")
  static Stream<User> invalidUserProvider() {
    return Stream.of(
        // Null user
        null,
        // Null email
        new User(null, "password", Collections.emptyMap()),
        // Invalid email
        new User(BAD_EMAIL, "password", Collections.emptyMap()));
  }

  @ParameterizedTest
  @MethodSource("invalidUserProvider")
  void post_invalidUserShouldFailValidation(User user) {
    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.postUser(asyncResponse, key, user);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.BAD_REQUEST, captor.getValue().getStatusInfo());
  }

  @Test
  void post_userWithInvalidPropertiesShouldFailValidation() {
    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(false);

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.postUser(asyncResponse, key, USER);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.BAD_REQUEST, captor.getValue().getStatusInfo());
  }

  @Test
  void post_databaseFailureShouldReturnServiceUnavailable() {
    when(usersDao.insert(any(User.class))).thenReturn(CompletableFuture.failedFuture(
        new DatabaseException("Error", DatabaseException.Error.DATABASE_DOWN)));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.postUser(asyncResponse, key, USER);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.SERVICE_UNAVAILABLE, captor.getValue().getStatusInfo());
  }

  @Test
  void post_databaseRejectionShouldReturnInternalServerError() {
    when(usersDao.insert(any(User.class))).thenReturn(CompletableFuture.failedFuture(
        new DatabaseException("Malformed", DatabaseException.Error.REQUEST_REJECTED)));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.postUser(asyncResponse, key, USER);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR, captor.getValue().getStatusInfo());
  }

  @Test
  void post_databaseExceptionWrappedShouldReturnCorrectResponse() {
    when(usersDao.insert(any(User.class))).thenReturn(CompletableFuture.failedFuture(
        new IllegalStateException(new DatabaseException(
            "Malformed", DatabaseException.Error.REQUEST_REJECTED))));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.postUser(asyncResponse, key, USER);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR, captor.getValue().getStatusInfo());
  }

  @Test
  void post_existingUserShouldReturnConflict() {
    when(usersDao.insert(any(User.class))).thenReturn(CompletableFuture.failedFuture(
        new DatabaseException("Existing user", DatabaseException.Error.CONFLICT)));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.postUser(asyncResponse, key, USER);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.CONFLICT, captor.getValue().getStatusInfo());
  }

  @Test
  void post_isSuccessful() {
    when(usersDao.insert(any(User.class)))
        .thenReturn(CompletableFuture.completedFuture(UPDATED_USER));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.postUser(asyncResponse, key, USER);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());

    User result = (User) captor.getValue().getEntity();

    assertAll("Response is correct for a successful creation",
        () -> assertEquals(Response.Status.CREATED, captor.getValue().getStatusInfo()),
        () -> assertEquals(UPDATED_USER, result));
  }

  @Test
  void post_userPasswordIsHashed() {
    // Setup the test object
    var hashService = mock(HashService.class);
    when(hashService.hash(anyString())).thenReturn("hashedpassword");

    var resource = new UserResource(usersDao, validator, hashService);

    // Setup captors and expected values
    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);
    var insertCaptor = ArgumentCaptor.forClass(User.class);

    var expectedUser = new User(
        Email.unverified("test@test.com"), "hashedpassword", Collections.emptyMap());

    when(usersDao.insert(insertCaptor.capture()))
        .thenReturn(CompletableFuture.completedFuture(expectedUser));

    resource.postUser(asyncResponse, key, USER);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());

    User result = (User) captor.getValue().getEntity();

    assertAll("Assert successful user creation and password hash",
        () -> assertEquals(Response.Status.CREATED, captor.getValue().getStatusInfo()),
        () -> assertEquals("hashedpassword", insertCaptor.getValue().getPassword()),
        () -> assertNotEquals("password", result.getPassword()),
        () -> assertEquals(expectedUser, result));
  }

  @ParameterizedTest
  @MethodSource("invalidUserProvider")
  void put_invalidUserShouldFailValidation(User user) {
    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.updateUser(asyncResponse, key, "password", null, user);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.BAD_REQUEST, captor.getValue().getStatusInfo());
  }

  @Test
  void put_nullPasswordShouldFailValidation() {
    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.updateUser(asyncResponse, key, null, null, USER);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.BAD_REQUEST, captor.getValue().getStatusInfo());
  }

  @Test
  void put_userWithInvalidPropertiesShouldFailValidation() {
    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(false);

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.updateUser(asyncResponse, key, "password", null, USER);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.BAD_REQUEST, captor.getValue().getStatusInfo());
  }

  @Test
  void put_nonexistentUserShouldReturnNotFound() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException("Not Found", DatabaseException.Error.USER_NOT_FOUND)));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.updateUser(asyncResponse, key, "password", null, USER);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.NOT_FOUND, captor.getValue().getStatusInfo());
  }

  @Test
  void put_databaseFailureOnLookupShouldReturnServiceUnavailable() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException("Down", DatabaseException.Error.DATABASE_DOWN)));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.updateUser(asyncResponse, key, "password", null, USER);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.SERVICE_UNAVAILABLE, captor.getValue().getStatusInfo());
  }

  @Test
  void put_databaseRejectionOnLookupShouldReturnInternalServerError() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException("Rejected", DatabaseException.Error.REQUEST_REJECTED)));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.updateUser(asyncResponse, key, "password", null, USER);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR, captor.getValue().getStatusInfo());
  }

  @Test
  void put_incorrectPasswordReturnsUnauthorized() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(USER));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.updateUser(asyncResponse, key, "incorrectPassword", null, UPDATED_USER);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.UNAUTHORIZED, captor.getValue().getStatusInfo());
  }

  @Test
  void put_userNotFoundDuringUpdateReturnsNotFound() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(USER));
    when(usersDao.update(null, UPDATED_USER))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException("Not Found", DatabaseException.Error.USER_NOT_FOUND)));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.updateUser(asyncResponse, key, "password", null, UPDATED_USER);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.NOT_FOUND, captor.getValue().getStatusInfo());
  }

  @Test
  void put_versionConflictReturnsConflict() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(USER));
    when(usersDao.update(null, UPDATED_USER))
        .thenReturn(CompletableFuture.failedFuture(new DatabaseException("Error",
            DatabaseException.Error.CONFLICT)));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.updateUser(asyncResponse, key, "password", null, UPDATED_USER);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.CONFLICT, captor.getValue().getStatusInfo());
  }

  @Test
  void put_databaseFailureOnUpdateShouldReturnServiceUnavailable() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(USER));
    when(usersDao.update(null, UPDATED_USER))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException("Error", DatabaseException.Error.DATABASE_DOWN)));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.updateUser(asyncResponse, key, "password", null, UPDATED_USER);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.SERVICE_UNAVAILABLE, captor.getValue().getStatusInfo());
  }

  @Test
  void put_whenPasswordHeaderCheckIsDisabledThenMissingPasswordSucceeds() {
    var validator = new RequestValidator(propertyValidator, HASH_SERVICE, false);
    var resource = new UserResource(usersDao, validator, HASH_SERVICE);

    // Set up the user that should already exist in the database
    var existingEmail = new Email("existing@test.com", true, "token");
    var existingUser = new User(existingEmail, "password", Collections.emptyMap());

    // Define the updated user with changed verification info
    var updatedUser = new User(
        new Email(existingEmail.getAddress(), false, "changedToken"),
        "password",
        Collections.singletonMap("Key", "Value"));

    // Expect that the existing verification information stays the same even though
    // the updated user had different information
    var expectedResponse = new User(
        new Email(updatedUser.getEmail().getAddress(), true, "token"),
        updatedUser.getPassword(), updatedUser.getProperties());

    var userCaptor = ArgumentCaptor.forClass(User.class);
    var asyncResponse = mock(AsyncResponse.class);

    when(usersDao.findByEmail(existingEmail.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(existingUser));
    when(usersDao.update(eq(null), userCaptor.capture()))
        .thenReturn(CompletableFuture.completedFuture(expectedResponse));

    // Update with a missing password header
    resource.updateUser(asyncResponse, key, null, null, updatedUser);

    var responseCaptor = ArgumentCaptor.forClass(Response.class);
    verify(asyncResponse, timeout(100).times(1)).resume(responseCaptor.capture());

    var result = (User) responseCaptor.getValue().getEntity();

    assertAll("Assert successful user update",
        () -> assertEquals(Response.Status.OK, responseCaptor.getValue().getStatusInfo()),
        () -> assertEquals(expectedResponse, userCaptor.getValue()),
        () -> assertEquals(expectedResponse, result));
  }

  @Test
  void put_shouldSucceed() {
    // Set up the user that should already exist in the database
    var existingEmail = new Email("existing@test.com", true, "token");
    var existingUser = new User(existingEmail, "password", Collections.emptyMap());

    // Define the updated user with changed verification info
    var updatedUser = new User(
        new Email(existingEmail.getAddress(), false, "changedToken"),
        "newPassword",
        Collections.emptyMap());

    // Expect that the existing verification information stays the same even though
    // the updated user had different information
    var expectedResponse = new User(
        new Email(updatedUser.getEmail().getAddress(), true, "token"),
        updatedUser.getPassword(), updatedUser.getProperties());

    var userCaptor = ArgumentCaptor.forClass(User.class);
    var asyncResponse = mock(AsyncResponse.class);

    when(usersDao.findByEmail(existingEmail.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(existingUser));
    when(usersDao.update(eq(null), userCaptor.capture()))
        .thenReturn(CompletableFuture.completedFuture(expectedResponse));

    resource.updateUser(asyncResponse, key, "password", null, updatedUser);

    var responseCaptor = ArgumentCaptor.forClass(Response.class);
    verify(asyncResponse, timeout(100).times(1)).resume(responseCaptor.capture());

    var result = (User) responseCaptor.getValue().getEntity();

    assertAll("Assert successful user update",
        () -> assertEquals(Response.Status.OK, responseCaptor.getValue().getStatusInfo()),
        () -> assertEquals(expectedResponse, userCaptor.getValue()),
        () -> assertEquals(expectedResponse, result));
  }

  @Test
  void put_userWithNewEmailSucceeds() {
    // Set up the user that should already exist in the database
    var existingEmail = new Email("existing@test.com", true, "token");
    var existingUser = new User(existingEmail, "password", Collections.emptyMap());

    // Define the updated user with a new email address
    var updatedUser = new User(
        new Email("newemail@test.com", true, "token"),
        "newPassword",
        Collections.emptyMap());

    // Define the expected user object
    var expectedResponse = new User(
        new Email(updatedUser.getEmail().getAddress(), false, null),
        updatedUser.getPassword(), updatedUser.getProperties());

    var userCaptor = ArgumentCaptor.forClass(User.class);
    var asyncResponse = mock(AsyncResponse.class);

    when(usersDao.findByEmail(existingEmail.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(existingUser));
    when(usersDao.update(eq(existingEmail.getAddress()), userCaptor.capture()))
        .thenReturn(CompletableFuture.completedFuture(expectedResponse));

    resource.updateUser(asyncResponse, key, "password", "existing@test.com", updatedUser);

    var responseCaptor = ArgumentCaptor.forClass(Response.class);
    verify(asyncResponse, timeout(100).times(1)).resume(responseCaptor.capture());

    var result = (User) responseCaptor.getValue().getEntity();

    assertAll("Assert successful user update with new email",
        () -> assertEquals(Response.Status.OK, responseCaptor.getValue().getStatusInfo()),
        () -> assertEquals(expectedResponse, userCaptor.getValue()),
        () -> assertEquals(expectedResponse, result));
  }

  @Test
  void put_NewPasswordShouldBeHashed() {
    var hashService = spy(HashAlgorithm.SHA256.newHashService(true, false));
    when(hashService.hash(anyString())).thenReturn("hashbrowns");

    var validator = new RequestValidator(propertyValidator, hashService, true);
    var resource = new UserResource(usersDao, validator, hashService);

    // Set up the user that should already exist in the database
    var existingEmail = new Email("existing@test.com", true, "token");
    var existingUser = new User(existingEmail,
        "saltysaltysalt226cb4d24e21a9955515d52d6dc86449202f55f5b1463a800d2803cdda90298530",
        Collections.emptyMap());

    // Define the updated user with changed password
    var updatedUser = new User(
        new Email(existingEmail.getAddress(), true, "token"),
        "newPassword",
        Collections.emptyMap());

    // Expect that the new password is hashed
    var expectedResponse = new User(
        new Email(updatedUser.getEmail().getAddress(), true, "token"),
        "hashbrowns", updatedUser.getProperties());

    var userCaptor = ArgumentCaptor.forClass(User.class);
    var asyncResponse = mock(AsyncResponse.class);

    when(usersDao.findByEmail(existingEmail.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(existingUser));
    when(usersDao.update(eq(null), userCaptor.capture()))
        .thenReturn(CompletableFuture.completedFuture(expectedResponse));

    resource.updateUser(asyncResponse, key, "password", null, updatedUser);

    var responseCaptor = ArgumentCaptor.forClass(Response.class);
    verify(asyncResponse, timeout(100).times(1)).resume(responseCaptor.capture());

    var result = (User) responseCaptor.getValue().getEntity();

    assertAll("Assert successful user update",
        () -> assertEquals(Response.Status.OK, responseCaptor.getValue().getStatusInfo()),
        () -> assertNotEquals("newPassword", result.getPassword()),
        () -> assertEquals(expectedResponse, userCaptor.getValue()),
        () -> assertEquals("hashbrowns", userCaptor.getValue().getPassword()),
        () -> assertEquals(expectedResponse, result));
  }

  @Test
  void testUpdateUserServerSideHashNoPasswordChange() {
    var hashService = HashAlgorithm.SHA256.newHashService(true, false);
    var validator = new RequestValidator(propertyValidator, hashService, true);
    var resource = new UserResource(usersDao, validator, hashService);

    // Set up the user that should already exist in the database
    var existingEmail = new Email("existing@test.com", true, "token");
    var existingUser = new User(existingEmail,
        "saltysaltysalt226cb4d24e21a9955515d52d6dc86449202f55f5b1463a800d2803cdda90298530",
        Collections.emptyMap());

    // Define the updated user with the same password
    var updatedUser = new User(
        new Email(existingEmail.getAddress(), true, "token"),
        "password", // hashes to the above
        Collections.singletonMap("ID", 80));

    // Expect that the password stays the same
    var expectedResponse = new User(
        new Email(updatedUser.getEmail().getAddress(), true, "token"),
        "saltysaltysalt226cb4d24e21a9955515d52d6dc86449202f55f5b1463a800d2803cdda90298530",
        updatedUser.getProperties());

    var userCaptor = ArgumentCaptor.forClass(User.class);
    var asyncResponse = mock(AsyncResponse.class);

    when(usersDao.findByEmail(existingEmail.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(existingUser));
    when(usersDao.update(eq(null), userCaptor.capture()))
        .thenReturn(CompletableFuture.completedFuture(expectedResponse));

    resource.updateUser(asyncResponse, key, "password", null, updatedUser);

    var responseCaptor = ArgumentCaptor.forClass(Response.class);
    verify(asyncResponse, timeout(100).times(1)).resume(responseCaptor.capture());

    var result = (User) responseCaptor.getValue().getEntity();

    assertAll("Assert successful user update",
        () -> assertEquals(Response.Status.OK, responseCaptor.getValue().getStatusInfo()),
        () -> assertNotEquals("password", result.getPassword()),
        () -> assertEquals(expectedResponse, userCaptor.getValue()),
        () -> assertEquals(expectedResponse, result));
  }

  @Test
  void testGetUserWithNullEmail() {
    Response response = resource.getUser(key, "password", null);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testGetUserWithNullPassword() {
    Response response = resource.getUser(key, null, EMAIL.getAddress());

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testGetUserNotFound() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException("Error", DatabaseException.Error.USER_NOT_FOUND)));

    Response response = resource.getUser(key, "password", EMAIL.getAddress());

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  void testGetUserDatabaseDown() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException("Error", DatabaseException.Error.DATABASE_DOWN)));

    Response response = resource.getUser(key, "password", EMAIL.getAddress());

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  void testGetUserPasswordMismatch() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(USER));

    Response response = resource.getUser(key, "incorrectPassword", EMAIL.getAddress());

    assertEquals(Response.Status.UNAUTHORIZED, response.getStatusInfo());
  }

  @Test
  void testGetUserDisabledHeaderCheck() {
    var validator = new RequestValidator(propertyValidator, HASH_SERVICE, false);
    UserResource resource = new UserResource(usersDao, validator, HASH_SERVICE);

    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(USER));

    Response response = resource.getUser(key, null, EMAIL.getAddress());
    User result = (User) response.getEntity();

    assertAll("Assert successful get user",
        () -> assertEquals(Response.Status.OK, response.getStatusInfo()),
        () -> assertEquals(USER, result));
  }

  @Test
  void testGetUser() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(USER));

    Response response = resource.getUser(key, "password", EMAIL.getAddress());
    User result = (User) response.getEntity();

    assertAll("Assert successful get user",
        () -> assertEquals(Response.Status.OK, response.getStatusInfo()),
        () -> assertEquals(USER, result));
  }

  @Test
  void testDeleteUserWithNullEmail() {
    Response response = resource.deleteUser(key, "password", null);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testDeleteUserWithNullPassword() {
    Response response = resource.deleteUser(key, null, EMAIL.getAddress());

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testDeleteUserLookupNotFound() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException("Error", DatabaseException.Error.USER_NOT_FOUND)));

    Response response = resource.deleteUser(key, "password", EMAIL.getAddress());

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  void testDeleteUserLookupDatabaseDown() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException("Error", DatabaseException.Error.DATABASE_DOWN)));

    Response response = resource.deleteUser(key, "password", EMAIL.getAddress());

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  void testDeleteUserPasswordMismatch() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(USER));

    Response response = resource.deleteUser(key, "incorrectPassword", EMAIL.getAddress());

    assertEquals(Response.Status.UNAUTHORIZED, response.getStatusInfo());
  }

  @Test
  void testDeleteUserNotFound() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(USER));
    when(usersDao.delete(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException("Error", DatabaseException.Error.USER_NOT_FOUND)));

    Response response = resource.deleteUser(key, "password", EMAIL.getAddress());

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  void testDeleteUserDatabaseDown() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(USER));
    when(usersDao.delete(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException("Error", DatabaseException.Error.DATABASE_DOWN)));

    Response response = resource.deleteUser(key, "password", EMAIL.getAddress());

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  void testDeleteUserDisabledHeaderCheck() {
    var validator = new RequestValidator(propertyValidator, HASH_SERVICE, false);
    UserResource resource = new UserResource(usersDao, validator, HASH_SERVICE);

    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(USER));
    when(usersDao.delete(EMAIL.getAddress())).thenReturn(CompletableFuture.completedFuture(USER));

    Response response = resource.deleteUser(key, null, EMAIL.getAddress());
    User result = (User) response.getEntity();

    assertAll("Assert successful delete user",
        () -> assertEquals(Response.Status.OK, response.getStatusInfo()),
        () -> assertEquals(USER, result));
  }

  @Test
  void testDeleteUser() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(USER));
    when(usersDao.delete(EMAIL.getAddress())).thenReturn(CompletableFuture.completedFuture(USER));

    Response response = resource.deleteUser(key, "password", EMAIL.getAddress());
    User result = (User) response.getEntity();

    assertAll("Assert successful delete user",
        () -> assertEquals(Response.Status.OK, response.getStatusInfo()),
        () -> assertEquals(USER, result));
  }
}
