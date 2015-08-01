package com.sanction.thunder.resources;

import com.sanction.thunder.dao.StormUsersDao;
import com.sanction.thunder.models.StormUser;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/users")
public class UserResource {
  private final StormUsersDao usersDao;

  @Inject
  public UserResource(StormUsersDao usersDao) {
    this.usersDao = usersDao;
  }

  /**
   * Posts a new StormUser to the database.
   *
   * @param user The user to post to the databse.
   * @return The response code that corresponds to the status of the request.
   */
  @POST
  public Response postUser(StormUser user) {
    if (user == null) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("Cannot post a null user").build();
    }

    boolean insert = usersDao.insert(user);
    if (!insert) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity("Unable to insert user in DB.").build();
    }

    return Response.status(Response.Status.CREATED).build();
  }

  @GET
  public Response getUser() {
    return Response.ok("Worked!").build();
  }

}
