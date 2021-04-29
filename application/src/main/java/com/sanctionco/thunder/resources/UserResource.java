package com.sanctionco.thunder.resources;

import com.codahale.metrics.annotation.Metered;
import com.sanctionco.thunder.crypto.HashService;
import com.sanctionco.thunder.dao.DatabaseException;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.User;
import com.sanctionco.thunder.validation.RequestValidationException;
import com.sanctionco.thunder.validation.RequestValidator;

import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.security.Principal;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
   * Creates the new user in the database.
   *
   * @param auth the auth principal required to access the resource
   * @param user the user to create in the database
   * @return the HTTP response that indicates success or failure. If successful, the response will
   *     contain the created user.
   */
  @POST
  @Operation(
      summary = "Create a new user",
      description = "Creates a new user in the database and returns the created user.",
      tags = { "users" },
      responses = {
          @ApiResponse(responseCode = "201",
              description = "The user was successfully created",
              content = @Content(
                  mediaType = "application/json", schema = @Schema(implementation = User.class))),
          @ApiResponse(responseCode = "400",
              description = "The create request was malformed"),
          @ApiResponse(responseCode = "409",
              description = "The user already exists in the database"),
          @ApiResponse(responseCode = "500",
              description = "The database rejected the request for an unknown reason"),
          @ApiResponse(responseCode = "503",
              description = "The database is currently unavailable")
      })
  @Metered(name = "post-requests")
  public Response postUser(
      @Parameter(hidden = true) @Auth Principal auth,
      @RequestBody(
          description = "The User object to create.",
          required = true,
          content = @Content(
              schema = @Schema(implementation = User.class),
              mediaType = "application/json"))
          User user) {

    try {
      requestValidator.validate(user);
    } catch (RequestValidationException e) {
      return e.response();
    }

    LOG.info("Attempting to create new user {}.", user.getEmail().getAddress());

    // Hash the user's password
    String finalPassword = hashService.hash(user.getPassword());

    // Update the user to non-verified status
    User updatedUser = new User(
        new Email(user.getEmail().getAddress(), false, null),
        finalPassword,
        user.getProperties());

    return usersDao.insert(updatedUser)
        .thenApply(result -> {
          LOG.info("Successfully created new user {}.", user.getEmail());
          return Response.status(Response.Status.CREATED).entity(result).build();
        })
        .exceptionally(throwable -> {
          var cause = (DatabaseException) throwable.getCause();

          LOG.error("Error posting user {} to the database. Caused by {}",
              user.getEmail(), cause.getErrorKind());
          return cause.response(user.getEmail().getAddress());
        }).join();
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
  @Operation(
      summary = "Update an existing user",
      description = "Updates an existing user in the database and returns the updated user.",
      tags = { "users" },
      responses = {
          @ApiResponse(responseCode = "200",
              description = "The user was successfully updated",
              content = @Content(
                  mediaType = "application/json", schema = @Schema(implementation = User.class))),
          @ApiResponse(responseCode = "400",
              description = "The update request was malformed"),
          @ApiResponse(responseCode = "401",
              description = "The request was unauthorized"),
          @ApiResponse(responseCode = "404",
              description = "The existing user was not found in the database"),
          @ApiResponse(responseCode = "409",
              description = "A user with the updated email address already exists"),
          @ApiResponse(responseCode = "500",
              description = "The database rejected the request for an unknown reason"),
          @ApiResponse(responseCode = "503",
              description = "The database is currently unavailable")
      })
  @Metered(name = "update-requests")
  public Response updateUser(
      @Parameter(hidden = true) @Auth Principal auth,
      @Parameter(description = "The password of the user, necessary if the "
          + "headerPasswordCheck is enabled.") @HeaderParam("password") String password,
      @Parameter(description = "The existing email address of the user. Only necessary if "
          + "the email address is to be changed.") @QueryParam("email") String existingEmail,
      @RequestBody(description = "The updated User object to insert.",
          required = true,
          content = @Content(
              schema = @Schema(implementation = User.class),
              mediaType = "application/json"))
          User user) {

    try {
      requestValidator.validate(password, existingEmail, user);
    } catch (RequestValidationException e) {
      return e.response();
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
        .exceptionally(throwable -> {
          if (throwable.getCause() instanceof RequestValidationException e) {
            return e.response();
          }

          var cause = (DatabaseException) throwable.getCause();

          LOG.error("Error updating user {} in database. Caused by: {}",
              email, cause.getErrorKind());
          return cause.response(email);
        }).join();
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
  @Operation(
      summary = "Retrieve a user from the database",
      description = "Retrieves a user from the database and returns the user.",
      tags = { "users" },
      responses = {
          @ApiResponse(responseCode = "200",
              description = "The user was found and returned in the response body",
              content = @Content(
                  mediaType = "application/json", schema = @Schema(implementation = User.class))),
          @ApiResponse(responseCode = "400",
              description = "The get request was malformed"),
          @ApiResponse(responseCode = "401",
              description = "The request was unauthorized"),
          @ApiResponse(responseCode = "404",
              description = "The user was not found in the database"),
          @ApiResponse(responseCode = "503",
              description = "The database is currently unavailable")
      })
  @Metered(name = "get-requests")
  public Response getUser(
      @Parameter(hidden = true) @Auth Principal auth,
      @Parameter(description = "The password of the user, necessary if the "
          + "headerPasswordCheck option is enabled.") @HeaderParam("password") String password,
      @Parameter(description = "The email address of the user to retrieve.", required = true)
          @QueryParam("email") String email) {

    try {
      requestValidator.validate(password, email, false);
    } catch (RequestValidationException e) {
      return e.response();
    }

    LOG.info("Attempting to get user {}.", email);

    return usersDao.findByEmail(email)
        .thenApply(user -> {
          // Check that the password is correct for the user that was requested
          requestValidator.verifyPasswordHeader(password, user.getPassword());

          LOG.info("Successfully retrieved user {}.", email);
          return Response.ok(user).build();
        })
        .exceptionally(throwable -> {
          if (throwable.getCause() instanceof RequestValidationException e) {
            return e.response();
          }

          var cause = (DatabaseException) throwable.getCause();

          LOG.error("Error retrieving user {} in database. Caused by: {}",
              email, cause.getErrorKind());
          return cause.response(email);
        }).join();
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
  @Operation(
      summary = "Delete a user from the database",
      description = "Deletes a user from the database and returns the deleted user.",
      tags = { "users" },
      responses = {
          @ApiResponse(responseCode = "200",
              description = "The user was successfully deleted",
              content = @Content(
                  mediaType = "application/json", schema = @Schema(implementation = User.class))),
          @ApiResponse(responseCode = "400",
              description = "The get request was malformed"),
          @ApiResponse(responseCode = "401",
              description = "The request was unauthorized"),
          @ApiResponse(responseCode = "404",
              description = "The user was not found in the database"),
          @ApiResponse(responseCode = "503",
              description = "The database is currently unavailable")
      })
  @Metered(name = "delete-requests")
  public Response deleteUser(
      @Parameter(hidden = true) @Auth Principal auth,
      @Parameter(description = "The password of the user, necessary if the "
          + "headerPasswordCheck is enabled.") @HeaderParam("password") String password,
      @Parameter(description = "The email address of the user to delete.", required = true)
          @QueryParam("email") String email) {

    try {
      requestValidator.validate(password, email, false);
    } catch (RequestValidationException e) {
      return e.response();
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
        .exceptionally(throwable -> {
          // throwable will be a CompletionException with the actual exception as the cause
          // TODO we should create a custom exception class (ThunderException (?)
          //  that can build a response for any exception
          if (throwable.getCause() instanceof RequestValidationException e) {
            return e.response();
          }

          var cause = (DatabaseException) throwable.getCause();

          LOG.error("Error while deleting user {} in database. Caused by: {}",
              email, cause.getErrorKind());
          return cause.response(email);
        }).join();
  }
}
