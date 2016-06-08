package com.sanction.thunder.resources;

import com.codahale.metrics.MetricRegistry;
import com.sanction.thunder.authentication.Key;
import com.sanction.thunder.dao.PilotUsersDao;
import com.sanction.thunder.models.PilotUser;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserResourceTest {
  private final PilotUsersDao usersDao = mock(PilotUsersDao.class);
  private final MetricRegistry metrics = new MetricRegistry();
  private final Key key = mock(Key.class);
  private final HttpHeaders headers = mock(HttpHeaders.class);

  private final UserResource resource = new UserResource(usersDao, metrics);

  @Test
  public void testPostNullUser() {
    Response response = resource.postUser(key, null);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  public void testPostUserFailure() {
    PilotUser pilotUser = mock(PilotUser.class);
    when(usersDao.insert(pilotUser)).thenReturn(null);

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
    Response response = resource.updateUser(key, headers, null);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  public void testUpdateUserWithNullHeader() {
    PilotUser pilotUser = mock(PilotUser.class);
    when(headers.getHeaderString("password")).thenReturn(null);

    Response response = resource.updateUser(key, headers, pilotUser);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  public void testUpdateUserLookupFailure() {
    PilotUser updateUser = mock(PilotUser.class);
    when(headers.getHeaderString("password")).thenReturn("password");
    when(usersDao.findByUsername("username")).thenReturn(null);

    Response response = resource.updateUser(key, headers, updateUser);

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  public void testUpdateUserMismatch() {
    PilotUser updateUser = new PilotUser("username", "newPassword", "newField", "newField",
        "newField");
    PilotUser lookupUser = new PilotUser("username", "password", "", "", "");
    when(headers.getHeaderString("password")).thenReturn("wrong");
    when(usersDao.findByUsername("username")).thenReturn(lookupUser);

    Response response = resource.updateUser(key, headers, updateUser);

    assertEquals(Response.Status.UNAUTHORIZED, response.getStatusInfo());
  }

  @Test
  public void testUpdateUserFailure() {
    PilotUser updateUser = new PilotUser("username", "newPassword", "newField", "newField",
        "newField");
    PilotUser lookupUser = new PilotUser("username", "password", "", "", "");
    when(headers.getHeaderString("password")).thenReturn("password");
    when(usersDao.findByUsername("username")).thenReturn(lookupUser);
    when(usersDao.update(updateUser)).thenReturn(null);

    Response response = resource.updateUser(key, headers, updateUser);

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  public void testUpdateUser() {
    PilotUser updateUser = new PilotUser("username", "newPassword", "newField", "newField",
        "newField");
    PilotUser lookupUser = new PilotUser("username", "password", "", "", "");
    when(headers.getHeaderString("password")).thenReturn("password");
    when(usersDao.findByUsername("username")).thenReturn(lookupUser);
    when(usersDao.update(updateUser)).thenReturn(updateUser);

    Response response = resource.updateUser(key, headers, updateUser);
    PilotUser result = (PilotUser) response.getEntity();

    assertEquals(Response.Status.OK, response.getStatusInfo());
    assertEquals(updateUser, result);
  }

  @Test
  public void testGetUserWithNullUsername() {
    Response response = resource.getUser(key, headers, null);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  public void testGetUserWithNullHeader() {
    when(headers.getHeaderString("password")).thenReturn(null);

    Response response = resource.getUser(key, headers, "username");

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  public void testGetUserFailure() {
    when(headers.getHeaderString("password")).thenReturn("password");
    when(usersDao.findByUsername("username")).thenReturn(null);

    Response response = resource.getUser(key, headers, "username");

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  public void testGetUserPasswordMismatch() {
    PilotUser pilotUser = new PilotUser("username", "wrong", "", "", "");
    when(headers.getHeaderString("password")).thenReturn("password");
    when(usersDao.findByUsername("username")).thenReturn(pilotUser);

    Response response = resource.getUser(key, headers, "username");

    assertEquals(Response.Status.UNAUTHORIZED, response.getStatusInfo());
  }

  @Test
  public void testGetUser() {
    PilotUser pilotUser = new PilotUser("username", "password", "", "", "");
    when(headers.getHeaderString("password")).thenReturn("password");
    when(usersDao.findByUsername("username")).thenReturn(pilotUser);

    Response response = resource.getUser(key, headers, "username");
    PilotUser result = (PilotUser) response.getEntity();

    assertEquals(Response.Status.OK, response.getStatusInfo());
    assertEquals(pilotUser, result);
  }

  @Test
  public void testDeleteUserWithNullUsername() {
    Response response = resource.deleteUser(key, headers, null);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  public void testDeleteUserWithNullHeader() {
    when(headers.getHeaderString("password")).thenReturn(null);

    Response response = resource.deleteUser(key, headers, "username");

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  public void testDeleteUserLookupFailure() {
    when(headers.getHeaderString("password")).thenReturn("password");
    when(usersDao.findByUsername("username")).thenReturn(null);

    Response response = resource.deleteUser(key, headers, "username");

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  public void testDeleteUserPasswordMismatch() {
    PilotUser pilotUser = new PilotUser("username", "wrong", "", "", "");
    when(headers.getHeaderString("password")).thenReturn("password");
    when(usersDao.findByUsername("username")).thenReturn(pilotUser);

    Response response = resource.deleteUser(key, headers, "username");

    assertEquals(Response.Status.UNAUTHORIZED, response.getStatusInfo());
  }

  @Test
  public void testDeleteUserFailure() {
    PilotUser pilotUser = new PilotUser("username", "password", "", "", "");
    when(headers.getHeaderString("password")).thenReturn("password");
    when(usersDao.findByUsername("username")).thenReturn(pilotUser);
    when(usersDao.delete("username")).thenReturn(null);

    Response response = resource.deleteUser(key, headers, "username");

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  public void testDeleteUser() {
    PilotUser pilotUser = new PilotUser("username", "password", "", "", "");
    when(headers.getHeaderString("password")).thenReturn("password");
    when(usersDao.findByUsername("username")).thenReturn(pilotUser);
    when(usersDao.delete("username")).thenReturn(pilotUser);

    Response response = resource.deleteUser(key, headers, "username");
    PilotUser result = (PilotUser) response.getEntity();

    assertEquals(Response.Status.OK, response.getStatusInfo());
    assertEquals(pilotUser, result);
  }
}
