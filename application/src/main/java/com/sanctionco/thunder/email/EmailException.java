package com.sanctionco.thunder.email;

/**
 * Represents an exception that occurred during an email operation.
 */
class EmailException extends RuntimeException {

  /**
   * Constructs a new {@code EmailException}.
   */
  EmailException() {
    super();
  }

  /**
   * Constructs a new {@code EmailException} with the given message.
   *
   * @param message the exception message
   */
  EmailException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@code EmailException} with the given message and cause.
   *
   * @param message the exception message
   * @param cause the exception's cause
   */
  EmailException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new {@code EmailException} with the given cause.
   *
   * @param cause the exception's cause
   */
  EmailException(Throwable cause) {
    super(cause);
  }
}
