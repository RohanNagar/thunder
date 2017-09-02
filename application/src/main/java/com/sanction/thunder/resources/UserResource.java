package com.sanction.thunder.resources;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.sanction.thunder.authentication.Key;
import com.sanction.thunder.dao.DatabaseError;
import com.sanction.thunder.dao.DatabaseException;
import com.sanction.thunder.dao.PilotUsersDao;
import com.sanction.thunder.models.PilotUser;

import io.dropwizard.auth.Auth;

import javax.inject.Inject;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
  private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);

  private final PilotUsersDao usersDao;

  // Counts number of requests
  private final Meter postRequests;
  private final Meter updateRequests;
  private final Meter getRequests;
  private final Meter deleteRequests;

  /**
   * Constructs a new UserResource to allow access to the user DB.
   *
   * @param usersDao The DAO to connect to the database with.
   * @param metrics The metrics object to set up meters with.
   */
  @Inject
  public UserResource(PilotUsersDao usersDao, MetricRegistry metrics) {
    this.usersDao = usersDao;

    // Set up metrics
    this.postRequests = metrics.meter(MetricRegistry.name(
        UserResource.class,
        "post-requests"));
    this.updateRequests = metrics.meter(MetricRegistry.name(
        UserResource.class,
        "update-requests"));
    this.getRequests = metrics.meter(MetricRegistry.name(
        UserResource.class,
        "get-requests"));
    this.deleteRequests = metrics.meter(MetricRegistry.name(
        UserResource.class,
        "delete-requests"));
  }

  /**
   * Posts a new PilotUser to the database.
   *
   * @param key The basic authentication key necessary to access the resource.
   * @param user The user to post to the database.
   * @return The user that was created in the database.
   */
  @POST
  public Response postUser(@Auth Key key, PilotUser user) {
    postRequests.mark();

    if (user == null) {
      LOG.warn("Attempted to post a null user.");
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Cannot post a null user.").build();
    }

    LOG.info("Attempting to create new user {}.", user.getEmail());

    PilotUser result;
    try {
      result = usersDao.insert(user);
    } catch (DatabaseException e) {
      LOG.error("Error posting user {} to the database. Caused by {}",
          user.getEmail(), e.getErrorKind());
      return buildResponseForDatabaseError(e.getErrorKind(), user.getEmail());
    }

    LOG.info("Successfully created new user {}.", user.getEmail());
    return Response.status(Response.Status.CREATED).entity(result).build();
  }

  /**
   * Updates a PilotUser in the database.
   *
   * @param key The basic authentication key necessary to access the resource.
   * @param password The password of the user to update. This should be the current password
   *                 before any updates are made. Used to verify the ability to edit the user.
   * @param user The PilotUser object with updated properties.
   * @return The pilotUser that was updated in the database.
   */
  @PUT
  public Response updateUser(@Auth Key key,
                             @HeaderParam("password") String password,
                             PilotUser user) {
    updateRequests.mark();

    if (user == null) {
      LOG.warn("Attempted to update a null user.");
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Cannot put a null user.").build();
    }

    if (password == null || password.equals("")) {
      LOG.warn("Attempted to update user {} without a password.", user.getEmail());
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Incorrect or missing header credentials.").build();
    }

    LOG.info("Attempting to update user {}.", user.getEmail());

    String email = user.getEmail();

    PilotUser foundUser;
    try {
      foundUser = usersDao.findByEmail(email);
    } catch (DatabaseException e) {
      LOG.error("Error retrieving user {} in database. Caused by: {}", email, e.getErrorKind());
      return buildResponseForDatabaseError(e.getErrorKind(), email);
    }

    // Check that the password is correct for the user to update
    if (!foundUser.getPassword().equals(password)) {
      LOG.error("The password for user {} was incorrect.", user.getEmail());
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity("Unable to validate user with provided credentials.").build();
    }

    PilotUser result;

    try {
      result = usersDao.update(user);
    } catch (DatabaseException e) {
      LOG.error("Error updating user {} in database. Caused by: {}", email, e.getErrorKind());
      return buildResponseForDatabaseError(e.getErrorKind(), email);
    }

    LOG.info("Successfully updated user {}.", user.getEmail());
    return Response.ok(result).build();
  }

  /**
   * Retrieves a PilotUser from the database.
   *
   * @param key The basic authentication key necessary to access the resource.
   * @param password The password of the user to fetch. Used to verify authentication.
   * @param email The email of the user to retrieve.
   * @return The pilotUser that was found in the database.
   */
  @GET
  public Response getUser(@Auth Key key,
                          @HeaderParam("password") String password,
                          @QueryParam("email") String email) {
    getRequests.mark();

    if (email == null || email.equals("")) {
      LOG.warn("Attempted to get a null user.");
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Incorrect or missing email query parameter.").build();
    }

    if (password == null || password.equals("")) {
      LOG.warn("Attempted to get user {} without a password", email);
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Incorrect or missing header credentials.").build();
    }

    LOG.info("Attempting to get user {}.", email);

    PilotUser user;
    try {
      user = usersDao.findByEmail(email);
    } catch (DatabaseException e) {
      LOG.error("Error retrieving user {} in database. Caused by: {}", email, e.getErrorKind());
      return buildResponseForDatabaseError(e.getErrorKind(), email);
    }

    // Check that the password is correct for the user that was requested
    if (!user.getPassword().equals(password)) {
      LOG.error("The password for user {} was incorrect.", email);
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity("Unable to validate user with provided credentials.").build();
    }

    LOG.info("Successfully retrieved user {}.", email);
    return Response.ok(user).build();
  }

  /**
   * Deletes a PilotUser from the database.
   *
   * @param key The basic authentication key necessary to access the resource.
   * @param password The password of the user to delete. Used to verify authentication.
   * @param email The email of the user to delete.
   * @return The user that was deleted from the database.
   */
  @DELETE
  public Response deleteUser(@Auth Key key,
                             @HeaderParam("password") String password,
                             @QueryParam("email") String email) {
    deleteRequests.mark();

    if (email == null || email.equals("")) {
      LOG.warn("Attempted to delete a null user.");
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Incorrect or missing email query parameter.").build();
    }

    if (password == null || password.equals("")) {
      LOG.warn("Attempted to delete user {} without a password.", email);
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Incorrect or missing header credentials.").build();
    }

    LOG.info("Attempting to delete user {}.", email);

    PilotUser user;
    try {
      user = usersDao.findByEmail(email);
    } catch (DatabaseException e) {
      LOG.error("Error retrieving user {} in database. Caused by: {}", email, e.getErrorKind());
      return buildResponseForDatabaseError(e.getErrorKind(), email);
    }

    // Check that password is correct before deleting
    if (!user.getPassword().equals(password)) {
      LOG.error("The password for user {} was incorrect.", email);
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity("Unable to validate user with provided credentials.").build();
    }

    PilotUser result;
    try {
      result = usersDao.delete(email);
    } catch (DatabaseException e) {
      LOG.error("Error deleting user {} in database. Caused by: {}", email, e.getErrorKind());
      return buildResponseForDatabaseError(e.getErrorKind(), email);
    }

    LOG.info("Successfully deleted user {}.", email);
    return Response.ok(result).build();
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
