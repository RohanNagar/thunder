package com.sanction.thunder.resources;

import com.sanction.thunder.dao.UsersDao;

import javax.inject.Inject;
import javax.ws.rs.Path;

@Path("/users")
public class UserResource {
  private final UsersDao usersDao;

  @Inject
  public UserResource(UsersDao usersDao) {
    this.usersDao = usersDao;
  }

}
