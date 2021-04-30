package com.sanctionco.thunder.resources;

import com.codahale.metrics.annotation.Metered;
import com.sanctionco.thunder.ThunderException;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.email.EmailService;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.ResponseType;
import com.sanctionco.thunder.models.User;
import com.sanctionco.thunder.openapi.SwaggerAnnotations;
import com.sanctionco.thunder.validation.RequestValidationException;
import com.sanctionco.thunder.validation.RequestValidator;

import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;

import java.net.URI;
import java.security.Principal;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides API methods to send a verification email, verify a user's email address, and display
 * a success page. The methods contained in this class are available at the
 * {@code /verify} endpoint.
 */
@Path("/verify")
@Produces(MediaType.APPLICATION_JSON)
public class VerificationResource {
  private static final Logger LOG = LoggerFactory.getLogger(VerificationResource.class);

  private final UsersDao usersDao;
  private final RequestValidator requestValidator;
  private final EmailService emailService;

  /**
   * Constructs a new {@code VerificationResource} with the given users DAO, metrics, email service,
   * hash service, and message options.
   *
   * @param usersDao the DAO used to connect to the database
   * @param requestValidator the validator used to validate incoming requests
   * @param emailService the email service used to send verification emails
   */
  @Inject
  public VerificationResource(UsersDao usersDao,
                              RequestValidator requestValidator,
                              EmailService emailService) {
    this.usersDao = Objects.requireNonNull(usersDao);
    this.requestValidator = Objects.requireNonNull(requestValidator);
    this.emailService = Objects.requireNonNull(emailService);
  }

  /**
   * Sends an email message to the given email address. The email message will contain
   * a custom URL that can be called to verify the email address. This method will update the user
   * in the database to include the generated verification token.
   *
   * @param uriInfo the HTTP metadata of the incoming request
   * @param auth the auth principal required to access the resource
   * @param email the message recipient's email address
   * @param password the user's password
   * @return the HTTP response that indicates success or failure. If successful, the response will
   *     contain the updated user after generating the verification token.
   *
   * @see VerificationResource#verifyEmail(String, String, ResponseType)
   */
  @POST
  @Metered(name = "send-email-requests")
  @SwaggerAnnotations.Methods.Email
  public Response createVerificationEmail(
      @Context UriInfo uriInfo,
      @Parameter(hidden = true) @Auth Principal auth,
      @Parameter(hidden = true) @QueryParam("email") String email,
      @Parameter(hidden = true) @HeaderParam("password") String password) {
    try {
      requestValidator.validate(password, email, false);
    } catch (RequestValidationException e) {
      return e.response(email);
    }

    LOG.info("Attempting to send verification email to user {}", email);

    return usersDao.findByEmail(email)
        .thenApply(user -> {
          // Check that the supplied password is correct for the user's account
          requestValidator.verifyPasswordHeader(password, user.getPassword());

          // Generate the unique verification token
          String token = generateVerificationToken();

          // Update the user's verification token
          return new User(
              new Email(user.getEmail().getAddress(), false, token),
              user.getPassword(),
              user.getProperties());
        })
        .thenCompose(user -> usersDao.update(user.getEmail().getAddress(), user))
        .thenCompose(result -> {
          // Build the verification URL
          String verificationUrl = uriInfo.getBaseUriBuilder().path("/verify")
              .queryParam("email", result.getEmail().getAddress())
              .queryParam("token", result.getEmail().getVerificationToken())
              .queryParam("response_type", "html")
              .build().toString();

          // Send the email to the user's email address
          return emailService.sendVerificationEmail(result.getEmail(), verificationUrl)
              .thenApply(success -> {
                if (!success) {
                  LOG.error("Error sending email to address {}", result.getEmail().getAddress());
                  throw new ThunderException("An error occurred while attempting to send email.");
                }

                return result;
              });
        })
        .thenApply(result -> {
          LOG.info("Successfully sent verification email to user {}.", email);
          return Response.ok(result).build();
        })
        .exceptionally(throwable -> handleFutureException(
            "Error sending verification email to user {}. Caused by: {}", email, throwable))
        .join();
  }

