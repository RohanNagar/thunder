package com.sanctionco.thunder.resources;

import com.codahale.metrics.annotation.Metered;
import com.sanctionco.thunder.ThunderException;
import com.sanctionco.thunder.crypto.HashService;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.User;
import com.sanctionco.thunder.openapi.SwaggerAnnotations;
import com.sanctionco.thunder.validation.RequestValidationException;
import com.sanctionco.thunder.validation.RequestValidator;

import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;

import java.security.Principal;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides API methods to create, fetch, update, and delete {@code User}
 * (in the {@code api} module) objects. The methods contained in this class are
 * available at the {@code /users} endpoint, and return JSON in the response.
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
  private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);

  private final HashService hashService;
  private final RequestValidator requestValidator;
  private final UsersDao usersDao;

  /**
   * Constructs a new {@code UserResource} with the given users DAO, request validator,
   * hash service, and metrics.
   *
   * @param usersDao the DAO used to connect to the database
   * @param requestValidator the validator used to validate incoming requests
   * @param hashService the service used to verify passwords in incoming requests
   */
  @Inject
  public UserResource(UsersDao usersDao,
                      RequestValidator requestValidator,
                      HashService hashService) {
    this.usersDao = Objects.requireNonNull(usersDao);
    this.requestValidator = Objects.requireNonNull(requestValidator);
    this.hashService = Objects.requireNonNull(hashService);
  }

  /**
   * Creates a new user in the database.
   *
   * @param auth the auth principal required to access the resource
   * @param user the user to create in the database
   * @param response the async response object used to notify that the operation has completed
   */
  @POST
  @Metered(name = "post-requests")
  @SwaggerAnnotations.Methods.Create
  public void postUser(@Parameter(hidden = true) @Auth Principal auth,
                       User user,
                       @Suspended AsyncResponse response) {
    try {
      requestValidator.validate(user);
    } catch (RequestValidationException exception) {
      response.resume(exception.response(
          Optional.ofNullable(user)
              .map(User::getEmail)
              .map(Email::getAddress)
              .orElse("null")));
      return;
    }

    String email = user.getEmail().getAddress();
    LOG.info("Attempting to create new user {}.", email);

    // Hash the user's password
    String finalPassword = hashService.hash(user.getPassword());

    // Make sure the user is not verified, as this is a new user
    User userToInsert = new User(Email.unverified(email), finalPassword, user.getProperties());

    usersDao.insert(userToInsert)
        .thenApply(result -> {
          LOG.info("Successfully created new user {}.", result.getEmail().getAddress());
          return response.resume(Response.status(Response.Status.CREATED).entity(result).build());
        })
        .exceptionally(throwable -> {
          var cause = (ThunderException) throwable.getCause();

          LOG.error("Error posting user {} to the database. Caused by {}",
              email, cause.getMessage());
          return response.resume(cause.response(email));
        });
  }

  /**
   * Updates the user in the database.
   *
   * @param auth the auth principal required to access the resource
   * @param password the user's password. This should be the existing password prior to any updates.
   * @param existingEmail the user's existing email. This can be {@code null} if the user's email
   *                      will stay the same. It must be present if the email is to be changed.
   * @param user the user with updated properties
   * @return the HTTP response that indicates success or failure. If successful, the response will
   *     contain the updated user.
   */
  @PUT
  @Metered(name = "update-requests")
  @SwaggerAnnotations.Methods.Update
  public Response updateUser(@Parameter(hidden = true) @Auth Principal auth,
                             @Parameter(hidden = true) @HeaderParam("password") String password,
                             @Parameter(hidden = true) @QueryParam("email") String existingEmail,
                             User user) {
    try {
      requestValidator.validate(password, existingEmail, user);
    } catch (RequestValidationException e) {
      return e.response(Optional.ofNullable(existingEmail)
          .orElseGet(() -> Optional.ofNullable(user)
              .map(User::getEmail)
              .map(Email::getAddress)
              .orElse("null")));
    }

    // Get the current email address for the user
    String email = Optional.ofNullable(existingEmail).orElse(user.getEmail().getAddress());
    LOG.info("Attempting to update user with existing email address {}.", email);

    return usersDao.findByEmail(email)
        .thenApply(foundUser -> {
          // Check that the password is correct for the user to update
          requestValidator.verifyPasswordHeader(password, foundUser.getPassword());

          // Determine what verification information to use for the updated user object.
          // If it's a new email address, reset verification status.
          // If it's the same, keep the existing verification status.
          boolean verified = email.equals(user.getEmail().getAddress())
              && foundUser.getEmail().isVerified();

          String verificationToken = email.equals(user.getEmail().getAddress())
              ? foundUser.getEmail().getVerificationToken()
              : null;

          // Hash the password if it is a new password
          String finalPassword = foundUser.getPassword();

          if (!hashService.isMatch(user.getPassword(), foundUser.getPassword())) {
            finalPassword = hashService.hash(user.getPassword());
          }

          LOG.info("Using verified status: {} and token: {} for the updated user.",
              verified, verificationToken);

          return new User(
              new Email(user.getEmail().getAddress(), verified, verificationToken),
              finalPassword,
              user.getProperties());
        })
        .thenCompose(updatedUser -> usersDao.update(existingEmail, updatedUser))
        .thenApply(result -> {
          LOG.info("Successfully updated user {}.", email);
          return Response.ok(result).build();
        })
        .exceptionally(throwable -> handleFutureException(
            "Error updating user {} in database. Caused by: {}", email, throwable))
        .join();
  }

  /**
   * Retrieves the user with the given email from the database.
   *
   * @param auth the auth principal required to access the resource
   * @param password the user's password
   * @param email the email of the user
   * @return the HTTP response that indicates success or failure. If successful, the response will
   *     contain the user.
   */
  @GET
  @Metered(name = "get-requests")
  @SwaggerAnnotations.Methods.Get
  public Response getUser(@Parameter(hidden = true) @Auth Principal auth,
                          @Parameter(hidden = true) @HeaderParam("password") String password,
                          @Parameter(hidden = true) @QueryParam("email") String email) {
    try {
      requestValidator.validate(password, email, false);
    } catch (RequestValidationException e) {
      return e.response(email);
    }

    LOG.info("Attempting to get user {}.", email);

    return usersDao.findByEmail(email)
        .thenApply(user -> {
          // Check that the password is correct for the user that was requested
          requestValidator.verifyPasswordHeader(password, user.getPassword());

          LOG.info("Successfully retrieved user {}.", email);
          return Response.ok(user).build();
        })
        .exceptionally(throwable -> handleFutureException(
            "Error retrieving user {} in database. Caused by: {}", email, throwable))
        .join();
  }

  /**
   * Deletes the user with the given email from the database.
   *
   * @param auth the auth principal required to access the resource
   * @param password the user's password
   * @param email the email of the user
   * @return the HTTP response that indicates success or failure. If successful, the response will
   *     contain the deleted user.
   */
  @DELETE
  @Metered(name = "delete-requests")
  @SwaggerAnnotations.Methods.Delete
  public Response deleteUser(@Parameter(hidden = true) @Auth Principal auth,
                             @Parameter(hidden = true) @HeaderParam("password") String password,
                             @Parameter(hidden = true) @QueryParam("email") String email) {
    try {
      requestValidator.validate(password, email, false);
    } catch (RequestValidationException e) {
      return e.response(email);
    }

    LOG.info("Attempting to delete user {}.", email);

    return usersDao.findByEmail(email)
        // Get the user to make sure the password header is correct (if enabled)
        .thenAccept(user -> {
          if (requestValidator.isPasswordHeaderCheckEnabled()
              && !hashService.isMatch(password, user.getPassword())) {
            LOG.error("The password for user {} was incorrect.", email);
            throw RequestValidationException
                .incorrectPassword("Unable to validate user with provided credentials.");
          }
        })
        // Once we verify the password header, delete the user
        .thenCompose(Void -> usersDao.delete(email))
        // Build the response
        .thenApply(result -> {
          LOG.info("Successfully deleted user {}.", email);
          return Response.ok(result).build();
        })
        .exceptionally(throwable -> handleFutureException(
            "Error while deleting user {} in database. Caused by: {}", email, throwable))
        .join();
  }

  private Response handleFutureException(String logMessage, String email, Throwable throwable) {
    var cause = (ThunderException) throwable.getCause();

    LOG.error(logMessage, email, cause.getMessage());
    return cause.response(email);
  }
}
