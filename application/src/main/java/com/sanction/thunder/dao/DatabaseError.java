package com.sanction.thunder.dao;

import javax.ws.rs.core.Response;

/**
 * Describes a specific Database Error that may occur. This should be used when throwing a
 * {@link DatabaseException} in order to provide more information to the caller on why
 * an exception occurred.
 *
 * @see DatabaseException
 */
public enum DatabaseError {
  CONFLICT {
    @Override
    public Response buildResponse(String email) {
      return Response.status(Response.Status.CONFLICT)
          .entity(String.format("User %s already exists in DB.", email)).build();
    }
  },
  USER_NOT_FOUND {
    @Override
    public Response buildResponse(String email) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(String.format("User %s not found in DB.", email)).build();
    }
  },
  REQUEST_REJECTED {
    @Override
    public Response buildResponse(String email) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("The database rejected the request. Check your data and try again.").build();
    }
  },
  DATABASE_DOWN {
    @Override
    public Response buildResponse(String email) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity("Database is currently unavailable. Please try again later.").build();
    }
  };

  /**
   * Builds a new Response object that can be returned as an HTTP response.
   *
   * @param email The email address that the database request was for.
   * @return The built Response instance.
   */
  public abstract Response buildResponse(String email);
}
