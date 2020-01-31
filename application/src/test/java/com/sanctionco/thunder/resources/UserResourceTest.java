package com.sanctionco.thunder.resources;

import com.sanctionco.thunder.authentication.Key;
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

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.AdditionalAnswers.returnsSecondArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserResourceTest {
  private final Email badEmail = new Email("badEmail", false, "");
  private final Email email = new Email("test@test.com", false, "");
  private final User user = new User(email, "password", Collections.emptyMap());
  private final User updatedUser = new User(email, "newPassword", Collections.emptyMap());

  private final HashService hashService = HashAlgorithm.SIMPLE.newHashService(false);

  private final UsersDao usersDao = mock(UsersDao.class);
  private final Key key = mock(Key.class);
  private final PropertyValidator propertyValidator = mock(PropertyValidator.class);
  private final RequestValidator validator = new RequestValidator(propertyValidator, true);
  private final UserResource resource
      = new UserResource(usersDao, validator, hashService);

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
    User user = new User(badEmail, "password", Collections.emptyMap());
    Response response = resource.postUser(key, user);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testPostUserInvalidProperties() {
    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(false);

    Response response = resource.postUser(key, user);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testPostUserDatabaseDown() {
    when(usersDao.insert(any(User.class)))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.postUser(key, user);

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  void testPostUserUnsupportedData() {
    when(usersDao.insert(any(User.class)))
        .thenThrow(new DatabaseException(DatabaseError.REQUEST_REJECTED));

    Response response = resource.postUser(key, user);

    assertEquals(Response.Status.INTERNAL_SERVER_ERROR, response.getStatusInfo());
  }

  @Test
  void testPostUserConflict() {
    when(usersDao.insert(any(User.class))).thenThrow(new DatabaseException(DatabaseError.CONFLICT));

    Response response = resource.postUser(key, user);

    assertEquals(Response.Status.CONFLICT, response.getStatusInfo());
  }

  @Test
  void testPostUser() {
    when(usersDao.insert(any(User.class))).thenReturn(updatedUser);

    Response response = resource.postUser(key, user);
    User result = (User) response.getEntity();

    assertAll("Assert successful user creation",
        () -> assertEquals(Response.Status.CREATED, response.getStatusInfo()),
        () -> assertEquals(updatedUser, result));
  }

  @Test
  void testPostUserServerSideHash() {
    HashService hashService = HashAlgorithm.MD5.newHashService(true);
    UserResource resource = new UserResource(usersDao, validator, hashService);

    when(usersDao.insert(any(User.class))).then(returnsFirstArg());

    User expectedUser = new User(
        new Email("test@test.com", false, null),
        "5f4dcc3b5aa765d61d8327deb882cf99",
        Collections.emptyMap());

    Response response = resource.postUser(key, user);
    User result = (User) response.getEntity();

    assertAll("Assert successful user creation and password hash",
        () -> assertEquals(Response.Status.CREATED, response.getStatusInfo()),
        () -> assertEquals(expectedUser, result));
  }

  @Test
  void testUpdateNullUser() {
    Response response = resource.updateUser(key, "password", null, null);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testUpdateUserNullEmail() {
    User user = new User(null, "password", Collections.emptyMap());
    Response response = resource.updateUser(key, "password", email.getAddress(), user);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testUpdateUserInvalidEmail() {
    User user = new User(badEmail, "password", Collections.emptyMap());
    Response response = resource.updateUser(key, "password", email.getAddress(), user);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testUpdateUserWithNullPassword() {
    Response response = resource.updateUser(key, null, null, user);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testUpdateUserInvalidProperties() {
    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(false);

    Response response = resource.updateUser(key, "password", null, user);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testUpdateUserLookupNotFound() {
    when(usersDao.findByEmail(email.getAddress()))
        .thenThrow(new DatabaseException(DatabaseError.USER_NOT_FOUND));

    Response response = resource.updateUser(key, "password", null, user);

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  void testUpdateUserLookupDatabaseDown() {
    when(usersDao.findByEmail(email.getAddress()))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.updateUser(key, "password", null, user);

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  void testUpdateUserLookupUnsupportedData() {
    when(usersDao.findByEmail(email.getAddress()))
        .thenThrow(new DatabaseException(DatabaseError.REQUEST_REJECTED));

    Response response = resource.updateUser(key, "password", null, user);

    assertEquals(Response.Status.INTERNAL_SERVER_ERROR, response.getStatusInfo());
  }

  @Test
  void testUpdateUserMismatch() {
    when(usersDao.findByEmail(email.getAddress())).thenReturn(user);

    Response response = resource.updateUser(key, "incorrectPassword", null, updatedUser);

    assertEquals(Response.Status.UNAUTHORIZED, response.getStatusInfo());
  }

  @Test
  void testUpdateUserNotFound() {
    when(usersDao.findByEmail(email.getAddress())).thenReturn(user);
    when(usersDao.update(null, updatedUser))
        .thenThrow(new DatabaseException(DatabaseError.USER_NOT_FOUND));

    Response response = resource.updateUser(key, "password", null, updatedUser);

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  void testUpdateUserConflict() {
    when(usersDao.findByEmail(email.getAddress())).thenReturn(user);
    when(usersDao.update(null, updatedUser))
        .thenThrow(new DatabaseException(DatabaseError.CONFLICT));

    Response response = resource.updateUser(key, "password", null, updatedUser);

    assertEquals(Response.Status.CONFLICT, response.getStatusInfo());
  }

  @Test
  void testUpdateUserDatabaseDown() {
    when(usersDao.findByEmail(email.getAddress())).thenReturn(user);
    when(usersDao.update(null, updatedUser))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.updateUser(key, "password", null, updatedUser);

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  void testUpdateUserDisabledHeaderCheck() {
    RequestValidator validator = new RequestValidator(propertyValidator, false);
    UserResource resource = new UserResource(usersDao, validator, hashService);

    // Set up the user that should already exist in the database
    Email existingEmail = new Email("existing@test.com", true, "token");
    User existingUser = new User(existingEmail, "password", Collections.emptyMap());

    when(usersDao.findByEmail(existingEmail.getAddress())).thenReturn(existingUser);
    when(usersDao.update(eq(null), any(User.class))).then(returnsSecondArg());

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

    // Update with a missing password header
    Response response = resource.updateUser(key, null, null, updatedUser);
    User result = (User) response.getEntity();

    assertAll("Assert successful user update",
        () -> assertEquals(Response.Status.OK, response.getStatusInfo()),
        () -> assertEquals(expectedResponse, result));
  }

  @Test
  void testUpdateUser() {
    // Set up the user that should already exist in the database
    Email existingEmail = new Email("existing@test.com", true, "token");
    User existingUser = new User(existingEmail, "password", Collections.emptyMap());

    when(usersDao.findByEmail(existingEmail.getAddress())).thenReturn(existingUser);
    when(usersDao.update(eq(null), any(User.class))).then(returnsSecondArg());

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

    Response response = resource.updateUser(key, "password", null, updatedUser);
    User result = (User) response.getEntity();

    assertAll("Assert successful user update",
        () -> assertEquals(Response.Status.OK, response.getStatusInfo()),
        () -> assertEquals(expectedResponse, result));
  }

  @Test
  void testUpdateUserWithNewEmail() {
    // Set up the user that should already exist in the database
    Email existingEmail = new Email("existing@test.com", true, "token");
    User existingUser = new User(existingEmail, "password", Collections.emptyMap());

    when(usersDao.findByEmail(existingEmail.getAddress())).thenReturn(existingUser);
    when(usersDao.update(eq(existingEmail.getAddress()), any(User.class))).then(returnsSecondArg());

    // Define the updated user with a new email address
    User updatedUser = new User(
        new Email("newemail@test.com", true, "token"),
        "newPassword",
        Collections.emptyMap());

    // Define the expected user object
    User expectedResponse = new User(
        new Email(updatedUser.getEmail().getAddress(), false, null),
        updatedUser.getPassword(), updatedUser.getProperties());

    Response response = resource.updateUser(key, "password", "existing@test.com", updatedUser);
    User result = (User) response.getEntity();

    assertAll("Assert successful user update with new email",
        () -> assertEquals(Response.Status.OK, response.getStatusInfo()),
        () -> assertEquals(expectedResponse, result));
  }

  @Test
  void testUpdateUserServerSideHash() {
    HashService hashService = HashAlgorithm.MD5.newHashService(true);
    UserResource resource = new UserResource(usersDao, validator, hashService);

    // Set up the user that should already exist in the database
    Email existingEmail = new Email("existing@test.com", true, "token");
    User existingUser = new User(existingEmail, "5f4dcc3b5aa765d61d8327deb882cf99",
        Collections.emptyMap());

    when(usersDao.findByEmail(existingEmail.getAddress())).thenReturn(existingUser);
    when(usersDao.update(eq(null), any(User.class))).then(returnsSecondArg());

    // Define the updated user with changed password
    User updatedUser = new User(
        new Email(existingEmail.getAddress(), true, "token"),
        "newPassword",
        Collections.emptyMap());

    // Expect that the new password is hashed with MD5
    User expectedResponse = new User(
        new Email(updatedUser.getEmail().getAddress(), true, "token"),
        "14a88b9d2f52c55b5fbcf9c5d9c11875", updatedUser.getProperties());

    Response response = resource.updateUser(key, "password", null, updatedUser);
    User result = (User) response.getEntity();

    assertAll("Assert successful user update",
        () -> assertEquals(Response.Status.OK, response.getStatusInfo()),
        () -> assertEquals(expectedResponse, result));
  }

  @Test
  void testUpdateUserServerSideHashNoPasswordChange() {
    HashService hashService = HashAlgorithm.MD5.newHashService(true);
    UserResource resource = new UserResource(usersDao, validator, hashService);

    // Set up the user that should already exist in the database
    Email existingEmail = new Email("existing@test.com", true, "token");
    User existingUser = new User(existingEmail, "5f4dcc3b5aa765d61d8327deb882cf99",
        Collections.emptyMap());

    when(usersDao.findByEmail(existingEmail.getAddress())).thenReturn(existingUser);
    when(usersDao.update(eq(null), any(User.class))).then(returnsSecondArg());

    // Define the updated user with the same password
    User updatedUser = new User(
        new Email(existingEmail.getAddress(), true, "token"),
        "password",
        Collections.singletonMap("ID", 80));

    // Expect that the password stays the same
    User expectedResponse = new User(
        new Email(updatedUser.getEmail().getAddress(), true, "token"),
        "5f4dcc3b5aa765d61d8327deb882cf99", updatedUser.getProperties());

    Response response = resource.updateUser(key, "password", null, updatedUser);
    User result = (User) response.getEntity();

    assertAll("Assert successful user update",
        () -> assertEquals(Response.Status.OK, response.getStatusInfo()),
        () -> assertEquals(expectedResponse, result));
  }

  @Test
  void testGetUserWithNullEmail() {
    Response response = resource.getUser(key, "password", null);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testGetUserWithNullPassword() {
    Response response = resource.getUser(key, null, email.getAddress());

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testGetUserNotFound() {
    when(usersDao.findByEmail(email.getAddress()))
        .thenThrow(new DatabaseException(DatabaseError.USER_NOT_FOUND));

    Response response = resource.getUser(key, "password", email.getAddress());

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  void testGetUserDatabaseDown() {
    when(usersDao.findByEmail(email.getAddress()))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.getUser(key, "password", email.getAddress());

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  void testGetUserPasswordMismatch() {
    when(usersDao.findByEmail(email.getAddress())).thenReturn(user);

    Response response = resource.getUser(key, "incorrectPassword", email.getAddress());

    assertEquals(Response.Status.UNAUTHORIZED, response.getStatusInfo());
  }

  @Test
  void testGetUserDisabledHeaderCheck() {
    RequestValidator validator = new RequestValidator(propertyValidator, false);
    UserResource resource = new UserResource(usersDao, validator, hashService);

    when(usersDao.findByEmail(email.getAddress())).thenReturn(user);

    Response response = resource.getUser(key, null, email.getAddress());
    User result = (User) response.getEntity();

    assertAll("Assert successful get user",
        () -> assertEquals(Response.Status.OK, response.getStatusInfo()),
        () -> assertEquals(user, result));
  }

  @Test
  void testGetUser() {
    when(usersDao.findByEmail(email.getAddress())).thenReturn(user);

    Response response = resource.getUser(key, "password", email.getAddress());
    User result = (User) response.getEntity();

    assertAll("Assert successful get user",
        () -> assertEquals(Response.Status.OK, response.getStatusInfo()),
        () -> assertEquals(user, result));
  }

  @Test
  void testDeleteUserWithNullEmail() {
    Response response = resource.deleteUser(key, "password", null);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testDeleteUserWithNullPassword() {
    Response response = resource.deleteUser(key, null, email.getAddress());

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  void testDeleteUserLookupNotFound() {
    when(usersDao.findByEmail(email.getAddress()))
        .thenThrow(new DatabaseException(DatabaseError.USER_NOT_FOUND));

    Response response = resource.deleteUser(key, "password", email.getAddress());

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  void testDeleteUserLookupDatabaseDown() {
    when(usersDao.findByEmail(email.getAddress()))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.deleteUser(key, "password", email.getAddress());

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  void testDeleteUserPasswordMismatch() {
    when(usersDao.findByEmail(email.getAddress())).thenReturn(user);

    Response response = resource.deleteUser(key, "incorrectPassword", email.getAddress());

    assertEquals(Response.Status.UNAUTHORIZED, response.getStatusInfo());
  }

  @Test
  void testDeleteUserNotFound() {
    when(usersDao.findByEmail(email.getAddress())).thenReturn(user);
    when(usersDao.delete(email.getAddress()))
        .thenThrow(new DatabaseException(DatabaseError.USER_NOT_FOUND));

    Response response = resource.deleteUser(key, "password", email.getAddress());

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  void testDeleteUserDatabaseDown() {
    when(usersDao.findByEmail(email.getAddress())).thenReturn(user);
    when(usersDao.delete(email.getAddress()))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.deleteUser(key, "password", email.getAddress());

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  void testDeleteUserDisabledHeaderCheck() {
    RequestValidator validator = new RequestValidator(propertyValidator, false);
    UserResource resource = new UserResource(usersDao, validator, hashService);

    when(usersDao.findByEmail(email.getAddress())).thenReturn(user);
    when(usersDao.delete(email.getAddress())).thenReturn(user);

    Response response = resource.deleteUser(key, null, email.getAddress());
    User result = (User) response.getEntity();

    assertAll("Assert successful delete user",
        () -> assertEquals(Response.Status.OK, response.getStatusInfo()),
        () -> assertEquals(user, result));
  }

  @Test
  void testDeleteUser() {
    when(usersDao.findByEmail(email.getAddress())).thenReturn(user);
    when(usersDao.delete(email.getAddress())).thenReturn(user);

    Response response = resource.deleteUser(key, "password", email.getAddress());
    User result = (User) response.getEntity();

    assertAll("Assert successful delete user",
        () -> assertEquals(Response.Status.OK, response.getStatusInfo()),
        () -> assertEquals(user, result));
  }
}
