package com.sanctionco.thunder.resources;

import com.codahale.metrics.MetricRegistry;
import com.sanctionco.jmail.EmailValidator;
import com.sanctionco.jmail.JMail;
import com.sanctionco.thunder.TestResources;
import com.sanctionco.thunder.authentication.basic.Key;
import com.sanctionco.thunder.crypto.HashAlgorithm;
import com.sanctionco.thunder.crypto.HashService;
import com.sanctionco.thunder.dao.DatabaseException;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.email.EmailService;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.ResponseType;
import com.sanctionco.thunder.models.User;
import com.sanctionco.thunder.util.MetricNameUtil;
import com.sanctionco.thunder.validation.PropertyValidator;
import com.sanctionco.thunder.validation.RequestValidator;

import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VerificationResourceTest {
  private static final String URL = "http://www.test.com/";
  private static final String SUCCESS_HTML = "<html>success!</html>";

  private static final MetricRegistry METRICS = TestResources.METRICS;
  private static final RequestOptions OPTIONS = new RequestOptions();
  private static final EmailValidator EMAIL_VALIDATOR = JMail.strictValidator();

  private final HashService hashService = HashAlgorithm.SIMPLE.newHashService(false, false);
  private final EmailService emailService = mock(EmailService.class);
  private final UsersDao usersDao = mock(UsersDao.class);
  private final PropertyValidator propertyValidator = mock(PropertyValidator.class);
  private final RequestValidator requestValidator
      = new RequestValidator(EMAIL_VALIDATOR, propertyValidator, hashService, true);
  private final Key key = mock(Key.class);

  private static final UriInfo uriInfo = mock(UriInfo.class);
  private static final UriBuilder uriBuilder = mock(UriBuilder.class);

  private final User unverifiedMockUser =
      new User(new Email("test@test.com", false, "verificationToken"),
          "password", Collections.emptyMap());
  private final User verifiedMockUser =
      new User(new Email("test@test.com", true, "verificationToken"),
          "password", Collections.emptyMap());
  private final User nullDatabaseTokenMockUser =
      new User(new Email("test@test.com", false, null),
          "password", Collections.emptyMap());
  private final User emptyDatabaseTokenMockUser =
      new User(new Email("test@test.com", false, ""),
          "password", Collections.emptyMap());
  private final User mismatchedTokenMockUser =
      new User(new Email("test@test.com", false, "mismatchedToken"),
          "password", Collections.emptyMap());

  private final VerificationResource resource = new VerificationResource(
      usersDao, OPTIONS, requestValidator, emailService, METRICS);

  @BeforeAll
  static void setup() throws Exception {
    when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
    when(uriBuilder.queryParam(anyString(), any())).thenReturn(uriBuilder);
    when(uriBuilder.build()).thenReturn(new URI(URL));

    when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
  }

  /* Create Email Tests */
  @Test
  void email_nullEmailFailsValidation() {
    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.sendEmail(uriInfo, asyncResponse, key, null, "password");

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.BAD_REQUEST, captor.getValue().getStatusInfo());
  }

  @Test
  void email_emptyEmailFailsValidation() {
    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.sendEmail(uriInfo, asyncResponse, key, "", "password");

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.BAD_REQUEST, captor.getValue().getStatusInfo());
  }

  @Test
  void email_nullPasswordFailsValidation() {
    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.sendEmail(uriInfo, asyncResponse, key, "test@test.com", null);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.BAD_REQUEST, captor.getValue().getStatusInfo());
  }

  @Test
  void email_emptyPasswordFailsValidation() {
    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.sendEmail(uriInfo, asyncResponse, key, "test@test.com", "");

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.BAD_REQUEST, captor.getValue().getStatusInfo());
  }

  @Test
  void email_databaseFailureReturnsServiceUnavailable() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException("Error", DatabaseException.Error.DATABASE_DOWN)));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.sendEmail(uriInfo, asyncResponse, key, "test@test.com", "password");

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.SERVICE_UNAVAILABLE, captor.getValue().getStatusInfo());
  }

  @Test
  void email_databaseFailureDuringUpdateReturnsServiceUnavailable() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));
    when(usersDao.update(anyString(), any(User.class)))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException("Error", DatabaseException.Error.DATABASE_DOWN)));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.sendEmail(uriInfo, asyncResponse, key, "test@test.com", "password");

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.SERVICE_UNAVAILABLE, captor.getValue().getStatusInfo());
  }

  @Test
  void email_incorrectPasswordReturnsUnauthorized() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));
    when(usersDao.update(anyString(), any(User.class)))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.sendEmail(uriInfo, asyncResponse, key, "test@test.com", "incorrect");

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.UNAUTHORIZED, captor.getValue().getStatusInfo());
  }

  @Test
  void email_emailFailureReturnsInternalServerError() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));
    when(usersDao.update(anyString(), any(User.class)))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));
    when(emailService.sendVerificationEmail(any(Email.class), anyString()))
        .thenReturn(CompletableFuture.completedFuture(false));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.sendEmail(uriInfo, asyncResponse, key, "test@test.com", "password");

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR, captor.getValue().getStatusInfo());
  }

  @Test
  void email_disabledPasswordHeaderCheckWithNullPasswordSucceeds() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));
    when(usersDao.update(anyString(), any(User.class)))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));
    when(emailService.sendVerificationEmail(any(Email.class), anyString()))
        .thenReturn(CompletableFuture.completedFuture(true));

    var requestValidator
        = new RequestValidator(EMAIL_VALIDATOR, propertyValidator, hashService, false);
    var resource
        = new VerificationResource(usersDao, OPTIONS, requestValidator, emailService, METRICS);
    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.sendEmail(uriInfo, asyncResponse, key, "test@test.com", null);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());

    User result = (User) captor.getValue().getEntity();

    assertAll("Assert successful send email",
        () -> assertEquals(captor.getValue().getStatusInfo(), Response.Status.OK),
        () -> assertEquals(unverifiedMockUser, result));
  }

  @Test
  void email_timeoutReturns() {
    ResourceTestHelpers.runTimeoutTest(
        resp -> resource.sendEmail(uriInfo, resp, key, "test@test.com", "password"),
        MetricNameUtil.SEND_EMAIL_TIMEOUTS,
        usersDao);
  }

  @Test
  void email_isSuccessful() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));
    when(usersDao.update(anyString(), any(User.class)))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));
    when(emailService.sendVerificationEmail(any(Email.class), anyString()))
        .thenReturn(CompletableFuture.completedFuture(true));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.sendEmail(uriInfo, asyncResponse, key, "test@test.com", "password");

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());

    User result = (User) captor.getValue().getEntity();

    assertAll("Assert successful send email",
        () -> assertEquals(captor.getValue().getStatusInfo(), Response.Status.OK),
        () -> assertEquals(unverifiedMockUser, result));
  }

  /* Verify Email Tests */
  @Test
  void verify_nullEmailFailsValidation() {
    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.verifyEmail(asyncResponse, null, "verificationToken", ResponseType.JSON);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.BAD_REQUEST, captor.getValue().getStatusInfo());
  }

  @Test
  void verify_emptyEmailFailsValidation() {
    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.verifyEmail(asyncResponse, "", "verificationToken", ResponseType.JSON);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.BAD_REQUEST, captor.getValue().getStatusInfo());
  }

  @Test
  void verify_nullTokenFailsValidation() {
    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.verifyEmail(asyncResponse, "test@test.com", null, ResponseType.JSON);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.BAD_REQUEST, captor.getValue().getStatusInfo());
  }

  @Test
  void verify_emptyTokenFailsValidation() {
    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.verifyEmail(asyncResponse, "test@test.com", "", ResponseType.JSON);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.BAD_REQUEST, captor.getValue().getStatusInfo());
  }

  @Test
  void verify_databaseErrorReturnsServiceUnavailable() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException("Error", DatabaseException.Error.DATABASE_DOWN)));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.verifyEmail(
        asyncResponse, "test@test.com", "verificationToken", ResponseType.JSON);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.SERVICE_UNAVAILABLE, captor.getValue().getStatusInfo());
  }

  @Test
  void verify_nullStoredTokenReturnsInternalServerError() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.completedFuture(nullDatabaseTokenMockUser));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.verifyEmail(
        asyncResponse, "test@test.com", "verificationToken", ResponseType.JSON);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR, captor.getValue().getStatusInfo());
  }

  @Test
  void verify_emptyStoredTokenReturnsInternalServerError() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.completedFuture(emptyDatabaseTokenMockUser));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.verifyEmail(
        asyncResponse, "test@test.com", "verificationToken", ResponseType.JSON);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR, captor.getValue().getStatusInfo());
  }

  @Test
  void verify_incorrectTokenReturnsBadRequest() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.completedFuture(mismatchedTokenMockUser));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.verifyEmail(
        asyncResponse, "test@test.com", "verificationToken", ResponseType.JSON);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.BAD_REQUEST, captor.getValue().getStatusInfo());
  }

  @Test
  void verify_databaseFailureDuringUpdateReturnsServiceUnavailable() {
    when(usersDao.findByEmail("test@test.com"))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));
    when(usersDao.update(unverifiedMockUser.getEmail().getAddress(), verifiedMockUser))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException("Error", DatabaseException.Error.DATABASE_DOWN)));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.verifyEmail(
        asyncResponse, "test@test.com", "verificationToken", ResponseType.JSON);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.SERVICE_UNAVAILABLE, captor.getValue().getStatusInfo());
  }

  @Test
  void verify_timeoutReturns() {
    ResourceTestHelpers.runTimeoutTest(
        resp ->
            resource.verifyEmail(resp, "test@test.com", "verificationToken", ResponseType.JSON),
        MetricNameUtil.VERIFY_TIMEOUTS,
        usersDao);
  }

  @Test
  void verify_isSuccessful() {
    when(usersDao.findByEmail("test@test.com"))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));
    when(usersDao.update(unverifiedMockUser.getEmail().getAddress(), verifiedMockUser))
        .thenReturn(CompletableFuture.completedFuture(verifiedMockUser));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.verifyEmail(
        asyncResponse, "test@test.com", "verificationToken", ResponseType.JSON);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());

    User result = (User) captor.getValue().getEntity();

    assertAll("Assert successful verify email with JSON response",
        () -> assertEquals(captor.getValue().getStatusInfo(), Response.Status.OK),
        () -> assertEquals(verifiedMockUser, result));
  }

  @Test
  void verify_withHtmlResponseTypeIsSuccessful() {
    when(usersDao.findByEmail("test@test.com"))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));
    when(usersDao.update(unverifiedMockUser.getEmail().getAddress(), verifiedMockUser))
        .thenReturn(CompletableFuture.completedFuture(verifiedMockUser));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.verifyEmail(
        asyncResponse, "test@test.com", "verificationToken", ResponseType.HTML);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());

    URI result = captor.getValue().getLocation();

    assertAll("Assert successful verify email with HTML response",
        () -> assertEquals(captor.getValue().getStatusInfo(), Response.Status.SEE_OTHER),
        () -> assertEquals(UriBuilder.fromUri("/verify/success").build(), result));
  }

  /* Reset Tests */
  @Test
  void reset_nullEmailFailsValidation() {
    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.resetVerified(asyncResponse, key, null, "password");

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.BAD_REQUEST, captor.getValue().getStatusInfo());
  }

  @Test
  void reset_emptyEmailFailsValidation() {
    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.resetVerified(asyncResponse, key, "", "password");

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.BAD_REQUEST, captor.getValue().getStatusInfo());
  }

  @Test
  void reset_nullPasswordFailsValidation() {
    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.resetVerified(asyncResponse, key, "test@test.com", null);

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.BAD_REQUEST, captor.getValue().getStatusInfo());
  }

  @Test
  void reset_emptyPasswordFailsValidation() {
    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.resetVerified(asyncResponse, key, "test@test.com", "");

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.BAD_REQUEST, captor.getValue().getStatusInfo());
  }

  @Test
  void reset_databaseFailureDuringFindReturnsServiceUnavailable() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException("Error", DatabaseException.Error.DATABASE_DOWN)));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.resetVerified(asyncResponse, key, "test@test.com", "password");

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.SERVICE_UNAVAILABLE, captor.getValue().getStatusInfo());
  }

  @Test
  void reset_databaseFailureDuringUpdateReturnsServiceUnavailable() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.completedFuture(verifiedMockUser));
    when(usersDao.update(eq(null), any(User.class)))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException("Error", DatabaseException.Error.DATABASE_DOWN)));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.resetVerified(asyncResponse, key, "test@test.com", "password");

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.SERVICE_UNAVAILABLE, captor.getValue().getStatusInfo());
  }

  @Test
  void reset_incorrectPasswordReturnsUnauthorized() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.completedFuture(verifiedMockUser));
    when(usersDao.update(anyString(), any(User.class)))
        .thenReturn(CompletableFuture.completedFuture(verifiedMockUser));

    var asyncResponse = mock(AsyncResponse.class);
    var captor = ArgumentCaptor.forClass(Response.class);

    resource.resetVerified(asyncResponse, key, "test@test.com", "incorrect");

    verify(asyncResponse, timeout(100).times(1)).resume(captor.capture());
    assertEquals(Response.Status.UNAUTHORIZED, captor.getValue().getStatusInfo());
  }

  @Test
  void reset_disabledPasswordHeaderCheckAndNullPasswordSucceeds() {
    var requestValidator
        = new RequestValidator(EMAIL_VALIDATOR, propertyValidator, hashService, false);
    var resource
        = new VerificationResource(usersDao, OPTIONS, requestValidator, emailService, METRICS);

    // Set up the user that should already exist in the database
    Email existingEmail = new Email("existing@test.com", true, "token");
    User existingUser = new User(existingEmail, "password", Collections.emptyMap());

    // Set up expected user object
    Email updatedEmail = new Email("existing@test.com", false, null);
    User updatedUser = new User(updatedEmail, "password", Collections.emptyMap());

    var userCaptor = ArgumentCaptor.forClass(User.class);

    when(usersDao.findByEmail(existingEmail.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(existingUser));
    when(usersDao.update(eq(null), userCaptor.capture()))
        .thenReturn(CompletableFuture.completedFuture(updatedUser));

    var asyncResponse = mock(AsyncResponse.class);
    var responseCaptor = ArgumentCaptor.forClass(Response.class);

    resource.resetVerified(asyncResponse, key, existingEmail.getAddress(), null);

    verify(asyncResponse, timeout(100).times(1)).resume(responseCaptor.capture());

    User result = (User) responseCaptor.getValue().getEntity();

    assertAll("Assert successful verification status reset",
        () -> assertEquals(responseCaptor.getValue().getStatusInfo(), Response.Status.OK),
        () -> assertEquals(updatedUser, userCaptor.getValue()),
        () -> assertEquals(updatedUser, result));
  }

  @Test
  void reset_timeoutReturns() {
    ResourceTestHelpers.runTimeoutTest(
        resp -> resource.resetVerified(resp, key, "existing@test.com", "password"),
        MetricNameUtil.VERIFICATION_RESET_TIMEOUTS,
        usersDao);
  }

  @Test
  void reset_isSuccessful() {
    // Set up the user that should already exist in the database
    Email existingEmail = new Email("existing@test.com", true, "token");
    User existingUser = new User(existingEmail, "password", Collections.emptyMap());

    // Set up expected user object
    Email updatedEmail = new Email("existing@test.com", false, null);
    User updatedUser = new User(updatedEmail, "password", Collections.emptyMap());

    var userCaptor = ArgumentCaptor.forClass(User.class);

    when(usersDao.findByEmail(existingEmail.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(existingUser));
    when(usersDao.update(eq(null), userCaptor.capture()))
        .thenReturn(CompletableFuture.completedFuture(updatedUser));

    var asyncResponse = mock(AsyncResponse.class);
    var responseCaptor = ArgumentCaptor.forClass(Response.class);

    resource.resetVerified(asyncResponse, key, existingEmail.getAddress(),
        existingUser.getPassword());

    verify(asyncResponse, timeout(100).times(1)).resume(responseCaptor.capture());

    User result = (User) responseCaptor.getValue().getEntity();

    assertAll("Assert successful verification status reset",
        () -> assertEquals(responseCaptor.getValue().getStatusInfo(), Response.Status.OK),
        () -> assertEquals(updatedUser, userCaptor.getValue()),
        () -> assertEquals(updatedUser, result));
  }

  /* HTML Success Tests */
  @Test
  void testGetSuccessHtml() {
    when(emailService.getSuccessHtml()).thenReturn(SUCCESS_HTML);

    Response response = resource.getSuccessHtml();
    String result = (String) response.getEntity();

    assertAll("Assert successful HTML response",
        () -> assertEquals(Response.Status.OK, response.getStatusInfo()),
        () -> assertEquals(SUCCESS_HTML, result));
  }
}
