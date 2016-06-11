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
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Cannot post a null user.").build();
    }

    PilotUser result;
    try {
      result = usersDao.insert(user);
    } catch (DatabaseException e) {
      LOG.error("Error posting user {} to the database. Caused by {}",
          user.getUsername(), e.getErrorKind());
      return buildResponseForDatabaseError(e.getErrorKind(), user.getUsername());
    }

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
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Cannot put a null user.").build();
    }

    if (password == null || password.equals("")) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Incorrect or missing header credentials.").build();
    }

    String username = user.getUsername();

    PilotUser foundUser;
    try {
      foundUser = usersDao.findByUsername(username);
    } catch (DatabaseException e) {
      LOG.error("Error retrieving user {} in database. Caused by: {}", username, e.getErrorKind());
      return buildResponseForDatabaseError(e.getErrorKind(), username);
    }

    // Check that the password is correct for the user to update
    if (!foundUser.getPassword().equals(password)) {
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity("Unable to validate user with provided credentials").build();
    }

    PilotUser result;

    try {
      result = usersDao.update(user);
    } catch (DatabaseException e) {
      LOG.error("Error updating user {} in database. Caused by: {}", username, e.getErrorKind());
      return buildResponseForDatabaseError(e.getErrorKind(), username);
    }

    return Response.ok(result).build();
  }

  /**
   * Retrieves a PilotUser from the database.
   *
   * @param key The basic authentication key necessary to access the resource.
   * @param password The password of the user to fetch. Used to verify authentication.
   * @param username The username of the user to retrieve.
   * @return The pilotUser that was found in the database.
   */
  @GET
  public Response getUser(@Auth Key key,
                          @HeaderParam("password") String password,
                          @QueryParam("username") String username) {
    getRequests.mark();

    if (username == null || username.equals("")) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Incorrect or missing username query parameter.").build();
    }

    if (password == null || password.equals("")) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Incorrect or missing header credentials.").build();
    }

    PilotUser user;
    try {
      user = usersDao.findByUsername(username);
    } catch (DatabaseException e) {
      LOG.error("Error retrieving user {} in database. Caused by: {}", username, e.getErrorKind());
      return buildResponseForDatabaseError(e.getErrorKind(), username);
    }

    // Check that the password is correct for the user that was requested
    if (!user.getPassword().equals(password)) {
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity("Unable to validate user with provided credentials").build();
    }

    return Response.ok(user).build();
  }

  /**
   * Deletes a PilotUser from the database.
   *
   * @param key The basic authentication key necessary to access the resource.
   * @param password The password of the user to delete. Used to verify authentication.
   * @param username The username of the user to delete.
   * @return The user that was deleted from the database.
   */
  @DELETE
  public Response deleteUser(@Auth Key key,
                             @HeaderParam("password") String password,
                             @QueryParam("username") String username) {
    deleteRequests.mark();

    if (username == null || username.equals("")) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Incorrect or missing username query parameter.").build();
    }

    if (password == null || password.equals("")) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Incorrect or missing header credentials.").build();
    }

    PilotUser user;
    try {
      user = usersDao.findByUsername(username);
    } catch (DatabaseException e) {
      LOG.error("Error retrieving user {} in database. Caused by: {}", username, e.getErrorKind());
      return buildResponseForDatabaseError(e.getErrorKind(), username);
    }

    // Check that password is correct before deleting
    if (!user.getPassword().equals(password)) {
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity("Unable to validate user with provided credentials").build();
    }

    PilotUser result;
    try {
      result = usersDao.delete(username);
    } catch (DatabaseException e) {
      LOG.error("Error deleting user {} in database. Caused by: {}", username, e.getErrorKind());
      return buildResponseForDatabaseError(e.getErrorKind(), username);
    }

    return Response.ok(result).build();
  }

  /**
   * Returns the appropriate Response object for a given DatabaseError.
   *
   * @param error The DatabaseError that occurred.
   * @param username The username of the user that this error is related to.
   * @return An appropriate Response object to return to the caller.
   */
  private Response buildResponseForDatabaseError(DatabaseError error, String username) {
    switch (error) {
      case CONFLICT:
        return Response.status(Response.Status.CONFLICT)
            .entity(String.format("User %s already exists in DB.", username)).build();
      case USER_NOT_FOUND:
        return Response.status(Response.Status.NOT_FOUND)
            .entity(String.format("User %s not found in DB.", username)).build();
      case DATABASE_DOWN:
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
            .entity("Database is currently unavailable. Please try again later.").build();
      default:
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity("An unknonwn error occurred.").build();
    }
  }

}
