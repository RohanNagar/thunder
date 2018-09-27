package com.sanctionco.thunder.resources;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import com.sanctionco.thunder.authentication.Key;
import com.sanctionco.thunder.crypto.HashService;
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
 * Provides API methods to send a verification email, verify a user's email address, and display
 * a success page. The methods contained in this class are available at the
 * {@code /verify} endpoint.
 */
@Path("/verify")
@Produces(MediaType.APPLICATION_JSON)
public class VerificationResource {
  private static final Logger LOG = LoggerFactory.getLogger(VerificationResource.class);

  private final UsersDao usersDao;
  private final EmailService emailService;
  private final MessageOptions messageOptions;
  private final HashService hashService;

  // Counts number of requests
  private final Meter sendEmailRequests;
  private final Meter verifyEmailRequests;
  private final Meter resetVerificationRequests;

  /**
   * Constructs a new {@code VerificationResource} with the given users DAO, metrics, email service,
   * hash service, and message options.
   *
   * @param usersDao the DAO used to connect to the database
   * @param metrics the metrics object used to set up meters
   * @param emailService the email service used to send verification emails
   * @param hashService the service used to verify passwords in incoming requests
   * @param messageOptions the options used to customize the verification email message
   */
  @Inject
  public VerificationResource(UsersDao usersDao,
                              MetricRegistry metrics,
                              EmailService emailService,
                              HashService hashService,
                              MessageOptions messageOptions) {
    this.usersDao = Objects.requireNonNull(usersDao);
    this.emailService = Objects.requireNonNull(emailService);
    this.hashService = Objects.requireNonNull(hashService);
    this.messageOptions = Objects.requireNonNull(messageOptions);

    // Set up metrics
    this.sendEmailRequests = metrics.meter(MetricRegistry.name(
        VerificationResource.class,
        "send-email-requests"));
    this.verifyEmailRequests = metrics.meter(MetricRegistry.name(
        VerificationResource.class,
        "verify-email-requests"));
    this.resetVerificationRequests = metrics.meter(MetricRegistry.name(
        VerificationResource.class,
        "reset-verification-requests"));
  }

  /**
   * Sends an email message to the given email address. The email message will contain
   * a custom URL that can be called to verify the email address. This method will update the user
   * in the database to include the generated verification token.
   *
   * @param uriInfo the HTTP metadata of the incoming request
   * @param key the basic authentication key required to access the resource
   * @param email the message recipient's email address
   * @param password the user's password
   * @return the HTTP response that indicates success or failure. If successful, the response will
   *     contain the updated user after generating the verification token.
   *
   * @see VerificationResource#verifyEmail(String, String, ResponseType)
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
    if (!hashService.isMatch(password, user.getPassword())) {
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
   * @see VerificationResource#createVerificationEmail(UriInfo, Key, String, String)
   * @see VerificationResource#getSuccessHtml()
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

  @POST
  @Path("/reset")
  public Response resetVerificationStatus(@Auth Key key,
                                          @QueryParam("email") String email,
                                          @HeaderParam("password") String password) {
    resetVerificationRequests.mark();

    if (email == null || email.isEmpty()) {
      LOG.warn("Attempted to reset user verification without an email.");
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Incorrect or missing email query parameter.").build();
    }

    if (password == null || password.isEmpty()) {
      LOG.warn("Attempted to reset user {} verification without a password.", email);
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Incorrect or missing header credentials.").build();
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
    if (!hashService.isMatch(password, user.getPassword())) {
      LOG.warn("Incorrect password parameter for user {} in database.", user.getEmail());
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity("Incorrect or missing header credentials.").build();
    }

    // Reset the user's verification token
    User updatedUser = new User(
        new Email(user.getEmail().getAddress(), false, null),
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

    LOG.info("Successfully reset verification status for user {}.", email);
    return Response.ok(result).build();
  }

  /**
   * Returns HTML to display as a success page after user verification.
   *
   * @return the HTTP response containing HTML
   */
  @GET
  @Path("/success")
  @Produces(MediaType.TEXT_HTML)
  public Response getSuccessHtml() {
    return Response.ok(messageOptions.getSuccessHtml()).build();
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
