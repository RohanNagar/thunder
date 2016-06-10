//package com.sanction.thunder.resources;
//
//import com.codahale.metrics.MetricRegistry;
//import com.sanction.thunder.authentication.Key;
//import com.sanction.thunder.dao.PilotUsersDao;
//import com.sanction.thunder.models.PilotUser;
//
//import javax.ws.rs.core.Response;
//import org.junit.Test;
//
//import static org.junit.Assert.assertEquals;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//public class UserResourceTest {
//  private final PilotUsersDao usersDao = mock(PilotUsersDao.class);
//  private final MetricRegistry metrics = new MetricRegistry();
//  private final Key key = mock(Key.class);
//
//  private final UserResource resource = new UserResource(usersDao, metrics);
//
//  @Test
//  public void testPostNullUser() {
//    Response response = resource.postUser(key, null);
//
//    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
//  }
//
//  @Test
//  public void testPostUserFailure() {
//    PilotUser pilotUser = mock(PilotUser.class);
//    when(usersDao.insert(pilotUser)).thenReturn(null);
//
//    Response response = resource.postUser(key, pilotUser);
//
//    assertEquals(Response.Status.CONFLICT, response.getStatusInfo());
//  }
//
//  @Test
//  public void testPostUser() {
//    PilotUser pilotUser = mock(PilotUser.class);
//    when(usersDao.insert(pilotUser)).thenReturn(pilotUser);
//
//    Response response = resource.postUser(key, pilotUser);
//    PilotUser result = (PilotUser) response.getEntity();
//
//    assertEquals(Response.Status.CREATED, response.getStatusInfo());
//    assertEquals(pilotUser, result);
//  }
//
//  @Test
//  public void testUpdateNullUser() {
//    Response response = resource.updateUser(key, "password", null);
//
//    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
//  }
//
//  @Test
//  public void testUpdateUserWithNullPassword() {
//    PilotUser pilotUser = mock(PilotUser.class);
//
//    Response response = resource.updateUser(key, null, pilotUser);
//
//    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
//  }
//
//  @Test
//  public void testUpdateUserLookupFailure() {
//    PilotUser updateUser = mock(PilotUser.class);
//    when(usersDao.findByUsername("username")).thenReturn(null);
//
//    Response response = resource.updateUser(key, "password", updateUser);
//
//    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
//  }
//
//  @Test
//  public void testUpdateUserMismatch() {
//    PilotUser lookupUser = new PilotUser("username", "password", "", "", "");
//    PilotUser updateUser = new PilotUser("username", "newPassword", "", "", "");
//    when(usersDao.findByUsername("username")).thenReturn(lookupUser);
//
//    Response response = resource.updateUser(key, "incorrectPassword", updateUser);
//
//    assertEquals(Response.Status.UNAUTHORIZED, response.getStatusInfo());
//  }
//
//  @Test
//  public void testUpdateUserFailure() {
//    PilotUser lookupUser = new PilotUser("username", "password", "", "", "");
//    PilotUser updateUser = new PilotUser("username", "newPassword", "", "", "");
//    when(usersDao.findByUsername("username")).thenReturn(lookupUser);
//    when(usersDao.update(updateUser)).thenReturn(null);
//
//    Response response = resource.updateUser(key, "password", updateUser);
//
//    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
//  }
//
//  @Test
//  public void testUpdateUser() {
//    PilotUser lookupUser = new PilotUser("username", "password", "", "", "");
//    PilotUser updateUser = new PilotUser("username", "newPassword", "", "", "");
//    when(usersDao.findByUsername("username")).thenReturn(lookupUser);
//    when(usersDao.update(updateUser)).thenReturn(updateUser);
//
//    Response response = resource.updateUser(key, "password", updateUser);
//    PilotUser result = (PilotUser) response.getEntity();
//
//    assertEquals(Response.Status.OK, response.getStatusInfo());
//    assertEquals(updateUser, result);
//  }
//
//  @Test
//  public void testGetUserWithNullUsername() {
//    Response response = resource.getUser(key, "password", null);
//
//    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
//  }
//
//  @Test
//  public void testGetUserWithNullPassword() {
//    Response response = resource.getUser(key, null, "username");
//
//    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
//  }
//
//  @Test
//  public void testGetUserFailure() {
//    when(usersDao.findByUsername("username")).thenReturn(null);
//
//    Response response = resource.getUser(key, "password", "username");
//
//    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
//  }
//
//  @Test
//  public void testGetUserPasswordMismatch() {
//    PilotUser pilotUser = new PilotUser("username", "password", "", "", "");
//    when(usersDao.findByUsername("username")).thenReturn(pilotUser);
//
//    Response response = resource.getUser(key, "incorrectPassword", "username");
//
//    assertEquals(Response.Status.UNAUTHORIZED, response.getStatusInfo());
//  }
//
//  @Test
//  public void testGetUser() {
//    PilotUser pilotUser = new PilotUser("username", "password", "", "", "");
//    when(usersDao.findByUsername("username")).thenReturn(pilotUser);
//
//    Response response = resource.getUser(key, "password", "username");
//    PilotUser result = (PilotUser) response.getEntity();
//
//    assertEquals(Response.Status.OK, response.getStatusInfo());
//    assertEquals(pilotUser, result);
//  }
//
//  @Test
//  public void testDeleteUserWithNullUsername() {
//    Response response = resource.deleteUser(key, "password", null);
//
//    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
//  }
//
//  @Test
//  public void testDeleteUserWithNullPassword() {
//    Response response = resource.deleteUser(key, null, "username");
//
//    assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo());
//  }
//
//  @Test
//  public void testDeleteUserLookupFailure() {
//    when(usersDao.findByUsername("username")).thenReturn(null);
//
//    Response response = resource.deleteUser(key, "password", "username");
//
//    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
//  }
//
//  @Test
//  public void testDeleteUserPasswordMismatch() {
//    PilotUser pilotUser = new PilotUser("username", "password", "", "", "");
//    when(usersDao.findByUsername("username")).thenReturn(pilotUser);
//
//    Response response = resource.deleteUser(key, "incorrectPassword", "username");
//
//    assertEquals(Response.Status.UNAUTHORIZED, response.getStatusInfo());
//  }
//
//  @Test
//  public void testDeleteUserFailure() {
//    PilotUser pilotUser = new PilotUser("username", "password", "", "", "");
//    when(usersDao.findByUsername("username")).thenReturn(pilotUser);
//    when(usersDao.delete("username")).thenReturn(null);
//
//    Response response = resource.deleteUser(key, "password", "username");
//
//    assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo());
//  }
//
//  @Test
//  public void testDeleteUser() {
//    PilotUser pilotUser = new PilotUser("username", "password", "", "", "");
//    when(usersDao.findByUsername("username")).thenReturn(pilotUser);
//    when(usersDao.delete("username")).thenReturn(pilotUser);
//
//    Response response = resource.deleteUser(key, "password", "username");
//    PilotUser result = (PilotUser) response.getEntity();
//
//    assertEquals(Response.Status.OK, response.getStatusInfo());
//    assertEquals(pilotUser, result);
//  }
//}
