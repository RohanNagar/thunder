package com.sanction.thunder.resources;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.sanction.thunder.authentication.Key;
import com.sanction.thunder.dao.DatabaseError;
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
  private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);

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

    PilotUser user;
    try {
      user = usersDao.findByEmail(email);
    } catch (DatabaseException e) {
      LOG.error("Error retrieving user {} in database. Caused by: {}", email, e.getErrorKind());
      return buildResponseForDatabaseError(e.getErrorKind(), email);
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
        new Email(user.getEmail().getAddress(), true, ""),
        user.getPassword(),
        user.getFacebookAccessToken(),
        user.getTwitterAccessToken(),
        user.getFacebookAccessToken()
    );

    try {
      usersDao.update(user.getEmail().getAddress(), updatedUser);
    } catch (DatabaseException e) {
      LOG.error("Error verifying user {} in database. Caused by: {}", email, e.getErrorKind());
      return buildResponseForDatabaseError(e.getErrorKind(), email);
    }

    LOG.info("Successfully verified user {}.", email);
    return Response.ok("User successfully verified!").build();
  }

  /**
   * Returns the appropriate Response object for a given DatabaseError.
   *
   * @param error The DatabaseError that occurred.
   * @param email The email of the user that this error is related to.
   * @return An appropriate Response object to return to the caller.
   */
  private Response buildResponseForDatabaseError(DatabaseError error, String email) {
    switch (error) {
      case CONFLICT:
        return Response.status(Response.Status.CONFLICT)
            .entity(String.format("User %s already exists in DB.", email)).build();
      case USER_NOT_FOUND:
        return Response.status(Response.Status.NOT_FOUND)
            .entity(String.format("User %s not found in DB.", email)).build();
      case REQUEST_REJECTED:
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity("The database rejected the request. Check your data and try again.").build();
      case DATABASE_DOWN:
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
            .entity("Database is currently unavailable. Please try again later.").build();
      default:
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity("An unknown error occurred.").build();
    }
  }
}
