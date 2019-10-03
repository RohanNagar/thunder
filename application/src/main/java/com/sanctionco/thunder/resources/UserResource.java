package com.sanctionco.thunder.resources;

import com.codahale.metrics.annotation.Metered;

import com.sanctionco.thunder.authentication.Key;
import com.sanctionco.thunder.crypto.HashService;
import com.sanctionco.thunder.dao.DatabaseException;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.User;
import com.sanctionco.thunder.validation.RequestValidator;

import io.dropwizard.auth.Auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.Objects;

import javax.inject.Inject;
import javax.validation.ValidationException;
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
   * @param key the basic authentication key required to access the resource
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
          @ApiResponse(responseCode = "200",
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
      @Parameter(hidden = true) @Auth Key key,
      @RequestBody(description = "The User object to create.", required = true,
          content = @Content(schema = @Schema(implementation = User.class))) User user) {

    try {
      requestValidator.validate(user);
    } catch (ValidationException e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(e.getMessage()).build();
    }

    LOG.info("Attempting to create new user {}.", user.getEmail().getAddress());

    // Hash the user's password
    String finalPassword = hashService.hash(user.getPassword());

    // Update the user to non-verified status
    User updatedUser = new User(
        new Email(user.getEmail().getAddress(), false, null),
        finalPassword,
        user.getProperties());

    User result;
    try {
      result = usersDao.insert(updatedUser);
    } catch (DatabaseException e) {
      LOG.error("Error posting user {} to the database. Caused by {}",
          user.getEmail(), e.getErrorKind());
      return e.getErrorKind().buildResponse(user.getEmail().getAddress());
    }

    LOG.info("Successfully created new user {}.", user.getEmail());
    return Response.status(Response.Status.CREATED).entity(result).build();
  }

  /**
   * Updates the user in the database.
   *
   * @param key the basic authentication key required to access the resource.
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
      @Parameter(hidden = true) @Auth Key key,
      @Parameter(description = "The password of the user, necessary if the "
          + "headerPasswordCheck is enabled.") @HeaderParam("password") String password,
      @Parameter(description = "The existing email address of the user. Only necessary if "
          + "the email address is to be changed.") @QueryParam("email") String existingEmail,
      @RequestBody(description = "The updated User object to insert.", required = true,
          content = @Content(schema = @Schema(implementation = User.class))) User user) {

    try {
      requestValidator.validate(password, existingEmail, user);
    } catch (ValidationException e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(e.getMessage()).build();
    }

    // Get the current email address for the user
    String email = existingEmail != null ? existingEmail : user.getEmail().getAddress();
    LOG.info("Attempting to update user with existing email address {}.", email);

    User foundUser;
    try {
      foundUser = usersDao.findByEmail(email);
    } catch (DatabaseException e) {
      LOG.error("Error retrieving user {} in database. Caused by: {}", email, e.getErrorKind());
      return e.getErrorKind().buildResponse(email);
    }

    // Check that the password is correct for the user to update
    if (requestValidator.isPasswordHeaderCheckEnabled()
        && !hashService.isMatch(password, foundUser.getPassword())) {
      LOG.error("The password for user {} was incorrect.", email);
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity("Unable to validate user with provided credentials.").build();
    }

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

    User updatedUser = new User(
        new Email(user.getEmail().getAddress(), verified, verificationToken),
        finalPassword,
        user.getProperties());

    User result;

    try {
      result = usersDao.update(existingEmail, updatedUser);
    } catch (DatabaseException e) {
      LOG.error("Error updating user {} in database. Caused by: {}", email, e.getErrorKind());
      return e.getErrorKind().buildResponse(user.getEmail().getAddress());
    }

    LOG.info("Successfully updated user {}.", email);
    return Response.ok(result).build();
  }

  /**
   * Retrieves the user with the given email from the database.
   *
   * @param key the basic authentication key required to access the resource
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
      @Parameter(hidden = true) @Auth Key key,
      @Parameter(description = "The password of the user, necessary if the "
          + "headerPasswordCheck option is enabled.") @HeaderParam("password") String password,
      @Parameter(description = "The email address of the user to retrieve.", required = true)
          @QueryParam("email") String email) {

    try {
      requestValidator.validate(password, email, false);
    } catch (ValidationException e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(e.getMessage()).build();
    }

    LOG.info("Attempting to get user {}.", email);

    User user;
    try {
      user = usersDao.findByEmail(email);
    } catch (DatabaseException e) {
      LOG.error("Error retrieving user {} in database. Caused by: {}", email, e.getErrorKind());
      return e.getErrorKind().buildResponse(email);
    }

    // Check that the password is correct for the user that was requested
    if (requestValidator.isPasswordHeaderCheckEnabled()
        && !hashService.isMatch(password, user.getPassword())) {
      LOG.error("The password for user {} was incorrect.", email);
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity("Unable to validate user with provided credentials.").build();
    }

    LOG.info("Successfully retrieved user {}.", email);
    return Response.ok(user).build();
  }

  /**
   * Deletes the user with the given email from the database.
   *
   * @param key the basic authentication key required to access the resource.
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
      @Parameter(hidden = true) @Auth Key key,
      @Parameter(description = "The password of the user, necessary if the "
          + "headerPasswordCheck is enabled.") @HeaderParam("password") String password,
      @Parameter(description = "The email address of the user to delete.", required = true)
          @QueryParam("email") String email) {

    try {
      requestValidator.validate(password, email, false);
    } catch (ValidationException e) {
      return Response.status(Response.Status.BAD_REQUEST)
        .entity(e.getMessage()).build();
    }

    LOG.info("Attempting to delete user {}.", email);

    User user;
    try {
      user = usersDao.findByEmail(email);
    } catch (DatabaseException e) {
      LOG.error("Error retrieving user {} in database. Caused by: {}", email, e.getErrorKind());
      return e.getErrorKind().buildResponse(email);
    }

    // Check that password is correct before deleting
    if (requestValidator.isPasswordHeaderCheckEnabled()
        && !hashService.isMatch(password, user.getPassword())) {
      LOG.error("The password for user {} was incorrect.", email);
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity("Unable to validate user with provided credentials.").build();
    }

    User result;
    try {
      result = usersDao.delete(email);
    } catch (DatabaseException e) {
      LOG.error("Error deleting user {} in database. Caused by: {}", email, e.getErrorKind());
      return e.getErrorKind().buildResponse(email);
    }

    LOG.info("Successfully deleted user {}.", email);
    return Response.ok(result).build();
  }
}
