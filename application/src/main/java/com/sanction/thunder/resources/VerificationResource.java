package com.sanction.thunder.resources;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import com.sanction.thunder.authentication.Key;
import com.sanction.thunder.dao.DatabaseException;
import com.sanction.thunder.dao.UsersDao;
import com.sanction.thunder.email.EmailService;
import com.sanction.thunder.models.Email;
import com.sanction.thunder.models.ResponseType;
import com.sanction.thunder.models.User;

import com.sanction.thunder.util.EmailUtilities;
import io.dropwizard.auth.Auth;

import java.net.URI;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/verify")
@Produces(MediaType.APPLICATION_JSON)
public class VerificationResource {
  private static final Logger LOG = LoggerFactory.getLogger(VerificationResource.class);

  private final UsersDao usersDao;
  private final EmailService emailService;

  private final String baseUrl;
  private final String successHtml;
  private final String verificationHtml;
  private final String verificationText;

  // Counts number of requests
  private final Meter verifyUserRequests;
  private final Meter verifyEmailRequests;

  /**
   * Constructs a new VerificationResource to allow verification of a user.
   *
   * @param usersDao The DAO to connect to the database with.
   * @param metrics The metrics object to set up meters with.
   */
  @Inject
  public VerificationResource(UsersDao usersDao,
                              MetricRegistry metrics,
                              EmailService emailService,
                              @Named("baseUrl") String baseUrl,
                              @Named("successHtml") String successHtml,
                              @Named("verificationHtml") String verificationHtml,
                              @Named("verificationText") String verificationText) {
    this.usersDao = usersDao;
    this.emailService = emailService;

    this.baseUrl = baseUrl;
    this.successHtml = successHtml;
    this.verificationHtml = verificationHtml;
    this.verificationText = verificationText;

    // Set up metrics
    this.verifyUserRequests = metrics.meter(MetricRegistry.name(
        VerificationResource.class,
        "verify-user-requests"));
    this.verifyEmailRequests = metrics.meter(MetricRegistry.name(
        VerificationResource.class,
        "verify-email-requests"));
  }

  /**
   * Validates a user account by sending an email with a unique token.
   *
   * @param key The basic authentication key necessary to access the resource.
   * @param email The email to send a unique token to.
   * @return A response status and message.
   */
  @POST
  public Response createVerificationEmail(@Auth Key key,
                                          @QueryParam("email") String email,
                                          @HeaderParam("password") String password) {
    verifyUserRequests.mark();

    if (email == null || email.isEmpty()) {
      LOG.warn("Attempted user verification without an email.");
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Incorrect or missing email query parameter.").build();
    }

    if (password == null || password.isEmpty()) {
      LOG.warn("Attempted to verify user {} without a password.", email);
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Incorrect or missing header credentials.").build();
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

    // Generate the unique verification token
    String token = generateVerificationToken();

    // Update the user's verification token
    User updatedUser = new User(
        new Email(user.getEmail().getAddress(), false, token),
        user.getPassword(),
        user.getProperties()
    );

    User result;
    try {
      result = usersDao.update(user.getEmail().getAddress(), updatedUser);
    } catch (DatabaseException e) {
      LOG.error("Error posting user {} to the database. Caused by {}",
          user.getEmail(), e.getErrorKind());
      return e.getErrorKind().buildResponse(user.getEmail().getAddress());
    }

    // Send the verification URL to the users email
    String verificationUrl = EmailUtilities
        .buildVerificationUrl(baseUrl, result.getEmail().getAddress(), token);

    boolean emailResult = emailService.sendEmail(result.getEmail(),
        "Account Verification",
        EmailUtilities.replaceUrlPlaceholder(verificationHtml, verificationUrl),
        EmailUtilities.replaceUrlPlaceholder(verificationText, verificationUrl));

    if (!emailResult) {
      LOG.error("Error sending email to address {}", result.getEmail().getAddress());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("An error occurred while attempting to send an email.").build();
    }

    LOG.info("Successfully sent verification email to user {}.", email);
    return Response.ok(result).build();
  }

  /**
   * Verifies the provided email, setting it as valid in the database.
   *
   * @param email The email to verify in the database.
   * @param token The verification token associated with the user.
   * @param responseType The type of object to respond with. Either JSON or HTML.
   * @return A response status and message.
   */
  @GET
  public Response verifyEmail(@QueryParam("email") String email,
                              @QueryParam("token") String token,
                              @QueryParam("response_type") @DefaultValue("json")
                                  ResponseType responseType) {
    verifyEmailRequests.mark();

    if (email == null || email.isEmpty()) {
      LOG.warn("Attempted email verification without an email.");
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Incorrect or missing email query parameter.").build();
    }

    if (token == null || token.isEmpty()) {
      LOG.warn("Attempted email verification without a token");
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Incorrect or missing verification token query parameter.").build();
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

    try {
      usersDao.update(user.getEmail().getAddress(), updatedUser);
    } catch (DatabaseException e) {
      LOG.error("Error verifying email {} in database. Caused by: {}", email, e.getErrorKind());
      return e.getErrorKind().buildResponse(email);
    }

    LOG.info("Successfully verified email {}.", email);
    if (responseType.equals(ResponseType.JSON)) {
      LOG.info("Returning JSON in the response.");
      return Response.ok(updatedUser).build();
    }

    LOG.info("Redirecting to /verify/success in order to return HTML.");
    URI uri = UriBuilder.fromUri("/verify/success").build();
    return Response.seeOther(uri).build();
  }

  /**
   * Returns HTML to display as a success page after user verification.
   *
   * @return A Response containing the HTML to display to the user.
   */
  @GET
  @Path("/success")
  @Produces(MediaType.TEXT_HTML)
  public Response getSuccessHtml() {
    return Response.ok(successHtml).build();
  }

  /**
   * Generates a random unique token for verifying a users email.
   *
   * @return Random alpha numeric token string.
   */
  private String generateVerificationToken() {
    return UUID.randomUUID().toString();
  }
}
