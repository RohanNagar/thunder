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
  public Response response(String email) {
    var message = String.format("%s (User: %s)", getMessage(), email);

    return switch (this.error) {
      case INVALID_PARAMETERS, INCORRECT_TOKEN -> Response.status(Response.Status.BAD_REQUEST)
          .entity(message).build();
      case INCORRECT_PASSWORD -> Response.status(Response.Status.UNAUTHORIZED)
          .entity(message).build();
      // TOKEN_NOT_SET is same as default
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
   * Construct a new {@code RequestValidationException} caused by an incorrect token
   * with a default message.
   *
   * @return the new {@code RequestValidationException}
   */
  public static RequestValidationException incorrectToken() {
    return new RequestValidationException("Incorrect verification token.", Error.INCORRECT_TOKEN);
  }

  /**
   * Construct a new {@code RequestValidationException} caused by an incorrect token.
   *
   * @param message a description of the exception
   * @return the new {@code RequestValidationException}
   */
  public static RequestValidationException incorrectToken(String message) {
    return new RequestValidationException(message, Error.INCORRECT_TOKEN);
  }

  /**
   * Construct a new {@code RequestValidationException} caused by a null or empty token in
   * the database.
   *
   * @return the new {@code RequestValidationException}
   */
  public static RequestValidationException tokenNotSet() {
    return new RequestValidationException("Empty value found for user verification token.",
        Error.TOKEN_NOT_SET);
  }

  /**
   * Construct a new {@code RequestValidationException} caused by a null or empty token in
   * the database.
   *
   * @param message a description of the exception
   * @return the new {@code RequestValidationException}
   */
  public static RequestValidationException tokenNotSet(String message) {
    return new RequestValidationException(message, Error.TOKEN_NOT_SET);
  }

  /**
   * The allowed types of errors to associate with a {@code RequestValidationException}.
   */
  public enum Error {
    INVALID_PARAMETERS,
    INCORRECT_PASSWORD,
    TOKEN_NOT_SET,
    INCORRECT_TOKEN
  }
}
