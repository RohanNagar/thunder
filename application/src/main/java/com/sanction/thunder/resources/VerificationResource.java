package com.sanction.thunder.resources;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.sanction.thunder.authentication.Key;
import com.sanction.thunder.dao.DatabaseException;
import com.sanction.thunder.dao.PilotUsersDao;
import com.sanction.thunder.email.EmailService;
import com.sanction.thunder.models.Email;
import com.sanction.thunder.models.PilotUser;

import io.dropwizard.auth.Auth;

import java.util.StringJoiner;
import java.util.UUID;

import javax.inject.Inject;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/verify")
@Produces(MediaType.APPLICATION_JSON)
public class VerificationResource {
  private static final Logger LOG = LoggerFactory.getLogger(VerificationResource.class);

  private final PilotUsersDao usersDao;
  private final EmailService emailService;

  // Counts number of requests
  private final Meter getRequests;

  /**
   * Constructs a new VerificationResource to allow verification of a user.
   *
   * @param usersDao The DAO to connect to the database with.
   * @param metrics The metrics object to set up meters with.
   */
  @Inject
  public VerificationResource(PilotUsersDao usersDao,
                              MetricRegistry metrics,
                              EmailService emailService) {
    this.usersDao = usersDao;
    this.emailService = emailService;

    // Set up metrics
    this.getRequests = metrics.meter(MetricRegistry.name(
        UserResource.class,
        "get-requests"));
  }

  /**
   * Validates a user account by sending an email with a unique token.
   *
   * @param key The basic authentication key necessary to access the resource.
   * @param email The email to send a unique token to.
   * @return A response status and message.
   */
  @POST
  public Response verifyUser(@Auth Key key,
                             @QueryParam("email") String email,
                             @HeaderParam("password") String password) {
    getRequests.mark();

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

    LOG.info("Attempting to verify user {}", email);

    // Get the existing PilotUser
    PilotUser user;
    try {
      user = usersDao.findByEmail(email);
    } catch (DatabaseException e) {
      LOG.error("Error retrieving user {} in database. Caused by: {}", email, e.getErrorKind());
      return e.getErrorKind().buildResponse(email);
    }

    // Generate the unique verification token
    String token = generateVerificationToken();

    // Update the user's verification token
    PilotUser updatedUser = new PilotUser(
        new Email(user.getEmail().getAddress(), false, token),
        user.getPassword(),
        user.getFacebookAccessToken(),
        user.getTwitterAccessToken(),
        user.getTwitterAccessSecret()
    );

    PilotUser result;
    try {
      result = usersDao.update(user.getEmail().getAddress(), updatedUser);
    } catch (DatabaseException e) {
      LOG.error("Error posting user {} to the database. Caused by {}",
          user.getEmail(), e.getErrorKind());
      return e.getErrorKind().buildResponse(user.getEmail().getAddress());
    }

    // Send the token URL to the users email
    boolean emailResult = emailService.sendEmail(result.getEmail(),
        "Account Verification",
        new StringJoiner("\n")
        .add("<h1> Welcome to Pilot! </h1>")
        .add("<p> Click the below link to verify your account. </p>")
        .add(String.format("<a href=\"thunder.sanctionco.com/verify?email=%s&token=%s\">"
            + "Click here to verify your account!</a>",
            result.getEmail().getAddress(),
            token))
        .toString(),
        new StringJoiner("\n")
        .add("Visit the below address to verify your account.")
        .add(String.format("thunder.sanctionco.com/verify?email=%s&token=%s",
                result.getEmail().getAddress(),
                token))
        .toString());

    if (!emailResult) {
      LOG.error("Error sending email to address {}", result.getEmail().getAddress());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    LOG.info("Successfully verified user {}.", email);
    return Response.ok(result).build();
  }

  /**
   * Verifies the provided email, setting it as valid in the database.
   *
   * @param key The basic authentication key necessary to access the resource.
   * @param email The email to verify in the database.
   * @param token The verification token associated with the user.
   * @return A response status and message.
   */
  @GET
  public Response verifyEmail(@Auth Key key,
                              @QueryParam("email") String email,
                              @QueryParam("token") String token) {
    getRequests.mark();

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

    PilotUser user;
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

    // Create the verified pilot user
    PilotUser updatedUser = new PilotUser(
        new Email(user.getEmail().getAddress(), true, user.getEmail().getVerificationToken()),
        user.getPassword(),
        user.getFacebookAccessToken(),
        user.getTwitterAccessToken(),
        user.getFacebookAccessToken()
    );

    try {
      usersDao.update(user.getEmail().getAddress(), updatedUser);
    } catch (DatabaseException e) {
      LOG.error("Error verifying email {} in database. Caused by: {}", email, e.getErrorKind());
      return e.getErrorKind().buildResponse(email);
    }

    LOG.info("Successfully verified email {}.", email);
    return Response.ok(updatedUser).build();
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
