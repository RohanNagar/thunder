package com.sanction.thunder.dao;

/**
 * A convenient {@code Exception} class to be used if an exception occurs during
 * a database operation. Use {@link DatabaseError} to provide more detail.
 */
public class DatabaseException extends RuntimeException {
  private final DatabaseError error;

  /**
   * Constructs a new DatabaseException.
   *
   * @param error The type of error that occurred.
   */
  public DatabaseException(DatabaseError error) {
    this.error = error;
  }

  /**
   * Constructs a new DatabaseException.
   *
   * @param message The message for the exception.
   * @param error The type of error that occurred.
   */
  public DatabaseException(String message, DatabaseError error) {
    super(message);

    this.error = error;
  }

  /**
   * Constructs a new DatabaseException.
   *
   * @param message The message for the exception.
   * @param cause The cause of the exception.
   * @param error The type of error that occurred.
   */
  public DatabaseException(String message, Throwable cause, DatabaseError error) {
    super(message, cause);

    this.error = error;
  }

  /**
   * Constructs a new DatabaseException.
   *
   * @param cause The cause of the exception.
   * @param error The type of error that occurred.
   */
  public DatabaseException(Throwable cause, DatabaseError error) {
    super(cause);

    this.error = error;
  }

  public DatabaseError getErrorKind() {
    return error;
  }
}
