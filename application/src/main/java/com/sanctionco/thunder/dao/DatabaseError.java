package com.sanctionco.thunder.dao;

import javax.ws.rs.core.Response;

/**
 * Describes a specific database error. This enum should be used when throwing a
 * {@link DatabaseException} in order to provide more information to the caller on why
 * a database exception exception occurred.
 */
public enum DatabaseError {
  CONFLICT {
    @Override
    public Response buildResponse(String email) {
      return Response.status(Response.Status.CONFLICT)
          .entity(String.format("User %s already exists in the database.", email)).build();
    }
  },
  USER_NOT_FOUND {
    @Override
    public Response buildResponse(String email) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(String.format("User %s not found in the database.", email)).build();
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
   * Builds a new HTTP Response object from the given email that describes the database error.
   *
   * @param email the email address used in the database request that failed
   * @return the constructed HTTP response
   */
  public abstract Response buildResponse(String email);
}
