package com.sanctionco.thunder.resources;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.sanctionco.thunder.authentication.Key;
import com.sanctionco.thunder.crypto.HashService;
import com.sanctionco.thunder.dao.DatabaseException;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.User;
import com.sanctionco.thunder.validation.RequestValidator;

import io.dropwizard.auth.Auth;

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
 * Provides API methods on {@link User} objects. The methods contained in this class are
 * available at the {@code /users} endpoint, and return JSON in the response.
 *
 * @see User
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
  private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);

  private final HashService hashService;
  private final RequestValidator requestValidator;
  private final UsersDao usersDao;

  // Counts number of requests
  private final Meter postRequests;
  private final Meter updateRequests;
  private final Meter getRequests;
  private final Meter deleteRequests;

  /**
   * Constructs a new UserResource to allow access to the user DB.
   *
   * @param usersDao The DAO to connect to the database with.
   * @param requestValidator The validator object used to validate incoming requests.
   * @param metrics The metrics object to set up meters with.
   */
  @Inject
  public UserResource(UsersDao usersDao,
                      RequestValidator requestValidator,
                      HashService hashService,
                      MetricRegistry metrics) {
    this.usersDao = Objects.requireNonNull(usersDao);
    this.requestValidator = Objects.requireNonNull(requestValidator);
    this.hashService = Objects.requireNonNull(hashService);

    // Set up metrics
    this.postRequests = metrics.meter(MetricRegistry.name(
        UserResource.class,
        "post-requests"));
    this.updateRequests = metrics.meter(MetricRegistry.name(
        UserResource.class,
        "update-requests"));
    this.getRequests = metrics.meter(MetricRegistry.name(
        UserResource.class,
        "get-requests"));
    this.deleteRequests = metrics.meter(MetricRegistry.name(
        UserResource.class,
        "delete-requests"));
  }

  /**
   * Posts a new User to the database.
   *
   * @param key The basic authentication key necessary to access the resource.
   * @param user The user to post to the database.
   * @return The user that was created in the database.
   */
  @POST
  public Response postUser(@Auth Key key, User user) {
    postRequests.mark();

    try {
      requestValidator.validate(user);
    } catch (ValidationException e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(e.getMessage()).build();
    }

    LOG.info("Attempting to create new user {}.", user.getEmail().getAddress());

    // Update the user to non-verified status
    User updatedUser = new User(
        new Email(user.getEmail().getAddress(), false, null),
        user.getPassword(),
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
   * Updates a User in the database.
   *
   * @param key The basic authentication key necessary to access the resource.
   * @param password The password of the user to update. This should be the current password
   *                 before any updates are made. Used to verify the ability to edit the user.
   * @param existingEmail The existing email for the user. This can be {@code null} if the email
   *                     will stay the same. It must be present if the email is to be changed.
   * @param user The User object with updated properties.
   * @return The User that was updated in the database.
   */
  @PUT
  public Response updateUser(@Auth Key key,
                             @HeaderParam("password") String password,
                             @QueryParam("email") String existingEmail,
                             User user) {
    updateRequests.mark();

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
    if (!hashService.isMatch(password, foundUser.getPassword())) {
      LOG.error("The password for user {} was incorrect.", email);
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity("Unable to validate user with provided credentials.").build();
    }

    User result;

    try {
      result = usersDao.update(existingEmail, user);
    } catch (DatabaseException e) {
      LOG.error("Error updating user {} in database. Caused by: {}", email, e.getErrorKind());
      return e.getErrorKind().buildResponse(user.getEmail().getAddress());
    }

    LOG.info("Successfully updated user {}.", email);
    return Response.ok(result).build();
  }

  /**
   * Retrieves a User from the database.
   *
   * @param key The basic authentication key necessary to access the resource.
   * @param password The password of the user to fetch. Used to verify authentication.
   * @param email The email of the user to retrieve.
   * @return The User that was found in the database.
   */
  @GET
  public Response getUser(@Auth Key key,
                          @HeaderParam("password") String password,
                          @QueryParam("email") String email) {
    getRequests.mark();

    try {
      requestValidator.validate(password, email);
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
    if (!hashService.isMatch(password, user.getPassword())) {
      LOG.error("The password for user {} was incorrect.", email);
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity("Unable to validate user with provided credentials.").build();
    }

    LOG.info("Successfully retrieved user {}.", email);
    return Response.ok(user).build();
  }

  /**
   * Deletes a User from the database.
   *
   * @param key The basic authentication key necessary to access the resource.
   * @param password The password of the user to delete. Used to verify authentication.
   * @param email The email of the user to delete.
   * @return The user that was deleted from the database.
   */
  @DELETE
  public Response deleteUser(@Auth Key key,
                             @HeaderParam("password") String password,
                             @QueryParam("email") String email) {
    deleteRequests.mark();

    try {
      requestValidator.validate(password, email);
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
    if (!hashService.isMatch(password, user.getPassword())) {
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
