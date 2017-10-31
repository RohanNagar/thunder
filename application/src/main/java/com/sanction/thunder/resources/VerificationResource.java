package com.sanction.thunder.resources;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.sanction.thunder.authentication.Key;
import com.sanction.thunder.dao.DatabaseException;
import com.sanction.thunder.dao.PilotUsersDao;
import com.sanction.thunder.models.Email;
import com.sanction.thunder.models.PilotUser;

import io.dropwizard.auth.Auth;

import javax.inject.Inject;

import javax.ws.rs.GET;
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

  // Counts number of requests
  private final Meter postRequests;

  /**
   * Constructs a new VerificationResource to allow verification of a user.
   *
   * @param usersDao The DAO to connect to the database with.
   * @param metrics The metrics object to set up meters with.
   */
  @Inject
  public VerificationResource(PilotUsersDao usersDao, MetricRegistry metrics) {
    this.usersDao = usersDao;

    // Set up metrics
    this.postRequests = metrics.meter(MetricRegistry.name(
        UserResource.class,
        "post-requests"));
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
    postRequests.mark();

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

    LOG.info("Attempting to verify user {}", email);

    PilotUser user;
    try {
      user = usersDao.findByEmail(email);
    } catch (DatabaseException e) {
      LOG.error("Error retrieving user {} in database. Caused by: {}", email, e.getErrorKind());
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
      LOG.error("Error verifying user {} in database. Caused by: {}", email, e.getErrorKind());
      return e.getErrorKind().buildResponse(email);
    }

    LOG.info("Successfully verified user {}.", email);
    return Response.ok(updatedUser).build();
  }
}
