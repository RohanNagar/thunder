package com.sanction.thunder.resources;

import com.codahale.metrics.MetricRegistry;

import com.sanction.thunder.authentication.Key;
import com.sanction.thunder.dao.DatabaseError;
import com.sanction.thunder.dao.DatabaseException;
import com.sanction.thunder.dao.UsersDao;
import com.sanction.thunder.email.EmailService;
import com.sanction.thunder.models.Email;
import com.sanction.thunder.models.ResponseType;
import com.sanction.thunder.models.User;

import java.net.URI;
import java.util.Collections;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

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
  private final String successHtmlPath = "fixtures/success-fixture.html";
  private final String verificationHtmlPath = "fixtures/verification-fixture.html";
  private final String verificationTextPath = "fixtures/verification-fixture.text";

  private final User unverifiedMockUser =
      new User(new Email("test@test.com", false, "verificationToken"),
          "password", Collections.emptyMap());
  private final User verifiedMockUser =
      new User(new Email("test@test.com", true, "verificationToken"),
          "password", Collections.emptyMap());
  private final User nullDatabaseTokenMockUser =
      new User(new Email("test@test.com", false, null),
          "password", Collections.emptyMap());
  private final User mismatchedTokenMockUser =
      new User(new Email("test@test.com", false, "mismatchedToken"),
          "password", Collections.emptyMap());

  private final VerificationResource resource =
      new VerificationResource(usersDao, metrics, emailService, successHtmlPath,
          verificationHtmlPath, verificationTextPath);

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
    when(usersDao.update(anyString(), any(User.class)))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.createVerificationEmail(key, "test@test.com", "password");

    assertEquals(response.getStatusInfo(), Response.Status.SERVICE_UNAVAILABLE);
  }

  @Test
  public void testVerifyUserSendEmailException() {
    when(usersDao.findByEmail(anyString())).thenReturn(nullDatabaseTokenMockUser);
    when(usersDao.update(anyString(), any(User.class)))
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
    when(usersDao.update(anyString(), any(User.class)))
        .thenReturn(unverifiedMockUser);
    when(emailService.sendEmail(any(Email.class),
        anyString(),
        anyString(),
        anyString())).thenReturn(true);

    Response response = resource.createVerificationEmail(key, "test@test.com", "password");
    User result = (User) response.getEntity();

    assertEquals(response.getStatusInfo(), Response.Status.OK);
    assertEquals(unverifiedMockUser, result);
  }

  /* Verify Email Tests */
  @Test
  public void testVerifyEmailWithNullEmail() {
    Response response = resource.verifyEmail(null, "verificationToken", ResponseType.JSON);

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  public void testVerifyEmailWithNullToken() {
    Response response = resource.verifyEmail("test@test.com", null, ResponseType.JSON);

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  public void testVerifyEmailFindUserException() {
    when(usersDao.findByEmail(anyString()))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.verifyEmail("test@test.com", "verificationToken",
        ResponseType.JSON);

    assertEquals(response.getStatusInfo(), Response.Status.SERVICE_UNAVAILABLE);
  }

  @Test
  public void testVerifyEmailWithNullDatabaseToken() {
    when(usersDao.findByEmail(anyString())).thenReturn(nullDatabaseTokenMockUser);

    Response response = resource.verifyEmail("test@test.com", "verificationToken",
        ResponseType.JSON);

    assertEquals(response.getStatusInfo(), Response.Status.INTERNAL_SERVER_ERROR);
  }

  @Test
  public void testVerifyEmailWithMismatchedToken() {
    when(usersDao.findByEmail(anyString())).thenReturn(mismatchedTokenMockUser);

    Response response = resource.verifyEmail("test@test.com", "verificationToken",
        ResponseType.JSON);

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  public void testVerifyEmailUpdateUserException() {
    when(usersDao.findByEmail("test@test.com")).thenReturn(unverifiedMockUser);
    when(usersDao.update(unverifiedMockUser.getEmail().getAddress(), verifiedMockUser))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.verifyEmail("test@test.com", "verificationToken",
        ResponseType.JSON);

    assertEquals(response.getStatusInfo(), Response.Status.SERVICE_UNAVAILABLE);
  }

  @Test
  public void testVerifyEmailSuccess() {
    when(usersDao.findByEmail("test@test.com")).thenReturn(unverifiedMockUser);
    when(usersDao.update(unverifiedMockUser.getEmail().getAddress(), verifiedMockUser))
        .thenReturn(verifiedMockUser);

    Response response = resource.verifyEmail("test@test.com", "verificationToken",
        ResponseType.JSON);
    User result = (User) response.getEntity();

    assertEquals(response.getStatusInfo(), Response.Status.OK);
    assertEquals(verifiedMockUser, result);
  }

  @Test
  public void testVerifyEmailWithHtmlResponse() {
    when(usersDao.findByEmail("test@test.com")).thenReturn(unverifiedMockUser);
    when(usersDao.update(unverifiedMockUser.getEmail().getAddress(), verifiedMockUser))
        .thenReturn(verifiedMockUser);

    Response response = resource.verifyEmail("test@test.com", "verificationToken",
        ResponseType.HTML);
    URI result = response.getLocation();

    assertEquals(response.getStatusInfo(), Response.Status.SEE_OTHER);
    assertEquals(UriBuilder.fromUri("/verify/success").build(), result);
  }

  /* HTML Success Tests */
  @Test
  public void testGetSuccessHtml() {
    String expected = "fixtures/success-fixture.html";

    Response response = resource.getSuccessHtml();
    String result = (String) response.getEntity();

    assertEquals(Response.Status.OK, response.getStatusInfo());
    assertEquals(expected, result);
  }
}
