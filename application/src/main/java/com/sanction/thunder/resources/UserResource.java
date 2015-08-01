package com.sanction.thunder.resources;

import com.sanction.thunder.dao.StormUsersDao;
import com.sanction.thunder.models.StormUser;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
   * @return The response code that corresponds to the status of the request.
   */
  @POST
  public Response postUser(StormUser user) {
    if (user == null) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Cannot post a null user.").build();
    }

    boolean insert = usersDao.insert(user);
    if (!insert) {
      LOG.warn("Unable to post user {}.", user);
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity("Unable to insert user in DB.").build();
    }

    return Response.status(Response.Status.CREATED)
        .entity(true).build();
  }

  /**
   * Retrieves a StormUser from the databse.
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
      LOG.warn("User {} did not exist in the DB.", username);
      return Response.status(Response.Status.NOT_FOUND)
          .entity(String.format("Unable to find user %s", username)).build();
    }

    return Response.ok(user).build();
  }

}
