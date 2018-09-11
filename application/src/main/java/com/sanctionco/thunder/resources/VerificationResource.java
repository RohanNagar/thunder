package com.sanctionco.thunder.resources;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import com.sanctionco.thunder.authentication.Key;
import com.sanctionco.thunder.crypto.password.PasswordVerifier;
import com.sanctionco.thunder.dao.DatabaseException;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.email.EmailService;
import com.sanctionco.thunder.email.MessageOptions;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.ResponseType;
import com.sanctionco.thunder.models.User;
import com.sanctionco.thunder.util.EmailUtilities;

import io.dropwizard.auth.Auth;

import java.net.URI;
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
 * Provides methods to verify user email addresses. The methods contained in this class are
 * available at the {@code /verify} endpoint.
 *
 * @see Email
 */
@Path("/verify")
@Produces(MediaType.APPLICATION_JSON)
public class VerificationResource {
  private static final Logger LOG = LoggerFactory.getLogger(VerificationResource.class);

  private final UsersDao usersDao;
  private final EmailService emailService;
  private final MessageOptions messageOptions;
  private final PasswordVerifier passwordVerifier;

  // Counts number of requests
  private final Meter sendEmailRequests;
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
                              PasswordVerifier passwordVerifier,
                              MessageOptions messageOptions) {
    this.usersDao = Objects.requireNonNull(usersDao);
    this.emailService = Objects.requireNonNull(emailService);
    this.passwordVerifier = Objects.requireNonNull(passwordVerifier);
    this.messageOptions = Objects.requireNonNull(messageOptions);

    // Set up metrics
    this.sendEmailRequests = metrics.meter(MetricRegistry.name(
        VerificationResource.class,
        "send-email-requests"));
    this.verifyEmailRequests = metrics.meter(MetricRegistry.name(
        VerificationResource.class,
        "verify-email-requests"));
  }

  /**
   * Validates a user account by sending an email with a unique token.
   *
   * @param key The basic authentication key necessary to access the resource.
   * @param email The email to send a unique token to.
   * @return The user that was sent an email with an updated verification token.
   */
  @POST
  public Response createVerificationEmail(@Context UriInfo uriInfo, @Auth Key key,
                                          @QueryParam("email") String email,
                                          @HeaderParam("password") String password) {
    sendEmailRequests.mark();

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

    // Check that the supplied password is correct for the user's account
    if (!passwordVerifier.isCorrectPassword(password, user.getPassword())) {
      LOG.warn("Incorrect password parameter for user {} in database.", user.getEmail());
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity("Incorrect or missing header credentials.").build();
    }

    // Generate the unique verification token
    String token = generateVerificationToken();

    // Update the user's verification token
    User updatedUser = new User(
        new Email(user.getEmail().getAddress(), false, token),
        user.getPassword(),
        user.getProperties());

    User result;
    try {
      result = usersDao.update(user.getEmail().getAddress(), updatedUser);
    } catch (DatabaseException e) {
      LOG.error("Error posting user {} to the database. Caused by {}",
          user.getEmail(), e.getErrorKind());
      return e.getErrorKind().buildResponse(user.getEmail().getAddress());
    }

    // Build the verification URL
    String verificationUrl = uriInfo.getBaseUriBuilder().path("/verify")
        .queryParam("email", result.getEmail().getAddress())
        .queryParam("token", token)
        .queryParam("response_type", "html")
        .build().toString();

    // Send the email to the user's email address
    boolean emailResult = emailService.sendEmail(result.getEmail(),
        messageOptions.getSubject(),
        EmailUtilities.replaceUrlPlaceholder(messageOptions.getBodyHtml(),
            messageOptions.getBodyHtmlUrlPlaceholder(), verificationUrl),
        EmailUtilities.replaceUrlPlaceholder(messageOptions.getBodyText(),
            messageOptions.getBodyTextUrlPlaceholder(), verificationUrl));

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
   * @return The user that was verified, or a redirect to an HTML success page.
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
    return Response.ok(messageOptions.getSuccessHtml()).build();
  }

  /**
   * Generates a random unique token for verifying a users email.
   *
   * @return A random alphanumeric token string.
   */
  private String generateVerificationToken() {
    return UUID.randomUUID().toString();
  }
}
