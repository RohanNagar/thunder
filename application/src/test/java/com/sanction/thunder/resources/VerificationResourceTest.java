package com.sanction.thunder.resources;

import com.codahale.metrics.MetricRegistry;

import com.sanction.thunder.authentication.Key;
import com.sanction.thunder.dao.PilotUsersDao;
import com.sanction.thunder.models.Email;
import com.sanction.thunder.models.PilotUser;

import javax.ws.rs.core.Response;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VerificationResourceTest {
  private final PilotUsersDao usersDao = mock(PilotUsersDao.class);
  private final MetricRegistry metrics = new MetricRegistry();
  private final Key key = mock(Key.class);

  private final PilotUser unverifiedMockUser =
      new PilotUser(new Email("test@test.com", false, "verificationToken"), "password", "", "", "");
  private final PilotUser verifiedMockUser =
      new PilotUser(new Email("test@test.com", true, "verificationToken"), "password", "", "", "");
  private final PilotUser nullDatabaseTokenMockUser =
      new PilotUser(new Email("test@test.com", false, null), "password", "", "", "");
  private final PilotUser mismatchedTokenMockUser =
      new PilotUser(new Email("test@test.com", false, "mismatchedToken"), "password", "", "", "");

  private final VerificationResource resource = new VerificationResource(usersDao, metrics);


  /* Verify Email Tests */
  @Test
  public void testVerifyEmailWithNullEmail() {
    Response response =
        resource.verifyEmail(key, null, "verificationToken");

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  public void testVerifyEmailWithNullToken() {
    Response response = resource.verifyEmail(key, "test@test.com", null);

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  public void testVerifyEmailWithNullDatabaseToken() {
    when(usersDao.findByEmail(anyString())).thenReturn(nullDatabaseTokenMockUser);

    Response response = resource.verifyEmail(key, "test@test.com", "verificationToken");

    assertEquals(response.getStatusInfo(), Response.Status.INTERNAL_SERVER_ERROR);
  }

  @Test
  public void testVerifyEmailWithMismatchedToken() {
    when(usersDao.findByEmail(anyString())).thenReturn(mismatchedTokenMockUser);

    Response response = resource.verifyEmail(key, "test@test.com", "verificationToken");

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  public void testVerifyEmailSuccess() {
    when(usersDao.findByEmail("test@test.com")).thenReturn(unverifiedMockUser);
    when(usersDao.update(unverifiedMockUser.getEmail().getAddress(), verifiedMockUser))
        .thenReturn(verifiedMockUser);

    Response response = resource.verifyEmail(key, "test@test.com", "verificationToken");
    String result = (String) response.getEntity();

    assertEquals(response.getStatusInfo(), Response.Status.OK);
    assertEquals("User successfully verified!", result);
  }
}
