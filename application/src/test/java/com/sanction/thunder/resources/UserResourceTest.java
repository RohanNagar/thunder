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
  private final PilotUser user = new PilotUser("username", "password", "", "", "");
  private final PilotUser updatedUser = new PilotUser("username", "newPassword", "", "", "");

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
    when(usersDao.findByUsername("username"))
        .thenThrow(new DatabaseException(DatabaseError.USER_NOT_FOUND));

    Response response = resource.updateUser(key, "password", user);

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  public void testUpdateUserLookupDatabaseDown() {
    when(usersDao.findByUsername("username"))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.updateUser(key, "password", user);

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  public void testUpdateUserMismatch() {
    when(usersDao.findByUsername("username")).thenReturn(user);

    Response response = resource.updateUser(key, "incorrectPassword", updatedUser);

    assertEquals(Response.Status.UNAUTHORIZED, response.getStatusInfo());
  }

  @Test
  public void testUpdateUserNotFound() {
    when(usersDao.findByUsername("username")).thenReturn(user);
    when(usersDao.update(updatedUser))
        .thenThrow(new DatabaseException(DatabaseError.USER_NOT_FOUND));

    Response response = resource.updateUser(key, "password", updatedUser);

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  public void testUpdateUserConflict() {
    when(usersDao.findByUsername("username")).thenReturn(user);
    when(usersDao.update(updatedUser))
        .thenThrow(new DatabaseException(DatabaseError.CONFLICT));

    Response response = resource.updateUser(key, "password", updatedUser);

    assertEquals(Response.Status.CONFLICT, response.getStatusInfo());
  }

  @Test
  public void testUpdateUserDatabaseDown() {
    when(usersDao.findByUsername("username")).thenReturn(user);
    when(usersDao.update(updatedUser))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.updateUser(key, "password", updatedUser);

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  public void testUpdateUser() {
    when(usersDao.findByUsername("username")).thenReturn(user);
    when(usersDao.update(updatedUser)).thenReturn(updatedUser);

    Response response = resource.updateUser(key, "password", updatedUser);
    PilotUser result = (PilotUser) response.getEntity();

    assertEquals(Response.Status.OK, response.getStatusInfo());
    assertEquals(updatedUser, result);
  }

  @Test
  public void testGetUserWithNullUsername() {
    Response response = resource.getUser(key, "password", null);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  public void testGetUserWithNullPassword() {
    Response response = resource.getUser(key, null, "username");

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  public void testGetUserNotFound() {
    when(usersDao.findByUsername("username"))
        .thenThrow(new DatabaseException(DatabaseError.USER_NOT_FOUND));

    Response response = resource.getUser(key, "password", "username");

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  public void testGetUserDatabaseDown() {
    when(usersDao.findByUsername("username"))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.getUser(key, "password", "username");

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  public void testGetUserPasswordMismatch() {
    when(usersDao.findByUsername("username")).thenReturn(user);

    Response response = resource.getUser(key, "incorrectPassword", "username");

    assertEquals(Response.Status.UNAUTHORIZED, response.getStatusInfo());
  }

  @Test
  public void testGetUser() {
    when(usersDao.findByUsername("username")).thenReturn(user);

    Response response = resource.getUser(key, "password", "username");
    PilotUser result = (PilotUser) response.getEntity();

    assertEquals(Response.Status.OK, response.getStatusInfo());
    assertEquals(user, result);
  }

  @Test
  public void testDeleteUserWithNullUsername() {
    Response response = resource.deleteUser(key, "password", null);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  public void testDeleteUserWithNullPassword() {
    Response response = resource.deleteUser(key, null, "username");

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  public void testDeleteUserLookupNotFound() {
    when(usersDao.findByUsername("username"))
        .thenThrow(new DatabaseException(DatabaseError.USER_NOT_FOUND));

    Response response = resource.deleteUser(key, "password", "username");

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  public void testDeleteUserLookupDatabaseDown() {
    when(usersDao.findByUsername("username"))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.deleteUser(key, "password", "username");

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  public void testDeleteUserPasswordMismatch() {
    when(usersDao.findByUsername("username")).thenReturn(user);

    Response response = resource.deleteUser(key, "incorrectPassword", "username");

    assertEquals(Response.Status.UNAUTHORIZED, response.getStatusInfo());
  }

  @Test
  public void testDeleteUserNotFound() {
    when(usersDao.findByUsername("username")).thenReturn(user);
    when(usersDao.delete("username"))
        .thenThrow(new DatabaseException(DatabaseError.USER_NOT_FOUND));

    Response response = resource.deleteUser(key, "password", "username");

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  public void testDeleteUserDatabaseDown() {
    when(usersDao.findByUsername("username")).thenReturn(user);
    when(usersDao.delete("username"))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.deleteUser(key, "password", "username");

    assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo());
  }

  @Test
  public void testDeleteUser() {
    when(usersDao.findByUsername("username")).thenReturn(user);
    when(usersDao.delete("username")).thenReturn(user);

    Response response = resource.deleteUser(key, "password", "username");
    PilotUser result = (PilotUser) response.getEntity();

    assertEquals(Response.Status.OK, response.getStatusInfo());
    assertEquals(user, result);
  }
}
