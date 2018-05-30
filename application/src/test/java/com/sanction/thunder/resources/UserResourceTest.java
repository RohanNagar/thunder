package com.sanction.thunder.resources;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.codahale.metrics.MetricRegistry;
import com.sanction.thunder.authentication.Key;
import com.sanction.thunder.dao.DatabaseError;
import com.sanction.thunder.dao.DatabaseException;
import com.sanction.thunder.dao.UsersDao;
import com.sanction.thunder.models.Email;
import com.sanction.thunder.models.User;
import com.sanction.thunder.validation.PropertyValidator;
import com.sanction.thunder.validation.RequestValidator;

class UserResourceTest {
  private final Email badEmail = new Email("badEmail", false, "");
  private final Email email = new Email("test@test.com", false, "");
  private final User user = new User(email, "password", Collections.emptyMap());
  private final User updatedUser = new User(email, "newPassword", Collections.emptyMap());

  private final UsersDao usersDao = mock(UsersDao.class);
  private final MetricRegistry metrics = new MetricRegistry();
  private final Key key = mock(Key.class);
  private final PropertyValidator propertyValidator = mock(PropertyValidator.class);
  private final RequestValidator validator = new RequestValidator(propertyValidator);
  private final UserResource resource = new UserResource(usersDao, validator, metrics);

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
    when(usersDao.insert(any(User.class))).thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.postUser(key, user);

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  void testPostUserUnsupportedData() {
    when(usersDao.insert(any(User.class))).thenThrow(new DatabaseException(DatabaseError.REQUEST_REJECTED));

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

    assertAll("Assert successful user creation", () -> assertEquals(Response.Status.CREATED, response.getStatusInfo()),
        () -> assertEquals(updatedUser, result));
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
    when(usersDao.findByEmail(email.getAddress())).thenThrow(new DatabaseException(DatabaseError.USER_NOT_FOUND));

    Response response = resource.updateUser(key, "password", null, user);

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  void testUpdateUserLookupDatabaseDown() {
    when(usersDao.findByEmail(email.getAddress())).thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.updateUser(key, "password", null, user);

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  void testUpdateUserLookupUnsupportedData() {
    when(usersDao.findByEmail(email.getAddress())).thenThrow(new DatabaseException(DatabaseError.REQUEST_REJECTED));

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
    when(usersDao.update(null, updatedUser)).thenThrow(new DatabaseException(DatabaseError.USER_NOT_FOUND));

    Response response = resource.updateUser(key, "password", null, updatedUser);

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  void testUpdateUserConflict() {
    when(usersDao.findByEmail(email.getAddress())).thenReturn(user);
    when(usersDao.update(null, updatedUser)).thenThrow(new DatabaseException(DatabaseError.CONFLICT));

    Response response = resource.updateUser(key, "password", null, updatedUser);

    assertEquals(Response.Status.CONFLICT, response.getStatusInfo());
  }

  @Test
  void testUpdateUserDatabaseDown() {
    when(usersDao.findByEmail(email.getAddress())).thenReturn(user);
    when(usersDao.update(null, updatedUser)).thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.updateUser(key, "password", null, updatedUser);

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  void testUpdateUser() {
    when(usersDao.findByEmail(email.getAddress())).thenReturn(user);
    when(usersDao.update(null, updatedUser)).thenReturn(updatedUser);

    Response response = resource.updateUser(key, "password", null, updatedUser);
    User result = (User) response.getEntity();

    assertAll("Assert successful user update", () -> assertEquals(Response.Status.OK, response.getStatusInfo()), () -> assertEquals(updatedUser, result));
  }

  @Test
  void testUpdateUserWithNewEmail() {
    when(usersDao.findByEmail("existingEmail")).thenReturn(user);
    when(usersDao.update("existingEmail", updatedUser)).thenReturn(updatedUser);

    Response response = resource.updateUser(key, "password", "existingEmail", updatedUser);
    User result = (User) response.getEntity();

    assertAll("Assert successful user update with new email", () -> assertEquals(Response.Status.OK, response.getStatusInfo()),
        () -> assertEquals(updatedUser, result));
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
    when(usersDao.findByEmail(email.getAddress())).thenThrow(new DatabaseException(DatabaseError.USER_NOT_FOUND));

    Response response = resource.getUser(key, "password", email.getAddress());

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  void testGetUserDatabaseDown() {
    when(usersDao.findByEmail(email.getAddress())).thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

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
  void testGetUser() {
    when(usersDao.findByEmail(email.getAddress())).thenReturn(user);

    Response response = resource.getUser(key, "password", email.getAddress());
    User result = (User) response.getEntity();

    assertAll("Assert successful get user", () -> assertEquals(Response.Status.OK, response.getStatusInfo()), () -> assertEquals(user, result));
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
    when(usersDao.findByEmail(email.getAddress())).thenThrow(new DatabaseException(DatabaseError.USER_NOT_FOUND));

    Response response = resource.deleteUser(key, "password", email.getAddress());

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  void testDeleteUserLookupDatabaseDown() {
    when(usersDao.findByEmail(email.getAddress())).thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

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
    when(usersDao.delete(email.getAddress())).thenThrow(new DatabaseException(DatabaseError.USER_NOT_FOUND));

    Response response = resource.deleteUser(key, "password", email.getAddress());

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  void testDeleteUserDatabaseDown() {
    when(usersDao.findByEmail(email.getAddress())).thenReturn(user);
    when(usersDao.delete(email.getAddress())).thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.deleteUser(key, "password", email.getAddress());

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  void testDeleteUser() {
    when(usersDao.findByEmail(email.getAddress())).thenReturn(user);
    when(usersDao.delete(email.getAddress())).thenReturn(user);

    Response response = resource.deleteUser(key, "password", email.getAddress());
    User result = (User) response.getEntity();

    assertAll("Assert successful delete user", () -> assertEquals(Response.Status.OK, response.getStatusInfo()), () -> assertEquals(user, result));
  }
}
