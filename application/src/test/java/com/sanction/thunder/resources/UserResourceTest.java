package com.sanction.thunder.resources;

import com.codahale.metrics.MetricRegistry;
import com.sanction.thunder.authentication.Key;
import com.sanction.thunder.dao.DatabaseError;
import com.sanction.thunder.dao.DatabaseException;
import com.sanction.thunder.dao.PilotUsersDao;
import com.sanction.thunder.models.PilotUser;

import javax.ws.rs.core.Response;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserResourceTest {
  private final PilotUser user = new PilotUser("email", "password", "", "", "", "");
  private final PilotUser updatedUser = new PilotUser("email", "newPassword", "", "", "", "");

  private final PilotUsersDao usersDao = mock(PilotUsersDao.class);
  private final MetricRegistry metrics = new MetricRegistry();
  private final Key key = mock(Key.class);

  private final UserResource resource = new UserResource(usersDao, metrics);

  @Test
  public void testPostNullUser() {
    Response response = resource.postUser(key, null);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  public void testPostUserDatabaseDown() {
    PilotUser pilotUser = mock(PilotUser.class);
    when(usersDao.insert(pilotUser)).thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.postUser(key, pilotUser);

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  public void testPostUserConflict() {
    PilotUser pilotUser = mock(PilotUser.class);
    when(usersDao.insert(pilotUser)).thenThrow(new DatabaseException(DatabaseError.CONFLICT));

    Response response = resource.postUser(key, pilotUser);

    assertEquals(Response.Status.CONFLICT, response.getStatusInfo());
  }

  @Test
  public void testPostUser() {
    PilotUser pilotUser = mock(PilotUser.class);
    when(usersDao.insert(pilotUser)).thenReturn(pilotUser);

    Response response = resource.postUser(key, pilotUser);
    PilotUser result = (PilotUser) response.getEntity();

    assertEquals(Response.Status.CREATED, response.getStatusInfo());
    assertEquals(pilotUser, result);
  }

  @Test
  public void testUpdateNullUser() {
    Response response = resource.updateUser(key, "password", null);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  public void testUpdateUserWithNullPassword() {
    PilotUser pilotUser = mock(PilotUser.class);

    Response response = resource.updateUser(key, null, pilotUser);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  public void testUpdateUserLookupNotFound() {
    when(usersDao.findByEmail("email"))
        .thenThrow(new DatabaseException(DatabaseError.USER_NOT_FOUND));

    Response response = resource.updateUser(key, "password", user);

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  public void testUpdateUserLookupDatabaseDown() {
    when(usersDao.findByEmail("email"))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.updateUser(key, "password", user);

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  public void testUpdateUserMismatch() {
    when(usersDao.findByEmail("email")).thenReturn(user);

    Response response = resource.updateUser(key, "incorrectPassword", updatedUser);

    assertEquals(Response.Status.UNAUTHORIZED, response.getStatusInfo());
  }

  @Test
  public void testUpdateUserNotFound() {
    when(usersDao.findByEmail("email")).thenReturn(user);
    when(usersDao.update(updatedUser))
        .thenThrow(new DatabaseException(DatabaseError.USER_NOT_FOUND));

    Response response = resource.updateUser(key, "password", updatedUser);

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  public void testUpdateUserConflict() {
    when(usersDao.findByEmail("email")).thenReturn(user);
    when(usersDao.update(updatedUser))
        .thenThrow(new DatabaseException(DatabaseError.CONFLICT));

    Response response = resource.updateUser(key, "password", updatedUser);

    assertEquals(Response.Status.CONFLICT, response.getStatusInfo());
  }

  @Test
  public void testUpdateUserDatabaseDown() {
    when(usersDao.findByEmail("email")).thenReturn(user);
    when(usersDao.update(updatedUser))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.updateUser(key, "password", updatedUser);

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  public void testUpdateUser() {
    when(usersDao.findByEmail("email")).thenReturn(user);
    when(usersDao.update(updatedUser)).thenReturn(updatedUser);

    Response response = resource.updateUser(key, "password", updatedUser);
    PilotUser result = (PilotUser) response.getEntity();

    assertEquals(Response.Status.OK, response.getStatusInfo());
    assertEquals(updatedUser, result);
  }

  @Test
  public void testGetUserWithNullEmail() {
    Response response = resource.getUser(key, "password", null);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  public void testGetUserWithNullPassword() {
    Response response = resource.getUser(key, null, "email");

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  public void testGetUserNotFound() {
    when(usersDao.findByEmail("email"))
        .thenThrow(new DatabaseException(DatabaseError.USER_NOT_FOUND));

    Response response = resource.getUser(key, "password", "email");

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  public void testGetUserDatabaseDown() {
    when(usersDao.findByEmail("email"))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.getUser(key, "password", "email");

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  public void testGetUserPasswordMismatch() {
    when(usersDao.findByEmail("email")).thenReturn(user);

    Response response = resource.getUser(key, "incorrectPassword", "email");

    assertEquals(Response.Status.UNAUTHORIZED, response.getStatusInfo());
  }

  @Test
  public void testGetUser() {
    when(usersDao.findByEmail("email")).thenReturn(user);

    Response response = resource.getUser(key, "password", "email");
    PilotUser result = (PilotUser) response.getEntity();

    assertEquals(Response.Status.OK, response.getStatusInfo());
    assertEquals(user, result);
  }

  @Test
  public void testDeleteUserWithNullEmail() {
    Response response = resource.deleteUser(key, "password", null);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  public void testDeleteUserWithNullPassword() {
    Response response = resource.deleteUser(key, null, "email");

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  public void testDeleteUserLookupNotFound() {
    when(usersDao.findByEmail("email"))
        .thenThrow(new DatabaseException(DatabaseError.USER_NOT_FOUND));

    Response response = resource.deleteUser(key, "password", "email");

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  public void testDeleteUserLookupDatabaseDown() {
    when(usersDao.findByEmail("email"))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.deleteUser(key, "password", "email");

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  public void testDeleteUserPasswordMismatch() {
    when(usersDao.findByEmail("email")).thenReturn(user);

    Response response = resource.deleteUser(key, "incorrectPassword", "email");

    assertEquals(Response.Status.UNAUTHORIZED, response.getStatusInfo());
  }

  @Test
  public void testDeleteUserNotFound() {
    when(usersDao.findByEmail("email")).thenReturn(user);
    when(usersDao.delete("email"))
        .thenThrow(new DatabaseException(DatabaseError.USER_NOT_FOUND));

    Response response = resource.deleteUser(key, "password", "email");

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  public void testDeleteUserDatabaseDown() {
    when(usersDao.findByEmail("email")).thenReturn(user);
    when(usersDao.delete("email"))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.deleteUser(key, "password", "email");

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  public void testDeleteUser() {
    when(usersDao.findByEmail("email")).thenReturn(user);
    when(usersDao.delete("email")).thenReturn(user);

    Response response = resource.deleteUser(key, "password", "email");
    PilotUser result = (PilotUser) response.getEntity();

    assertEquals(Response.Status.OK, response.getStatusInfo());
    assertEquals(user, result);
  }
}
