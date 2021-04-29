package com.sanctionco.thunder.resources;

import com.sanctionco.thunder.authentication.basic.Key;
import com.sanctionco.thunder.crypto.HashAlgorithm;
import com.sanctionco.thunder.crypto.HashService;
import com.sanctionco.thunder.dao.DatabaseError;
import com.sanctionco.thunder.dao.DatabaseException;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.User;
import com.sanctionco.thunder.validation.PropertyValidator;
import com.sanctionco.thunder.validation.RequestValidator;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.mockito.Mockito.when;

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

  @Test
  void testPostNullUser() {
    Response response = resource.postUser(key, null);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testPostUserNullEmail() {
    User user = new User(null, "password", Collections.emptyMap());
    Response response = resource.postUser(key, user);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testPostUserInvalidEmail() {
    User user = new User(BAD_EMAIL, "password", Collections.emptyMap());
    Response response = resource.postUser(key, user);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testPostUserInvalidProperties() {
    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(false);

    Response response = resource.postUser(key, USER);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testPostUserDatabaseDown() {
    when(usersDao.insert(any(User.class)))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException(DatabaseError.DATABASE_DOWN)));

    Response response = resource.postUser(key, USER);

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  void testPostUserUnsupportedData() {
    when(usersDao.insert(any(User.class)))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException(DatabaseError.REQUEST_REJECTED)));

    Response response = resource.postUser(key, USER);

    assertEquals(Response.Status.INTERNAL_SERVER_ERROR, response.getStatusInfo());
  }

  @Test
  void testPostUserConflict() {
    when(usersDao.insert(any(User.class)))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException(DatabaseError.CONFLICT)));

    Response response = resource.postUser(key, USER);

    assertEquals(Response.Status.CONFLICT, response.getStatusInfo());
  }

  @Test
  void testPostUser() {
    when(usersDao.insert(any(User.class)))
        .thenReturn(CompletableFuture.completedFuture(UPDATED_USER));

    Response response = resource.postUser(key, USER);
    User result = (User) response.getEntity();

    assertAll("Assert successful user creation",
        () -> assertEquals(Response.Status.CREATED, response.getStatusInfo()),
        () -> assertEquals(UPDATED_USER, result));
  }

  @Test
  void testPostUserServerSideHash() {
    HashService hashService = mock(HashService.class);
    when(hashService.hash(anyString())).thenReturn("hashedpassword");

    var captor = ArgumentCaptor.forClass(User.class);

    var expectedUser = new User(
        new Email("test@test.com", false, null),
        "hashedpassword",
        Collections.emptyMap());

    UserResource resource = new UserResource(usersDao, validator, hashService);

    when(usersDao.insert(captor.capture()))
        .thenReturn(CompletableFuture.completedFuture(expectedUser));

    Response response = resource.postUser(key, USER);
    User result = (User) response.getEntity();

    assertAll("Assert successful user creation and password hash",
        () -> assertEquals(Response.Status.CREATED, response.getStatusInfo()),
        () -> assertNotEquals("password", result.getPassword()),
        () -> assertEquals(expectedUser, result),
        () -> assertEquals("hashedpassword", captor.getValue().getPassword()));
  }

  @Test
  void testUpdateNullUser() {
    Response response = resource.updateUser(key, "password", null, null);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testUpdateUserNullEmail() {
    User user = new User(null, "password", Collections.emptyMap());
    Response response = resource.updateUser(key, "password", EMAIL.getAddress(), user);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testUpdateUserInvalidEmail() {
    User user = new User(BAD_EMAIL, "password", Collections.emptyMap());
    Response response = resource.updateUser(key, "password", EMAIL.getAddress(), user);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testUpdateUserWithNullPassword() {
    Response response = resource.updateUser(key, null, null, USER);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testUpdateUserInvalidProperties() {
    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(false);

    Response response = resource.updateUser(key, "password", null, USER);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testUpdateUserLookupNotFound() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException(DatabaseError.USER_NOT_FOUND)));

    Response response = resource.updateUser(key, "password", null, USER);

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  void testUpdateUserLookupDatabaseDown() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException(DatabaseError.DATABASE_DOWN)));

    Response response = resource.updateUser(key, "password", null, USER);

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  void testUpdateUserLookupUnsupportedData() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException(DatabaseError.REQUEST_REJECTED)));

    Response response = resource.updateUser(key, "password", null, USER);

    assertEquals(Response.Status.INTERNAL_SERVER_ERROR, response.getStatusInfo());
  }

  @Test
  void testUpdateUserMismatch() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(USER));

    Response response = resource.updateUser(key, "incorrectPassword", null, UPDATED_USER);

    assertEquals(Response.Status.UNAUTHORIZED, response.getStatusInfo());
  }

  @Test
  void testUpdateUserNotFound() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(USER));
    when(usersDao.update(null, UPDATED_USER))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException(DatabaseError.USER_NOT_FOUND)));

    Response response = resource.updateUser(key, "password", null, UPDATED_USER);

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  void testUpdateUserConflict() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(USER));
    when(usersDao.update(null, UPDATED_USER))
        .thenReturn(CompletableFuture.failedFuture(new DatabaseException(DatabaseError.CONFLICT)));

    Response response = resource.updateUser(key, "password", null, UPDATED_USER);

    assertEquals(Response.Status.CONFLICT, response.getStatusInfo());
  }

  @Test
  void testUpdateUserDatabaseDown() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(USER));
    when(usersDao.update(null, UPDATED_USER))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException(DatabaseError.DATABASE_DOWN)));

    Response response = resource.updateUser(key, "password", null, UPDATED_USER);

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  void testUpdateUserDisabledHeaderCheck() {
    var validator = new RequestValidator(propertyValidator, HASH_SERVICE, false);
    UserResource resource = new UserResource(usersDao, validator, HASH_SERVICE);

    // Set up the user that should already exist in the database
    Email existingEmail = new Email("existing@test.com", true, "token");
    User existingUser = new User(existingEmail, "password", Collections.emptyMap());

    // Define the updated user with changed verification info
    User updatedUser = new User(
        new Email(existingEmail.getAddress(), false, "changedToken"),
        "password",
        Collections.singletonMap("Key", "Value"));

    // Expect that the existing verification information stays the same even though
    // the updated user had different information
    User expectedResponse = new User(
        new Email(updatedUser.getEmail().getAddress(), true, "token"),
        updatedUser.getPassword(), updatedUser.getProperties());

    var captor = ArgumentCaptor.forClass(User.class);

    when(usersDao.findByEmail(existingEmail.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(existingUser));
    when(usersDao.update(eq(null), captor.capture()))
        .thenReturn(CompletableFuture.completedFuture(expectedResponse));

    // Update with a missing password header
    Response response = resource.updateUser(key, null, null, updatedUser);
    User result = (User) response.getEntity();

    assertAll("Assert successful user update",
        () -> assertEquals(Response.Status.OK, response.getStatusInfo()),
        () -> assertEquals(expectedResponse, captor.getValue()),
        () -> assertEquals(expectedResponse, result));
  }

  @Test
  void testUpdateUser() {
    // Set up the user that should already exist in the database
    Email existingEmail = new Email("existing@test.com", true, "token");
    User existingUser = new User(existingEmail, "password", Collections.emptyMap());

    // Define the updated user with changed verification info
    User updatedUser = new User(
        new Email(existingEmail.getAddress(), false, "changedToken"),
        "newPassword",
        Collections.emptyMap());

    // Expect that the existing verification information stays the same even though
    // the updated user had different information
    User expectedResponse = new User(
        new Email(updatedUser.getEmail().getAddress(), true, "token"),
        updatedUser.getPassword(), updatedUser.getProperties());

    var captor = ArgumentCaptor.forClass(User.class);

    when(usersDao.findByEmail(existingEmail.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(existingUser));
    when(usersDao.update(eq(null), captor.capture()))
        .thenReturn(CompletableFuture.completedFuture(expectedResponse));

    Response response = resource.updateUser(key, "password", null, updatedUser);
    User result = (User) response.getEntity();

    assertAll("Assert successful user update",
        () -> assertEquals(Response.Status.OK, response.getStatusInfo()),
        () -> assertEquals(expectedResponse, captor.getValue()),
        () -> assertEquals(expectedResponse, result));
  }

  @Test
  void testUpdateUserWithNewEmail() {
    // Set up the user that should already exist in the database
    Email existingEmail = new Email("existing@test.com", true, "token");
    User existingUser = new User(existingEmail, "password", Collections.emptyMap());

    // Define the updated user with a new email address
    User updatedUser = new User(
        new Email("newemail@test.com", true, "token"),
        "newPassword",
        Collections.emptyMap());

    // Define the expected user object
    User expectedResponse = new User(
        new Email(updatedUser.getEmail().getAddress(), false, null),
        updatedUser.getPassword(), updatedUser.getProperties());

    var captor = ArgumentCaptor.forClass(User.class);

    when(usersDao.findByEmail(existingEmail.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(existingUser));
    when(usersDao.update(eq(existingEmail.getAddress()), captor.capture()))
        .thenReturn(CompletableFuture.completedFuture(expectedResponse));

    Response response = resource.updateUser(key, "password", "existing@test.com", updatedUser);
    User result = (User) response.getEntity();

    assertAll("Assert successful user update with new email",
        () -> assertEquals(Response.Status.OK, response.getStatusInfo()),
        () -> assertEquals(expectedResponse, captor.getValue()),
        () -> assertEquals(expectedResponse, result));
  }

  @Test
  void testUpdateUserServerSideHash() {
    HashService hashService = spy(HashAlgorithm.SHA256.newHashService(true, false));
    var validator = new RequestValidator(propertyValidator, hashService, true);
    when(hashService.hash(anyString())).thenReturn("hashbrowns");
    UserResource resource = new UserResource(usersDao, validator, hashService);

    // Set up the user that should already exist in the database
    Email existingEmail = new Email("existing@test.com", true, "token");
    User existingUser = new User(existingEmail,
        "saltysaltysalt226cb4d24e21a9955515d52d6dc86449202f55f5b1463a800d2803cdda90298530",
        Collections.emptyMap());

    // Define the updated user with changed password
    User updatedUser = new User(
        new Email(existingEmail.getAddress(), true, "token"),
        "newPassword",
        Collections.emptyMap());

    // Expect that the new password is hashed
    User expectedResponse = new User(
        new Email(updatedUser.getEmail().getAddress(), true, "token"),
        "hashbrowns", updatedUser.getProperties());

    var captor = ArgumentCaptor.forClass(User.class);

    when(usersDao.findByEmail(existingEmail.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(existingUser));
    when(usersDao.update(eq(null), captor.capture()))
        .thenReturn(CompletableFuture.completedFuture(expectedResponse));

    Response response = resource.updateUser(key, "password", null, updatedUser);
    User result = (User) response.getEntity();

    assertAll("Assert successful user update",
        () -> assertEquals(Response.Status.OK, response.getStatusInfo()),
        () -> assertNotEquals("newPassword", result.getPassword()),
        () -> assertEquals(expectedResponse, captor.getValue()),
        () -> assertEquals("hashbrowns", captor.getValue().getPassword()),
        () -> assertEquals(expectedResponse, result));
  }

  @Test
  void testUpdateUserServerSideHashNoPasswordChange() {
    HashService hashService = HashAlgorithm.SHA256.newHashService(true, false);
    var validator = new RequestValidator(propertyValidator, hashService, true);
    UserResource resource = new UserResource(usersDao, validator, hashService);

    // Set up the user that should already exist in the database
    Email existingEmail = new Email("existing@test.com", true, "token");
    User existingUser = new User(existingEmail,
        "saltysaltysalt226cb4d24e21a9955515d52d6dc86449202f55f5b1463a800d2803cdda90298530",
        Collections.emptyMap());

    // Define the updated user with the same password
    User updatedUser = new User(
        new Email(existingEmail.getAddress(), true, "token"),
        "password",
        Collections.singletonMap("ID", 80));

    // Expect that the password stays the same
    User expectedResponse = new User(
        new Email(updatedUser.getEmail().getAddress(), true, "token"),
        "saltysaltysalt226cb4d24e21a9955515d52d6dc86449202f55f5b1463a800d2803cdda90298530",
        updatedUser.getProperties());

    var captor = ArgumentCaptor.forClass(User.class);

    when(usersDao.findByEmail(existingEmail.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(existingUser));
    when(usersDao.update(eq(null), captor.capture()))
        .thenReturn(CompletableFuture.completedFuture(expectedResponse));

    Response response = resource.updateUser(key, "password", null, updatedUser);
    User result = (User) response.getEntity();

    assertAll("Assert successful user update",
        () -> assertEquals(Response.Status.OK, response.getStatusInfo()),
        () -> assertNotEquals("password", result.getPassword()),
        () -> assertEquals(expectedResponse, captor.getValue()),
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
            new DatabaseException(DatabaseError.USER_NOT_FOUND)));

    Response response = resource.getUser(key, "password", EMAIL.getAddress());

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  void testGetUserDatabaseDown() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException(DatabaseError.DATABASE_DOWN)));

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
            new DatabaseException(DatabaseError.USER_NOT_FOUND)));

    Response response = resource.deleteUser(key, "password", EMAIL.getAddress());

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  void testDeleteUserLookupDatabaseDown() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException(DatabaseError.DATABASE_DOWN)));

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
            new DatabaseException(DatabaseError.USER_NOT_FOUND)));

    Response response = resource.deleteUser(key, "password", EMAIL.getAddress());

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  void testDeleteUserDatabaseDown() {
    when(usersDao.findByEmail(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(USER));
    when(usersDao.delete(EMAIL.getAddress()))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException(DatabaseError.DATABASE_DOWN)));

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