  /**
   * Verifies the given email, marking it as verified in the database if the token matches the
   * stored verification token. Depending on the given response type, the method will either return
   * a response that contains the updated verified user or will redirect to an HTML success page.
   *
   * @param email the email to verify
   * @param token the verification token associated with the email
   * @param responseType the type of object to include in the HTTP response. Either JSON or HTML.
   * @return the HTTP response that indicates success or failure. If successful and the response
   *     type is JSON, the response will contain the updated user after marking the email as
   *     verified. If the response type is HTML, the response will redirect to the success page.
   *
   * @see VerificationResource#createVerificationEmail(UriInfo, Principal, String, String)
   * @see VerificationResource#getSuccessHtml()
   */
  @GET
  @Metered(name = "verify-email-requests")
  @SwaggerAnnotations.Methods.Verify
  public Response verifyEmail(@Parameter(hidden = true) @QueryParam("email") String email,
                              @Parameter(hidden = true) @QueryParam("token") String token,
                              @Parameter(hidden = true) @QueryParam("response_type")
                                @DefaultValue("json") ResponseType responseType) {
    try {
      requestValidator.validate(token, email, true);
    } catch (RequestValidationException e) {
      return e.response(email);
    }

    LOG.info("Attempting to verify email {}", email);

    return usersDao.findByEmail(email)
        .thenApply(user -> {
          String verificationToken = user.getEmail().getVerificationToken();
          if (verificationToken == null || verificationToken.isEmpty()) {
            LOG.warn("Tried to read null or empty verification token");
            throw RequestValidationException
                .tokenNotSet("Bad value found for user verification token.");
          }

          if (!token.equals(verificationToken)) {
            LOG.warn("User provided verification token does not match DB verification token.");
            throw RequestValidationException.incorrectToken("Incorrect verification token.");
          }

          // Create the verified user
          return new User(
              new Email(user.getEmail().getAddress(), true, user.getEmail().getVerificationToken()),
              user.getPassword(),
              user.getProperties());
        })
        .thenCompose(updatedUser -> usersDao.update(email, updatedUser))
        .thenApply(result -> {
          LOG.info("Successfully verified email {}.", email);
          if (responseType.equals(ResponseType.JSON)) {
            LOG.info("Returning JSON in the response.");
            return Response.ok(result).build();
          }

          LOG.info("Redirecting to /verify/success in order to return HTML.");
          URI uri = UriBuilder.fromUri("/verify/success").build();
          return Response.seeOther(uri).build();
        })
        .exceptionally(throwable -> handleFutureException(
            "Error verifying email {} in database. Caused by: {}", email, throwable))
        .join();
  }

  /**
   * Resets the verification status of the user with the given email and password.
   *
   * @param auth the auth principal required to access the resource
   * @param email the user's email address
   * @param password the user's password
   * @return the HTTP response that indicates success or failure. If successful, the response will
   *     contain the updated user after resetting the verification information.
   */
  @POST
  @Path("/reset")
  @Metered(name = "reset-verification-requests")
  @SwaggerAnnotations.Methods.Reset
  public Response resetVerificationStatus(
      @Parameter(hidden = true) @Auth Principal auth,
      @Parameter(hidden = true) @QueryParam("email") String email,
      @Parameter(hidden = true) @HeaderParam("password") String password) {
    try {
      requestValidator.validate(password, email, false);
    } catch (RequestValidationException e) {
      return e.response(email);
    }

    LOG.info("Attempting to reset verification status for user {}", email);

    return usersDao.findByEmail(email)
        .thenApply(user -> {
          // Check that the supplied password is correct for the user's account
          requestValidator.verifyPasswordHeader(password, user.getPassword());

          return new User(
              new Email(user.getEmail().getAddress(), false, null),
              user.getPassword(),
              user.getProperties());
        })
        .thenCompose(user -> usersDao.update(null, user))
        .thenApply(result -> {
          LOG.info("Successfully reset verification status for user {}.", email);
          return Response.ok(result).build();
        })
        .exceptionally(throwable -> handleFutureException(
            "Error posting user {} to the database. Caused by {}", email, throwable))
        .join();
  }

  /**
   * Returns HTML to display as a success page after user verification.
   *
   * @return the HTTP response containing HTML
   */
  @GET
  @Path("/success")
  @Produces(MediaType.TEXT_HTML)
  @SwaggerAnnotations.Methods.Success
  public Response getSuccessHtml() {
    return Response.ok(emailService.getSuccessHtml()).build();
  }

  /**
   * Generates a random unique token.
   *
   * @return the token
   */
  private String generateVerificationToken() {
    return UUID.randomUUID().toString();
  }

  private Response handleFutureException(String logMessage, String email, Throwable throwable) {
    var cause = (ThunderException) throwable.getCause();

    LOG.error(logMessage, email, cause.getMessage());
    return cause.response(email);
  }
}
