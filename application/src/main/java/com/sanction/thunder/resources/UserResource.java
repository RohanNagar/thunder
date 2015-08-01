package com.sanction.thunder.resources;

import com.sanction.thunder.dao.StormUsersDao;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/users")
public class UserResource {
  private final StormUsersDao usersDao;

  @Inject
  public UserResource(StormUsersDao usersDao) {
    this.usersDao = usersDao;
  }

  @GET
  public Response getUser() {
    return Response.ok("Worked!").build();
  }

}
