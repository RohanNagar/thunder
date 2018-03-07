package com.sanction.thunder.resources;

import com.codahale.metrics.MetricRegistry;

import com.sanction.thunder.authentication.Key;
import com.sanction.thunder.dao.DatabaseError;
import com.sanction.thunder.dao.DatabaseException;
import com.sanction.thunder.dao.UsersDao;
import com.sanction.thunder.email.EmailService;
import com.sanction.thunder.models.Email;
import com.sanction.thunder.models.PilotUser;

import javax.ws.rs.core.Response;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VerificationResourceTest {
  private final UsersDao usersDao = mock(UsersDao.class);
  private final MetricRegistry metrics = new MetricRegistry();
  private final Key key = mock(Key.class);
  private final EmailService emailService = mock(EmailService.class);

  private final PilotUser unverifiedMockUser =
      new PilotUser(new Email("test@test.com", false, "verificationToken"), "password", "", "", "");
  private final PilotUser verifiedMockUser =
      new PilotUser(new Email("test@test.com", true, "verificationToken"), "password", "", "", "");
  private final PilotUser nullDatabaseTokenMockUser =
      new PilotUser(new Email("test@test.com", false, null), "password", "", "", "");
  private final PilotUser mismatchedTokenMockUser =
      new PilotUser(new Email("test@test.com", false, "mismatchedToken"), "password", "", "", "");

  private final VerificationResource resource =
      new VerificationResource(usersDao, metrics, emailService);

  /* Verify User Tests */
  @Test
  public void testVerifyUserWithNullEmail() {
    Response response = resource.createVerificationEmail(key, null, "password");

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  public void testVerifyUserWithNullPassword() {
    Response response = resource.createVerificationEmail(key, "test@test.com", null);

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  public void testVerifyUserFindUserException() {
    when(usersDao.findByEmail(anyString()))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.createVerificationEmail(key, "test@test.com", "password");

    assertEquals(response.getStatusInfo(), Response.Status.SERVICE_UNAVAILABLE);
  }

  @Test
  public void testVerifyUserUpdateUserException() {
    when(usersDao.findByEmail(anyString())).thenReturn(nullDatabaseTokenMockUser);
    when(usersDao.update(anyString(), any(PilotUser.class)))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.createVerificationEmail(key, "test@test.com", "password");

    assertEquals(response.getStatusInfo(), Response.Status.SERVICE_UNAVAILABLE);
  }

  @Test
  public void testVerifyUserSendEmailException() {
    when(usersDao.findByEmail(anyString())).thenReturn(nullDatabaseTokenMockUser);
    when(usersDao.update(anyString(), any(PilotUser.class)))
        .thenReturn(unverifiedMockUser);
    when(emailService.sendEmail(unverifiedMockUser.getEmail(),
        "Account Verification",
        "Test email",
        "Test email")).thenReturn(false);

    Response response = resource.createVerificationEmail(key, "test@test.com", "password");

    assertEquals(response.getStatusInfo(), Response.Status.INTERNAL_SERVER_ERROR);
  }

  @Test
  public void testVerifyUserSuccess() {
    when(usersDao.findByEmail(anyString())).thenReturn(nullDatabaseTokenMockUser);
    when(usersDao.update(anyString(), any(PilotUser.class)))
        .thenReturn(unverifiedMockUser);
    when(emailService.sendEmail(any(Email.class),
        anyString(),
        anyString(),
        anyString())).thenReturn(true);

    Response response = resource.createVerificationEmail(key, "test@test.com", "password");
    PilotUser result = (PilotUser) response.getEntity();

    assertEquals(response.getStatusInfo(), Response.Status.OK);
    assertEquals(unverifiedMockUser, result);
  }

  /* Verify Email Tests */
  @Test
  public void testVerifyEmailWithNullEmail() {
    Response response = resource.verifyEmail(null, "verificationToken");

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  public void testVerifyEmailWithNullToken() {
    Response response = resource.verifyEmail("test@test.com", null);

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  public void testVerifyEmailFindUserException() {
    when(usersDao.findByEmail(anyString()))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.verifyEmail("test@test.com", "verificationToken");

    assertEquals(response.getStatusInfo(), Response.Status.SERVICE_UNAVAILABLE);
  }

  @Test
  public void testVerifyEmailWithNullDatabaseToken() {
    when(usersDao.findByEmail(anyString())).thenReturn(nullDatabaseTokenMockUser);

    Response response = resource.verifyEmail("test@test.com", "verificationToken");

    assertEquals(response.getStatusInfo(), Response.Status.INTERNAL_SERVER_ERROR);
  }

  @Test
  public void testVerifyEmailWithMismatchedToken() {
    when(usersDao.findByEmail(anyString())).thenReturn(mismatchedTokenMockUser);

    Response response = resource.verifyEmail("test@test.com", "verificationToken");

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  public void testVerifyEmailUpdateUserException() {
    when(usersDao.findByEmail("test@test.com")).thenReturn(unverifiedMockUser);
    when(usersDao.update(unverifiedMockUser.getEmail().getAddress(), verifiedMockUser))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.verifyEmail("test@test.com", "verificationToken");

    assertEquals(response.getStatusInfo(), Response.Status.SERVICE_UNAVAILABLE);
  }

  @Test
  public void testVerifyEmailSuccess() {
    when(usersDao.findByEmail("test@test.com")).thenReturn(unverifiedMockUser);
    when(usersDao.update(unverifiedMockUser.getEmail().getAddress(), verifiedMockUser))
        .thenReturn(verifiedMockUser);

    Response response = resource.verifyEmail("test@test.com", "verificationToken");
    PilotUser result = (PilotUser) response.getEntity();

    assertEquals(response.getStatusInfo(), Response.Status.OK);
    assertEquals(verifiedMockUser, result);
  }
}
