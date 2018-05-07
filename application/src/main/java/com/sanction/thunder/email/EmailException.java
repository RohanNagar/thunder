package com.sanction.thunder.email;

/**
 * A convenient {@code Exception} class to be used if an exception occurs during
 * an email operation.
 */
class EmailException extends RuntimeException {

  /**
   * Constructs a new EmailException.
   */
  EmailException() {
    super();
  }

  /**
   * Constructs a new EmailException.
   *
   * @param message The message for the exception.
   */
  EmailException(String message) {
    super(message);
  }

  /**
   * Constructs a new EmailException.
   *
   * @param message The message for the exception.
   * @param cause The cause of the exception.
   */
  EmailException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new EmailException.
   *
   * @param cause The cause of the exception.
   */
  EmailException(Throwable cause) {
    super(cause);
  }
}
