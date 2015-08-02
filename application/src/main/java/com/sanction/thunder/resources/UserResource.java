package com.sanction.thunder.resources;

import com.sanction.thunder.dao.StormUsersDao;
import com.sanction.thunder.models.StormUser;
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

  private final StormUsersDao usersDao;

  @Inject
  public UserResource(StormUsersDao usersDao) {
    this.usersDao = usersDao;
  }

  /**
   * Posts a new StormUser to the database.
   *
   * @param user The user to post to the database.
   * @return The user that was created in the database.
   */
  @POST
  public Response postUser(StormUser user) {
    if (user == null) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Cannot post a null user.").build();
    }

    StormUser result = usersDao.insert(user);
    if (result == null) {
      LOG.warn("Unable to post duplicate user {}.", user);
      return Response.status(Response.Status.CONFLICT)
          .entity("User already exists in DB.").build();
    }

    return Response.status(Response.Status.CREATED)
        .entity(result).build();
  }

  /**
   * Updates a StormUser in the database.
   *
   * @param user The user with updated properties. Must have the same username as previously had.
   * @return The user that was updated in the database.
   */
  @PUT
  public Response updateUser(StormUser user) {
    if (user == null) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Cannot put a null user.").build();
    }

    StormUser result = usersDao.update(user);
    if (result == null) {
      LOG.warn("Unable to update user {}.", user);
      return Response.status(Response.Status.NOT_FOUND)
          .entity(String.format("User %s does not exist or there was a conflict.", user)).build();
    }

    return Response.ok(result).build();
  }

  /**
   * Retrieves a StormUser from the database.
   *
   * @param username The username of the user to retrieve.
   * @return The user that was found in the database.
   */
  @GET
  public Response getUser(@QueryParam("username") String username) {
    if (username == null) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("username query parameter is required to get a user.").build();
    }

    StormUser user = usersDao.findByUsername(username);
    if (user == null) {
      LOG.warn("User with name {} did not exist in the DB.", username);
      return Response.status(Response.Status.NOT_FOUND)
          .entity(String.format("Unable to find user %s", username)).build();
    }

    return Response.ok(user).build();
  }

  /**
   * Deletes a StormUser from the database.
   *
   * @param username The username of the user to delete.
   * @return The user that was deleted from the database.
   */
  @DELETE
  public Response deleteUser(@QueryParam("username") String username) {
    if (username == null) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("username query parameter is required to delete a user.").build();
    }

    StormUser user = usersDao.delete(username);
    if (user == null) {
      LOG.warn("Unable to delete user with name {}.", username);
      return Response.status(Response.Status.NOT_FOUND)
          .entity(String.format("Unable to delete user %s, not found in DB.", username)).build();
    }

    return Response.ok(user).build();
  }

}
