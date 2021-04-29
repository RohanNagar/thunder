package com.sanctionco.thunder.validation;

import com.sanctionco.thunder.ThunderException;

import javax.ws.rs.core.Response;

/**
 * An exception that represents a validation issue with a request.
 */
public class RequestValidationException extends ThunderException {
  private final Error error;

  /**
   * Constructs a new instance of {@code RequestValidationException}.
   *
   * @param message a description of the exception
   * @param error the type of {@link RequestValidationException.Error} causing the exception
   */
  public RequestValidationException(String message, Error error) {
    super(message);
    this.error = error;
  }

  /**
   * Constructs a new instance of {@code RequestValidationException}.
   *
   * @param message a description of the exception
   * @param cause the cause of the exception
   * @param error the type of {@link RequestValidationException.Error} causing the exception
   */
  public RequestValidationException(String message, Throwable cause, Error error) {
    super(message, cause);
    this.error = error;
  }

  /**
   * Gets the error associated with exception.
   *
   * @return the associated error reason
   */
  public Error getError() {
    return error;
  }

  @Override
  public Response response() {
    return switch (this.error) {
      case INVALID_PARAMETERS -> Response.status(Response.Status.BAD_REQUEST)
          .entity(getMessage()).build();
      case INCORRECT_PASSWORD -> Response.status(Response.Status.UNAUTHORIZED)
          .entity(getMessage()).build();
      default -> Response.serverError().entity(getMessage()).build();
    };
  }

  @Override
  public Response response(String email) {
    var message = String.format("%s (User: %s)", getMessage(), email);

    return switch (this.error) {
      case INVALID_PARAMETERS -> Response.status(Response.Status.BAD_REQUEST)
          .entity(message).build();
      case INCORRECT_PASSWORD -> Response.status(Response.Status.UNAUTHORIZED)
          .entity(message).build();
      default -> Response.serverError().entity(message).build();
    };
  }

  /**
   * Construct a new {@code RequestValidationException} caused by invalid parameters
   * with a default message.
   *
   * @return the new {@code RequestValidationException}
   */
  public static RequestValidationException invalidParameters() {
    return new RequestValidationException("The request was malformed.", Error.INVALID_PARAMETERS);
  }

  /**
   * Construct a new {@code RequestValidationException} caused by invalid parameters.
   *
   * @param message a description of the exception
   * @return the new {@code RequestValidationException}
   */
  public static RequestValidationException invalidParameters(String message) {
    return new RequestValidationException(message, Error.INVALID_PARAMETERS);
  }

  /**
   * Construct a new {@code RequestValidationException} caused by invalid parameters.
   *
   * @param message a description of the exception
   * @param cause the cause of the exception
   * @return the new {@code RequestValidationException}
   */
  public static RequestValidationException invalidParameters(String message, Throwable cause) {
    return new RequestValidationException(message, cause, Error.INVALID_PARAMETERS);
  }

  /**
   * Construct a new {@code RequestValidationException} caused by an incorrect password
   * with a default message.
   *
   * @return the new {@code RequestValidationException}
   */
  public static RequestValidationException incorrectPassword() {
    return new RequestValidationException(
        "Unable to validate the request with the provided credentials", Error.INCORRECT_PASSWORD);
  }

  /**
   * Construct a new {@code RequestValidationException} caused by an incorrect password.
   *
   * @param message a description of the exception
   * @return the new {@code RequestValidationException}
   */
  public static RequestValidationException incorrectPassword(String message) {
    return new RequestValidationException(message, Error.INCORRECT_PASSWORD);
  }

  /**
   * Construct a new {@code RequestValidationException} caused by an incorrect password.
   *
   * @param message a description of the exception
   * @param cause the cause of the exception
   * @return the new {@code RequestValidationException}
   */
  public static RequestValidationException incorrectPassword(String message, Throwable cause) {
    return new RequestValidationException(message, cause, Error.INCORRECT_PASSWORD);
  }

  /**
   * The allowed types of errors to associate with a {@code RequestValidationException}.
   */
  public enum Error {
    INVALID_PARAMETERS,
    INCORRECT_PASSWORD,
    UNKNOWN
  }
}
