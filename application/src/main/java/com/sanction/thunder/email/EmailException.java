package com.sanction.thunder.email;

public class EmailException extends RuntimeException {

  /**
   * Constructs a new EmailException.
   */
  public EmailException() {
    super();
  }

  /**
   * Constructs a new EmailException.
   *
   * @param message The message for the exception.
   */
  public EmailException(String message) {
    super(message);
  }

  /**
   * Constructs a new EmailException.
   *
   * @param message The message for the exception.
   * @param cause The cause of the exception.
   */
  public EmailException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new EmailException.
   *
   * @param cause The cause of the exception.
   */
  public EmailException(Throwable cause) {
    super(cause);
  }

}
