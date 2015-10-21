package com.sanction.thunder.resources;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.sanction.thunder.authentication.Key;
import com.sanction.thunder.dao.PilotUsersDao;
import com.sanction.thunder.models.PilotUser;

import io.dropwizard.auth.Auth;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
   * Constructs a new UserResource to allow access to the User DB.
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

    PilotUser result = usersDao.insert(user);
    if (result == null) {
      LOG.warn("Unable to post duplicate user {}.", user);
      return Response.status(Response.Status.CONFLICT)
          .entity("User already exists in DB.").build();
    }

    return Response.status(Response.Status.CREATED)
        .entity(result).build();
  }

  /**
   * Updates a PilotUser in the database.
   *
   * @param user The user with updated properties. Must have the same username as previously had.
   * @return The user that was updated in the database.
   */
  @PUT
  public Response updateUser(@Auth Key key, PilotUser user) {
    updateRequests.mark();

    if (user == null) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Cannot put a null user.").build();
    }

    PilotUser result = usersDao.update(user);
    if (result == null) {
      LOG.warn("Unable to update user {}.", user);
      return Response.status(Response.Status.NOT_FOUND)
          .entity(String.format("User %s does not exist or there was a conflict.", user)).build();
    }

    return Response.ok(result).build();
  }

  /**
   * Retrieves a PilotUser from the database.
   *
   * @param username The username of the user to retrieve.
   * @return The user that was found in the database.
   */
  @GET
  public Response getUser(@Auth Key key, @QueryParam("username") String username) {
    getRequests.mark();

    if (username == null || username.equals("")) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("username query parameter is required to get a user.").build();
    }

    PilotUser user = usersDao.findByUsername(username);
    if (user == null) {
      LOG.warn("User with name {} did not exist in the DB.", username);
      return Response.status(Response.Status.NOT_FOUND)
          .entity(String.format("Unable to find user %s", username)).build();
    }

    return Response.ok(user).build();
  }

  /**
   * Deletes a PilotUser from the database.
   *
   * @param username The username of the user to delete.
   * @return The user that was deleted from the database.
   */
  @DELETE
  public Response deleteUser(@Auth Key key, @QueryParam("username") String username) {
    deleteRequests.mark();

    if (username == null || username.equals("")) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("username query parameter is required to delete a user.").build();
    }

    PilotUser user = usersDao.delete(username);
    if (user == null) {
      LOG.warn("Unable to delete user with name {}.", username);
      return Response.status(Response.Status.NOT_FOUND)
          .entity(String.format("Unable to delete user %s, not found in DB.", username)).build();
    }

    return Response.ok(user).build();
  }

}
