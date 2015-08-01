package com.sanction.thunder.resources;

import com.sanction.thunder.dao.StormUsersDao;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path("/users")
public class UserResource {
  private final StormUsersDao usersDao;

  @Inject
  public UserResource(StormUsersDao usersDao) {
    this.usersDao = usersDao;
  }

}
