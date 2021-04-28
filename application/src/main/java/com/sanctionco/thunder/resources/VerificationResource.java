package com.sanctionco.thunder.resources;

import com.codahale.metrics.annotation.Metered;
import com.sanctionco.thunder.crypto.HashService;
import com.sanctionco.thunder.dao.DatabaseException;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.email.EmailService;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.ResponseType;
import com.sanctionco.thunder.models.User;
import com.sanctionco.thunder.validation.RequestValidator;

import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.net.URI;
import java.security.Principal;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;
import javax.validation.ValidationException;
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
  private final HashService hashService;

  /**
   * Constructs a new {@code VerificationResource} with the given users DAO, metrics, email service,
   * hash service, and message options.
   *
   * @param usersDao the DAO used to connect to the database
   * @param requestValidator the validator used to validate incoming requests
   * @param emailService the email service used to send verification emails
   * @param hashService the service used to verify passwords in incoming requests
   */
  @Inject
  public VerificationResource(UsersDao usersDao,
                              RequestValidator requestValidator,
                              EmailService emailService,
                              HashService hashService) {
    this.usersDao = Objects.requireNonNull(usersDao);
    this.requestValidator = Objects.requireNonNull(requestValidator);
    this.emailService = Objects.requireNonNull(emailService);
    this.hashService = Objects.requireNonNull(hashService);
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
  @Operation(
      summary = "Send a verification email to the specified email address",
      description = "Initiates the user verification process by sending a verification email "
          + "to the email address provided as a query parameter. The user in the database will be "
          + "updated to include a unique verification token that is sent along with the email.",
      tags = { "verify" },
      responses = {
          @ApiResponse(responseCode = "200",
              description = "The operation was successfully completed",
              content = @Content(
                  mediaType = "application/json", schema = @Schema(implementation = User.class))),
          @ApiResponse(responseCode = "400",
              description = "The send email request was malformed"),
          @ApiResponse(responseCode = "401",
              description = "The request was unauthorized"),
          @ApiResponse(responseCode = "404",
              description = "The user with the given email address was not found in the database"),
          @ApiResponse(responseCode = "500",
              description = "The database rejected the request for an unknown reason"),
          @ApiResponse(responseCode = "503",
              description = "The database is currently unavailable")
      })
  @Metered(name = "send-email-requests")
  public Response createVerificationEmail(
      @Context UriInfo uriInfo,
      @Parameter(hidden = true) @Auth Principal auth,
      @Parameter(description = "The email address of the user to send the email.", required = true)
          @QueryParam("email") String email,
      @Parameter(description = "The password of the user, required if "
          + "headerPasswordCheck is enabled.") @HeaderParam("password") String password) {

    try {
      requestValidator.validate(password, email, false);
    } catch (ValidationException e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(e.getMessage()).build();
    }

    LOG.info("Attempting to send verification email to user {}", email);

    // Get the existing User
    User user;
    try {
      user = usersDao.findByEmail(email);
    } catch (DatabaseException e) {
      LOG.error("Error retrieving user {} in database. Caused by: {}", email, e.getErrorKind());
      return e.getErrorKind().buildResponse(email);
    }

    // Check that the supplied password is correct for the user's account
    if (requestValidator.isPasswordHeaderCheckEnabled()
        && !hashService.isMatch(password, user.getPassword())) {
      LOG.warn("Incorrect password parameter for user {} in database.", user.getEmail());
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity("Unable to validate user with provided credentials.").build();
    }

    // Generate the unique verification token
    String token = generateVerificationToken();

    // Update the user's verification token
    User updatedUser = new User(
        new Email(user.getEmail().getAddress(), false, token),
        user.getPassword(),
        user.getProperties());

    return usersDao.update(user.getEmail().getAddress(), updatedUser)
        .thenApply(result -> {
          // Build the verification URL
          String verificationUrl = uriInfo.getBaseUriBuilder().path("/verify")
              .queryParam("email", result.getEmail().getAddress())
              .queryParam("token", token)
              .queryParam("response_type", "html")
              .build().toString();

          // Send the email to the user's email address
          boolean emailResult = emailService.sendVerificationEmail(
              result.getEmail(), verificationUrl);

          if (!emailResult) {
            LOG.error("Error sending email to address {}", result.getEmail().getAddress());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("An error occurred while attempting to send an email.").build();
          }

          LOG.info("Successfully sent verification email to user {}.", email);
          return Response.ok(result).build();
        })
        .exceptionally(throwable -> {
          var cause = (DatabaseException) throwable.getCause();

          LOG.error("Error posting user {} to the database. Caused by {}",
              user.getEmail(), cause.getErrorKind());
          return cause.getErrorKind().buildResponse(user.getEmail().getAddress());
        }).join();
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
  @Operation(
      summary = "Verify a user email address",
      description = "Used to verify a user email. Typically, the user will click on this link in "
          + "their email to verify their account. Upon verification, the user object in the "
          + "database will be updated to indicate that the email address is verified.",
      tags = { "verify" },
      responses = {
          @ApiResponse(responseCode = "200",
              description = "The operation was successfully completed",
              content = @Content(
                  mediaType = "application/json", schema = @Schema(implementation = User.class))),
          @ApiResponse(responseCode = "303",
              description = "The request should be redirected to /verify/success"),
          @ApiResponse(responseCode = "400",
              description = "The verify request was malformed"),
          @ApiResponse(responseCode = "404",
              description = "The user was not found in the database"),
          @ApiResponse(responseCode = "500",
              description = "The database rejected the request for an unknown reason"),
          @ApiResponse(responseCode = "503",
              description = "The database is currently unavailable")
      })
  @Metered(name = "verify-email-requests")
  public Response verifyEmail(
      @Parameter(description = "The email address of the user to verify.", required = true)
          @QueryParam("email") String email,
      @Parameter(description = "The verification token that matches the one sent via email.",
          required = true) @QueryParam("token") String token,
      @Parameter(description = "The optional response type, either HTML or JSON. If HTML is "
          + "specified, the URL will redirect to /verify/success.") @QueryParam("response_type")
          @DefaultValue("json") ResponseType responseType) {

    try {
      requestValidator.validate(token, email, true);
    } catch (ValidationException e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(e.getMessage()).build();
    }

    LOG.info("Attempting to verify email {}", email);

    User user;
    try {
      user = usersDao.findByEmail(email);
    } catch (DatabaseException e) {
      LOG.error("Error retrieving email {} in database. Caused by: {}", email, e.getErrorKind());
      return e.getErrorKind().buildResponse(email);
    }

    String verificationToken = user.getEmail().getVerificationToken();
    if (verificationToken == null || verificationToken.isEmpty()) {
      LOG.warn("Tried to read null or empty verification token");
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("Bad value found for user verification token.").build();
    }

    if (!token.equals(verificationToken)) {
      LOG.warn("User provided verification token does not match database verification token.");
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Incorrect verification token.").build();
    }

    // Create the verified user
    User updatedUser = new User(
        new Email(user.getEmail().getAddress(), true, user.getEmail().getVerificationToken()),
        user.getPassword(),
        user.getProperties()
    );

    return usersDao.update(user.getEmail().getAddress(), updatedUser)
        .thenApply(result -> {
          LOG.info("Successfully verified email {}.", email);
          if (responseType.equals(ResponseType.JSON)) {
            LOG.info("Returning JSON in the response.");
            return Response.ok(updatedUser).build();
          }

          LOG.info("Redirecting to /verify/success in order to return HTML.");
          URI uri = UriBuilder.fromUri("/verify/success").build();
          return Response.seeOther(uri).build();
        }).exceptionally(throwable -> {
          var cause = (DatabaseException) throwable.getCause();

          LOG.error("Error verifying email {} in database. Caused by: {}",
              email, cause.getErrorKind());
          return cause.getErrorKind().buildResponse(email);
        }).join();
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
  @Operation(
      summary = "Reset a user's email verification status",
      description = "Resets the verification status of a user's email address to unverified.",
      tags = { "verify" },
      responses = {
          @ApiResponse(responseCode = "200",
              description = "The user's verification status was successfully reset",
              content = @Content(
                  mediaType = "application/json", schema = @Schema(implementation = User.class))),
          @ApiResponse(responseCode = "400",
              description = "The verify request was malformed"),
          @ApiResponse(responseCode = "401",
              description = "The request was unauthorized"),
          @ApiResponse(responseCode = "404",
              description = "The user was not found in the database"),
          @ApiResponse(responseCode = "500",
              description = "The database rejected the request for an unknown reason"),
          @ApiResponse(responseCode = "503",
              description = "The database is currently unavailable")
      })
  @Metered(name = "reset-verification-requests")
  public Response resetVerificationStatus(
      @Parameter(hidden = true) @Auth Principal auth,
      @Parameter(description = "The email address of the user to reset.", required = true)
          @QueryParam("email") String email,
      @Parameter(description = "The password of the user, required if "
          + "headerPasswordCheck is enabled.") @HeaderParam("password") String password) {

    try {
      requestValidator.validate(password, email, false);
    } catch (ValidationException e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(e.getMessage()).build();
    }

    LOG.info("Attempting to reset verification status for user {}", email);

    // Get the existing User
    User user;
    try {
      user = usersDao.findByEmail(email);
    } catch (DatabaseException e) {
      LOG.error("Error retrieving user {} in database. Caused by: {}", email, e.getErrorKind());
      return e.getErrorKind().buildResponse(email);
    }

    // Check that the supplied password is correct for the user's account
    if (requestValidator.isPasswordHeaderCheckEnabled()
        && !hashService.isMatch(password, user.getPassword())) {
      LOG.warn("Incorrect password parameter for user {} in database.", user.getEmail());
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity("Unable to validate user with provided credentials.").build();
    }

    // Reset the user's verification token
    User updatedUser = new User(
        new Email(user.getEmail().getAddress(), false, null),
        user.getPassword(),
        user.getProperties());

    return usersDao.update(null, updatedUser)
        .thenApply(result -> {
          LOG.info("Successfully reset verification status for user {}.", email);
          return Response.ok(result).build();
        }).exceptionally(throwable -> {
          var cause = (DatabaseException) throwable.getCause();

          LOG.error("Error posting user {} to the database. Caused by {}",
              user.getEmail(), cause.getErrorKind());
          return cause.getErrorKind().buildResponse(user.getEmail().getAddress());
        }).join();
  }

  /**
   * Returns HTML to display as a success page after user verification.
   *
   * @return the HTTP response containing HTML
   */
  @GET
  @Path("/success")
  @Produces(MediaType.TEXT_HTML)
  @Operation(
      summary = "Get success HTML",
      description = """
          Returns an HTML success page that is shown after a user successfully
          verifies their account. GET /verify will redirect to this URL if the response_type
          query parameter is set to html.""",
      tags = { "verify" },
      responses = {
          @ApiResponse(responseCode = "200",
              description = "The operation was successful",
              content = @Content(
                  mediaType = "text/html", schema = @Schema(example = """
                  <!DOCTYPE html>
                  <html>
                    <div class="alert alert-success">
                      <div align="center"><strong>Success!</strong><br>Your account has been \
                      verified.</div>
                    </div>
                    <link rel="stylesheet" \
                    href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/css/bootstrap.min.css"/>
                  </html>""")))
      })
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
}
