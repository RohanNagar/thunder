package com.sanctionco.thunder.resources;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Metered;
import com.sanctionco.thunder.ThunderException;
import com.sanctionco.thunder.crypto.HashService;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.User;
import com.sanctionco.thunder.openapi.SwaggerAnnotations;
import com.sanctionco.thunder.util.MetricNameUtil;
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
  private final RequestOptions requestOptions;
  private final RequestValidator requestValidator;
  private final UsersDao usersDao;

  private final Counter createTimeoutCounter;
  private final Counter getTimeoutCounter;
  private final Counter updateTimeoutCounter;
  private final Counter deleteTimeoutCounter;

  /**
   * Constructs a new {@code UserResource} with the given users DAO, request validator,
   * hash service, and metrics.
   *
   * @param usersDao the DAO used to connect to the database
   * @param requestOptions the set of request options to use for each incoming request
   * @param requestValidator the validator used to validate incoming requests
   * @param hashService the service used to verify passwords in incoming requests
   * @param metrics the {@code MetricRegistry} instance used to register metrics
   */
  @Inject
  public UserResource(UsersDao usersDao,
                      RequestOptions requestOptions,
                      RequestValidator requestValidator,
                      HashService hashService,
                      MetricRegistry metrics) {
    this.usersDao = Objects.requireNonNull(usersDao);
    this.requestOptions = Objects.requireNonNull(requestOptions);
    this.requestValidator = Objects.requireNonNull(requestValidator);
    this.hashService = Objects.requireNonNull(hashService);

    this.createTimeoutCounter = metrics.counter(MetricNameUtil.CREATE_TIMEOUTS);
    this.getTimeoutCounter = metrics.counter(MetricNameUtil.GET_TIMEOUTS);
    this.updateTimeoutCounter = metrics.counter(MetricNameUtil.UPDATE_TIMEOUTS);
    this.deleteTimeoutCounter = metrics.counter(MetricNameUtil.DELETE_TIMEOUTS);
  }

  /**
   * Creates a new user in the database.
   *
   * @param response the async response object used to notify that the operation has completed
   * @param auth the auth principal required to access the resource
   * @param user the user to create in the database
   */
  @POST
  @Metered(name = "post-requests")
  @SwaggerAnnotations.Methods.Create
  public void postUser(@Suspended AsyncResponse response,
                       @Parameter(hidden = true) @Auth Principal auth,
                       User user) {
    requestOptions.setTimeout(response, createTimeoutCounter);

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
        .whenComplete((result, throwable) -> {
          if (Objects.isNull(throwable)) {
            LOG.info("Successfully created new user {}.", result.getEmail().getAddress());
            response.resume(Response.status(Response.Status.CREATED).entity(result).build());
          } else {
            LOG.error("Error creating new user {}. Caused by {}", email, throwable.getMessage());
            response.resume(ThunderException.responseFromThrowable(throwable, email));
          }
        });
  }

  /**
   * Updates the user in the database.
   *
   * @param response the async response object used to notify that the operation has completed
   * @param auth the auth principal required to access the resource
   * @param password the user's password. This should be the existing password prior to any updates.
   * @param existingEmail the user's existing email. This can be {@code null} if the user's email
   *                      will stay the same. It must be present if the email is to be changed.
   * @param user the user with updated properties
   */
  @PUT
  @Metered(name = "update-requests")
  @SwaggerAnnotations.Methods.Update
  public void updateUser(@Suspended AsyncResponse response,
                         @Parameter(hidden = true) @Auth Principal auth,
                         @Parameter(hidden = true) @HeaderParam("password") String password,
                         @Parameter(hidden = true) @QueryParam("email") String existingEmail,
                         User user) {
    requestOptions.setTimeout(response, updateTimeoutCounter);

    try {
      requestValidator.validate(password, existingEmail, user);
    } catch (RequestValidationException e) {
      response.resume(e.response(Optional.ofNullable(existingEmail)
          .orElseGet(() -> Optional.ofNullable(user)
              .map(User::getEmail)
              .map(Email::getAddress)
              .orElse("null"))));
      return;
    }

    // Get the current email address for the user
    String email = Optional.ofNullable(existingEmail).orElse(user.getEmail().getAddress());
    LOG.info("Attempting to update user with existing email address {}.", email);

    usersDao.findByEmail(email)
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
        .whenComplete((result, throwable) -> {
          if (Objects.isNull(throwable)) {
            LOG.info("Successfully updated user {}.", email);
            response.resume(Response.ok(result).build());
          } else {
            LOG.error("Error updating user {}. Caused by: {}", email, throwable.getMessage());
            response.resume(ThunderException.responseFromThrowable(throwable, email));
          }
        });
  }

  /**
   * Retrieves the user with the given email from the database.
   *
   * @param response the async response object used to notify that the operation has completed
   * @param auth the auth principal required to access the resource
   * @param password the user's password
   * @param email the email of the user
   */
  @GET
  @Metered(name = "get-requests")
  @SwaggerAnnotations.Methods.Get
  public void getUser(@Suspended AsyncResponse response,
                      @Parameter(hidden = true) @Auth Principal auth,
                      @Parameter(hidden = true) @HeaderParam("password") String password,
                      @Parameter(hidden = true) @QueryParam("email") String email) {
    requestOptions.setTimeout(response, getTimeoutCounter);

    try {
      requestValidator.validate(password, email, false);
    } catch (RequestValidationException e) {
      response.resume(e.response(email));
      return;
    }

    LOG.info("Attempting to get user {}.", email);

    usersDao.findByEmail(email)
        .thenAccept(user -> {
          // Check that the password is correct for the user that was requested
          requestValidator.verifyPasswordHeader(password, user.getPassword());

          LOG.info("Successfully retrieved user {}.", email);
          response.resume(Response.ok(user).build());
        })
        .exceptionally(throwable -> {
          LOG.error("Error retrieving user {}. Caused by: {}", email, throwable.getMessage());
          response.resume(ThunderException.responseFromThrowable(throwable, email));
          return null;
        });
  }

  /**
   * Deletes the user with the given email from the database.
   *
   * @param response the async response object used to notify that the operation has completed
   * @param auth the auth principal required to access the resource
   * @param password the user's password
   * @param email the email of the user
   */
  @DELETE
  @Metered(name = "delete-requests")
  @SwaggerAnnotations.Methods.Delete
  public void deleteUser(@Suspended AsyncResponse response,
                         @Parameter(hidden = true) @Auth Principal auth,
                         @Parameter(hidden = true) @HeaderParam("password") String password,
                         @Parameter(hidden = true) @QueryParam("email") String email) {
    requestOptions.setTimeout(response, deleteTimeoutCounter);

    try {
      requestValidator.validate(password, email, false);
    } catch (RequestValidationException e) {
      response.resume(e.response(email));
      return;
    }

    LOG.info("Attempting to delete user {}.", email);

    usersDao.findByEmail(email)
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
        // Send the success/failure result
        .whenComplete((result, throwable) -> {
          if (Objects.isNull(throwable)) {
            LOG.info("Successfully deleted user {}.", email);
            response.resume(Response.ok(result).build());
          } else {
            LOG.error("Error deleting user {}. Caused by: {}", email, throwable.getMessage());
            response.resume(ThunderException.responseFromThrowable(throwable, email));
          }
        });
  }
}
