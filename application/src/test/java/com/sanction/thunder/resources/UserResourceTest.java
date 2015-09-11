package com.sanction.thunder.resources;

import com.codahale.metrics.MetricRegistry;
import com.sanction.thunder.authentication.Key;
import com.sanction.thunder.dao.StormUsersDao;
import com.sanction.thunder.models.StormUser;
import javax.ws.rs.core.Response;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserResourceTest {
  private final StormUsersDao usersDao = mock(StormUsersDao.class);
  private final MetricRegistry metrics = new MetricRegistry();
  private final Key key = mock(Key.class);

  private final UserResource resource = new UserResource(usersDao, metrics);

  @Test
  public void testPostNullUser() {
    Response response = resource.postUser(key, null);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  public void testPostUserFailure() {
    StormUser user = mock(StormUser.class);
    when(usersDao.insert(user)).thenReturn(null);

    Response response = resource.postUser(key, user);

    assertEquals(Response.Status.CONFLICT, response.getStatusInfo());
  }

  @Test
  public void testPostUser() {
    StormUser user = mock(StormUser.class);
    when(usersDao.insert(user)).thenReturn(user);

    Response response = resource.postUser(key, user);
    StormUser result = (StormUser) response.getEntity();

    assertEquals(Response.Status.CREATED, response.getStatusInfo());
    assertEquals(user, result);
  }

  @Test
  public void testUpdateNullUser() {
    Response response = resource.updateUser(key, null);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  public void testUpdateUserFailure() {
    StormUser user = mock(StormUser.class);
    when(usersDao.update(user)).thenReturn(null);

    Response response = resource.updateUser(key, user);

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  public void testUpdateUser() {
    StormUser user = mock(StormUser.class);
    when(usersDao.update(user)).thenReturn(user);

    Response response = resource.updateUser(key, user);
    StormUser result = (StormUser) response.getEntity();

    assertEquals(Response.Status.OK, response.getStatusInfo());
    assertEquals(user, result);
  }

  @Test
  public void testGetUserWithNullUsername() {
    Response response = resource.getUser(key, null);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  public void testGetUserFailure() {
    when(usersDao.findByUsername("test")).thenReturn(null);

    Response response = resource.getUser(key, "test");

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  public void testGetUser() {
    StormUser user = mock(StormUser.class);
    when(usersDao.findByUsername("test")).thenReturn(user);

    Response response = resource.getUser(key, "test");
    StormUser result = (StormUser) response.getEntity();

    assertEquals(Response.Status.OK, response.getStatusInfo());
    assertEquals(user, result);
  }

  @Test
  public void testDeleteNullUser() {
    Response response = resource.deleteUser(key, null);

    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
  }

  @Test
  public void testDeleteUserFailure() {
    when(usersDao.delete("test")).thenReturn(null);

    Response response = resource.deleteUser(key, "test");

    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
  }

  @Test
  public void testDeleteUser() {
    StormUser user = mock(StormUser.class);
    when(usersDao.delete("test")).thenReturn(user);

    Response response = resource.deleteUser(key, "test");
    StormUser result = (StormUser) response.getEntity();

    assertEquals(Response.Status.OK, response.getStatusInfo());
    assertEquals(user, result);
  }
}
