package com.sanctionco.thunder.dao;

import com.sanctionco.thunder.ThunderException;

import jakarta.ws.rs.core.Response;

/**
 * Represents an exception that occurred during a database operation. Provides constructors
 * that use {@link Error} in order to provide more detail about the database failure.
 */
public class DatabaseException extends ThunderException {
  private final Error error;

  /**
   * Constructs a new {@code DatabaseException} with the given message and database error.
   *
   * @param message the exception message
   * @param error the type of error that occurred
   */
  public DatabaseException(String message, Error error) {
    super(message);

    this.error = error;
  }

  /**
   * Constructs a new {@code DatabaseException} with the given message, cause, and database error.
   *
   * @param message the exception message
   * @param cause the exception's cause
   * @param error the type of error that occurred
   */
  public DatabaseException(String message, Throwable cause, Error error) {
    super(message, cause);

    this.error = error;
  }

  public Error getError() {
    return error;
  }

  @Override
  public Response response(String email) {
    String message = String.format("%s (User: %s)", getMessage(), email);

    return switch (this.error) {
      case USER_NOT_FOUND -> Response.status(Response.Status.NOT_FOUND).entity(message).build();
      case CONFLICT -> Response.status(Response.Status.CONFLICT).entity(message).build();
      case DATABASE_DOWN -> Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity(message).build();
      // REQUEST_REJECTED is the same as the default
      default -> Response.serverError().entity(message).build();
    };
  }

  public enum Error {
    USER_NOT_FOUND,
    CONFLICT,
    DATABASE_DOWN,
    REQUEST_REJECTED
  }
}
