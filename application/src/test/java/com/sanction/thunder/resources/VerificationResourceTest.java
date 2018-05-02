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
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VerificationResourceTest {
  private final EmailService emailService = mock(EmailService.class);
  private final MetricRegistry metrics = new MetricRegistry();
  private final UsersDao usersDao = mock(UsersDao.class);
  private final Key key = mock(Key.class);

  private static final UriInfo uriInfo = mock(UriInfo.class);
  private static final UriBuilder uriBuilder = mock(UriBuilder.class);

  private final String successHtml = "<html>success!</html>";
  private final String verificationHtml = "<html>Verify</html>";
  private final String verificationText = "Verify";

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
      new VerificationResource(usersDao, metrics, emailService, successHtml, verificationHtml,
          verificationText);

  /**
   * Sets up the test suite with appropriate method stubs.
   *
   * @throws Exception If a failure occurs during setup.
   */
  @BeforeClass
  public static void setup() throws Exception {
    when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
    when(uriBuilder.queryParam(anyString(), any())).thenReturn(uriBuilder);
    when(uriBuilder.build()).thenReturn(new URI("http://www.test.com/"));

    when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
  }

  /* Verify User Tests */
  @Test
  public void testCreateVerificationEmailWithNullEmail() {
    Response response = resource.createVerificationEmail(uriInfo, key, null, "password");

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  public void testCreateVerificationEmailWithNullPassword() {
    Response response = resource.createVerificationEmail(uriInfo, key, "test@test.com", null);

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  public void testCreateVerificationEmailFindUserException() {
    when(usersDao.findByEmail(anyString()))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.createVerificationEmail(uriInfo, key, "test@test.com", "password");

    assertEquals(response.getStatusInfo(), Response.Status.SERVICE_UNAVAILABLE);
  }

  @Test
  public void testCreateVerificationEmailUpdateUserException() {
    when(usersDao.findByEmail(anyString())).thenReturn(unverifiedMockUser);
    when(usersDao.update(anyString(), any(User.class)))
        .thenThrow(new DatabaseException(DatabaseError.DATABASE_DOWN));

    Response response = resource.createVerificationEmail(uriInfo, key, "test@test.com", "password");

    assertEquals(response.getStatusInfo(), Response.Status.SERVICE_UNAVAILABLE);
  }

  @Test
  public void testCreateVerificationEmailSendEmailFailure() {
    when(usersDao.findByEmail(anyString())).thenReturn(unverifiedMockUser);
    when(usersDao.update(anyString(), any(User.class))).thenReturn(unverifiedMockUser);
    when(emailService.sendEmail(any(Email.class), anyString(), anyString(), anyString()))
        .thenReturn(false);

    Response response = resource.createVerificationEmail(uriInfo, key, "test@test.com", "password");

    assertEquals(response.getStatusInfo(), Response.Status.INTERNAL_SERVER_ERROR);
  }

  @Test
  public void testCreateVerificationEmailSuccess() {
    when(usersDao.findByEmail(anyString())).thenReturn(unverifiedMockUser);
    when(usersDao.update(anyString(), any(User.class))).thenReturn(unverifiedMockUser);
    when(emailService.sendEmail(any(Email.class), anyString(), anyString(), anyString()))
        .thenReturn(true);

    Response response = resource.createVerificationEmail(uriInfo, key, "test@test.com", "password");
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
    Response response = resource.getSuccessHtml();
    String result = (String) response.getEntity();

    assertEquals(Response.Status.OK, response.getStatusInfo());
    assertEquals(successHtml, result);
  }
}
