package com.sanctionco.thunder;

import javax.ws.rs.core.Response;

/**
 * An exception class that is the parent class of internal exceptions within
 * the Thunder application.
 */
public class ThunderException extends RuntimeException {

  /**
   * Constructs a new instance of {@code ThunderException}.
   *
   * @param message a description of the exception
   */
  public ThunderException(String message) {
    super(message);
  }

  /**
   * Constructs a new instance of {@code ThunderException}.
   *
   * @param message a description of the exception
   * @param cause the cause of the exception
   */
  public ThunderException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Build a {@link Response} object representing the failure that
   * can be returned as an HTTP response.
   *
   * @return the built response instance
   */
  public Response response() {
    return Response.serverError().entity(getMessage()).build();
  }
}
