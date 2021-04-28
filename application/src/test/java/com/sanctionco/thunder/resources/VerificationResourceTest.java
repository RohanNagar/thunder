package com.sanctionco.thunder.resources;

import com.sanctionco.thunder.authentication.basic.Key;
import com.sanctionco.thunder.crypto.HashAlgorithm;
import com.sanctionco.thunder.crypto.HashService;
import com.sanctionco.thunder.dao.DatabaseError;
import com.sanctionco.thunder.dao.DatabaseException;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.email.EmailService;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.ResponseType;
import com.sanctionco.thunder.models.User;
import com.sanctionco.thunder.validation.PropertyValidator;
import com.sanctionco.thunder.validation.RequestValidator;

import java.net.URI;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.returnsSecondArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VerificationResourceTest {
  private static final String URL = "http://www.test.com/";
  private static final String SUCCESS_HTML = "<html>success!</html>";

  private final HashService hashService = HashAlgorithm.SIMPLE.newHashService(false, false);
  private final EmailService emailService = mock(EmailService.class);
  private final UsersDao usersDao = mock(UsersDao.class);
  private final PropertyValidator propertyValidator = mock(PropertyValidator.class);
  private final RequestValidator requestValidator = new RequestValidator(propertyValidator, true);
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
      usersDao, requestValidator, emailService, hashService);

  @BeforeAll
  static void setup() throws Exception {
    when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
    when(uriBuilder.queryParam(anyString(), any())).thenReturn(uriBuilder);
    when(uriBuilder.build()).thenReturn(new URI(URL));

    when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
  }

  /* Create Email Tests */
  @Test
  void testCreateVerificationEmailWithNullEmail() {
    Response response = resource.createVerificationEmail(uriInfo, key, null, "password");

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  void testCreateVerificationEmailWithEmptyEmail() {
    Response response = resource.createVerificationEmail(uriInfo, key, "", "password");

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);

  }

  @Test
  void testCreateVerificationEmailWithNullPassword() {
    Response response = resource.createVerificationEmail(uriInfo, key, "test@test.com", null);

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  void testCreateVerificationEmailWithEmptyPassword() {
    Response response = resource.createVerificationEmail(uriInfo, key, "test@test.com", "");

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  void testCreateVerificationEmailFindUserException() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException(DatabaseError.DATABASE_DOWN)));

    Response response = resource.createVerificationEmail(uriInfo, key, "test@test.com", "password");

    assertEquals(response.getStatusInfo(), Response.Status.SERVICE_UNAVAILABLE);
  }

  @Test
  void testCreateVerificationEmailUpdateUserException() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));
    when(usersDao.update(anyString(), any(User.class)))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException(DatabaseError.DATABASE_DOWN)));

    Response response = resource.createVerificationEmail(uriInfo, key, "test@test.com", "password");

    assertEquals(response.getStatusInfo(), Response.Status.SERVICE_UNAVAILABLE);
  }

  @Test
  void testCreateVerificationEmailWithIncorrectPassword() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));
    when(usersDao.update(anyString(), any(User.class)))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));

    Response response = resource.createVerificationEmail(uriInfo, key, "test@test.com",
        "incorrect");

    assertEquals(response.getStatusInfo(), Response.Status.UNAUTHORIZED);
  }

  @Test
  void testCreateVerificationEmailSendEmailFailure() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));
    when(usersDao.update(anyString(), any(User.class)))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));
    when(emailService.sendVerificationEmail(any(Email.class), anyString())).thenReturn(false);

    Response response = resource.createVerificationEmail(uriInfo, key, "test@test.com", "password");

    assertEquals(response.getStatusInfo(), Response.Status.INTERNAL_SERVER_ERROR);
  }

  @Test
  void testCreateVerificationEmailDisabledHeaderCheck() {
    RequestValidator requestValidator = new RequestValidator(propertyValidator, false);
    VerificationResource resource = new VerificationResource(
        usersDao, requestValidator, emailService, hashService);

    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));
    when(usersDao.update(anyString(), any(User.class)))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));
    when(emailService.sendVerificationEmail(any(Email.class), anyString())).thenReturn(true);

    Response response = resource.createVerificationEmail(uriInfo, key, "test@test.com", null);
    User result = (User) response.getEntity();

    assertAll("Assert successful send email",
        () -> assertEquals(response.getStatusInfo(), Response.Status.OK),
        () -> assertEquals(unverifiedMockUser, result));
  }

  @Test
  void testCreateVerificationEmailSuccess() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));
    when(usersDao.update(anyString(), any(User.class)))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));
    when(emailService.sendVerificationEmail(any(Email.class), anyString())).thenReturn(true);

    Response response = resource.createVerificationEmail(uriInfo, key, "test@test.com", "password");
    User result = (User) response.getEntity();

    assertAll("Assert successful send email",
        () -> assertEquals(response.getStatusInfo(), Response.Status.OK),
        () -> assertEquals(unverifiedMockUser, result));
  }

  /* Verify Email Tests */
  @Test
  void testVerifyEmailWithNullEmail() {
    Response response = resource.verifyEmail(null, "verificationToken", ResponseType.JSON);

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  void testVerifyEmailWithEmptyEmail() {
    Response response = resource.verifyEmail("", "verificationToken", ResponseType.JSON);

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  void testVerifyEmailWithNullToken() {
    Response response = resource.verifyEmail("test@test.com", null, ResponseType.JSON);

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  void testVerifyEmailWithEmptyToken() {
    Response response = resource.verifyEmail("test@test.com", "", ResponseType.JSON);

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  void testVerifyEmailFindUserException() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException(DatabaseError.DATABASE_DOWN)));

    Response response = resource.verifyEmail("test@test.com", "verificationToken",
        ResponseType.JSON);

    assertEquals(response.getStatusInfo(), Response.Status.SERVICE_UNAVAILABLE);
  }

  @Test
  void testVerifyEmailWithNullDatabaseToken() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.completedFuture(nullDatabaseTokenMockUser));

    Response response = resource.verifyEmail("test@test.com", "verificationToken",
        ResponseType.JSON);

    assertEquals(response.getStatusInfo(), Response.Status.INTERNAL_SERVER_ERROR);
  }

  @Test
  void testVerifyEmailWithEmptyDatabaseToken() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.completedFuture(emptyDatabaseTokenMockUser));

    Response response = resource.verifyEmail("test@test.com", "verificationToken",
        ResponseType.JSON);

    assertEquals(response.getStatusInfo(), Response.Status.INTERNAL_SERVER_ERROR);
  }

  @Test
  void testVerifyEmailWithMismatchedToken() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.completedFuture(mismatchedTokenMockUser));

    Response response = resource.verifyEmail("test@test.com", "verificationToken",
        ResponseType.JSON);

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  void testVerifyEmailUpdateUserException() {
    when(usersDao.findByEmail("test@test.com"))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));
    when(usersDao.update(unverifiedMockUser.getEmail().getAddress(), verifiedMockUser))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException(DatabaseError.DATABASE_DOWN)));

    Response response = resource.verifyEmail("test@test.com", "verificationToken",
        ResponseType.JSON);

    assertEquals(response.getStatusInfo(), Response.Status.SERVICE_UNAVAILABLE);
  }

  @Test
  void testVerifyEmailSuccess() {
    when(usersDao.findByEmail("test@test.com"))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));
    when(usersDao.update(unverifiedMockUser.getEmail().getAddress(), verifiedMockUser))
        .thenReturn(CompletableFuture.completedFuture(verifiedMockUser));

    Response response = resource.verifyEmail("test@test.com", "verificationToken",
        ResponseType.JSON);
    User result = (User) response.getEntity();

    assertAll("Assert successful verify email with JSON response",
        () -> assertEquals(response.getStatusInfo(), Response.Status.OK),
        () -> assertEquals(verifiedMockUser, result));
  }

  @Test
  void testVerifyEmailWithHtmlResponse() {
    when(usersDao.findByEmail("test@test.com"))
        .thenReturn(CompletableFuture.completedFuture(unverifiedMockUser));
    when(usersDao.update(unverifiedMockUser.getEmail().getAddress(), verifiedMockUser))
        .thenReturn(CompletableFuture.completedFuture(verifiedMockUser));

    Response response = resource.verifyEmail("test@test.com", "verificationToken",
        ResponseType.HTML);
    URI result = response.getLocation();

    assertAll("Assert successful verify email with HTML response",
        () -> assertEquals(response.getStatusInfo(), Response.Status.SEE_OTHER),
        () -> assertEquals(UriBuilder.fromUri("/verify/success").build(), result));
  }

  /* Reset Tests */
  @Test
  void testResetVerificationStatusWithNullEmail() {
    Response response = resource.resetVerificationStatus(key, null, "password");

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  void testResetVerificationStatusWithEmptyEmail() {
    Response response = resource.resetVerificationStatus(key, "", "password");

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);

  }

  @Test
  void testResetVerificationStatusWithNullPassword() {
    Response response = resource.resetVerificationStatus(key, "test@test.com", null);

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  void testResetVerificationStatusWithEmptyPassword() {
    Response response = resource.resetVerificationStatus(key, "test@test.com", "");

    assertEquals(response.getStatusInfo(), Response.Status.BAD_REQUEST);
  }

  @Test
  void testResetVerificationStatusFindUserDatabaseDown() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException(DatabaseError.DATABASE_DOWN)));

    Response response = resource.resetVerificationStatus(key, "test@test.com", "password");

    assertEquals(response.getStatusInfo(), Response.Status.SERVICE_UNAVAILABLE);
  }

  @Test
  void testResetVerificationStatusUpdateUserDatabaseDown() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.completedFuture(verifiedMockUser));
    when(usersDao.update(eq(null), any(User.class)))
        .thenReturn(CompletableFuture.failedFuture(
            new DatabaseException(DatabaseError.DATABASE_DOWN)));

    Response response = resource.resetVerificationStatus(key, "test@test.com", "password");

    assertEquals(response.getStatusInfo(), Response.Status.SERVICE_UNAVAILABLE);
  }

  @Test
  void testResetVerificationStatusWithIncorrectPassword() {
    when(usersDao.findByEmail(anyString()))
        .thenReturn(CompletableFuture.completedFuture(verifiedMockUser));
    when(usersDao.update(anyString(), any(User.class)))
        .thenReturn(CompletableFuture.completedFuture(verifiedMockUser));

    Response response = resource.resetVerificationStatus(key, "test@test.com", "incorrect");

    assertEquals(response.getStatusInfo(), Response.Status.UNAUTHORIZED);
  }

  @Test
  void testResetVerificationStatusDisabledHeaderCheck() {
    RequestValidator requestValidator = new RequestValidator(propertyValidator, false);
    VerificationResource resource = new VerificationResource(
        usersDao, requestValidator, emailService, hashService);

    // Set up the user that should already exist in the database
    Email existingEmail = new Email("existing@test.com", true, "token");
    User existingUser = new User(existingEmail, "password", Collections.emptyMap());

    // Set up expected user object
    Email updatedEmail = new Email("existing@test.com", false, null);
    User updatedUser = new User(updatedEmail, "password", Collections.emptyMap());

    var captor = ArgumentCaptor.forClass(User.class);

    when(usersDao.findByEmail(existingEmail.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(existingUser));
    when(usersDao.update(eq(null), captor.capture()))
        .thenReturn(CompletableFuture.completedFuture(updatedUser));

    Response response = resource.resetVerificationStatus(key, existingEmail.getAddress(), null);
    User result = (User) response.getEntity();

    assertAll("Assert successful verification status reset",
        () -> assertEquals(response.getStatusInfo(), Response.Status.OK),
        () -> assertEquals(updatedUser, captor.getValue()),
        () -> assertEquals(updatedUser, result));
  }

  @Test
  void testResetVerificationStatusSuccess() {
    // Set up the user that should already exist in the database
    Email existingEmail = new Email("existing@test.com", true, "token");
    User existingUser = new User(existingEmail, "password", Collections.emptyMap());

    // Set up expected user object
    Email updatedEmail = new Email("existing@test.com", false, null);
    User updatedUser = new User(updatedEmail, "password", Collections.emptyMap());

    var captor = ArgumentCaptor.forClass(User.class);

    when(usersDao.findByEmail(existingEmail.getAddress()))
        .thenReturn(CompletableFuture.completedFuture(existingUser));
    when(usersDao.update(eq(null), captor.capture()))
        .thenReturn(CompletableFuture.completedFuture(updatedUser));

    Response response = resource.resetVerificationStatus(key, existingEmail.getAddress(),
        existingUser.getPassword());
    User result = (User) response.getEntity();

    assertAll("Assert successful verification status reset",
        () -> assertEquals(response.getStatusInfo(), Response.Status.OK),
        () -> assertEquals(updatedUser, captor.getValue()),
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
