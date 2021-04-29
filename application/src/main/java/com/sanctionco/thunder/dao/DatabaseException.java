package com.sanctionco.thunder.dao;

import com.sanctionco.thunder.ThunderException;

import javax.ws.rs.core.Response;

/**
 * Represents an exception that occurred during a database operation. Provides constructors
 * that use {@link DatabaseError} in order to provide more detail about the database failure.
 */
public class DatabaseException extends ThunderException {
  private final DatabaseError error;

  /**
   * Constructs a new {@code DatabaseException} with the given database error.
   *
   * @param error the type of error that occurred
   */
  public DatabaseException(DatabaseError error) {
    super("An error occurred in the database interaction.");

    this.error = error;
  }

  /**
   * Constructs a new {@code DatabaseException} with the given message and database error.
   *
   * @param message the exception message
   * @param error the type of error that occurred
   */
  public DatabaseException(String message, DatabaseError error) {
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
  public DatabaseException(String message, Throwable cause, DatabaseError error) {
    super(message, cause);

    this.error = error;
  }

  public DatabaseError getErrorKind() {
    return error;
  }

  @Override
  public Response response() {
    return response("unknown");
  }

  @Override
  public Response response(String email) {
    return error.buildResponse(email);
  }
}
