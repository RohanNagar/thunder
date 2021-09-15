package com.sanctionco.thunder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.ResponseType;
import com.sanctionco.thunder.models.User;

import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.junit5.DropwizardClientExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import retrofit2.HttpException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(DropwizardExtensionsSupport.class)
class ThunderClientTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
  private static final Email email = new Email("test@test.com", true, "hashToken");
  private static final String password = "password";
  private static final User user = new User(email, password, Collections.emptyMap());

  /**
   * Resource to be used as a test double. Requests from the ThunderClient interface
   * will be directed here for the unit tests. Use this to verify that all parameters are
   * being set correctly.
   */
  @Path("/")
  @TestDouble
  @Produces(MediaType.APPLICATION_JSON)
  public static final class TestResource {

    /**
     * Sample postUser method. The user object must be present.
     */
    @POST
    @TestDouble
    @Path("users")
    public Response postUser(User user) {
      if (user == null) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(null).build();
      }

      return Response.status(Response.Status.CREATED)
          .entity(user).build();
    }

    /**
     * Sample updateUser method. The password and user object must be present.
     */
    @PUT
    @TestDouble
    @Path("users")
    public Response updateUser(@QueryParam("email") String existingEmail,
                               @HeaderParam("password") String password,
                               User user) {
      if (password == null || password.isEmpty() || user == null) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(null).build();
      }

      return Response.status(Response.Status.OK)
          .entity(user).build();
    }

    /**
     * Sample getUser method. The email and password must be present.
     */
    @GET
    @TestDouble
    @Path("users")
    public Response getUser(@QueryParam("email") String email,
                            @HeaderParam("password") String password) {
      if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(null).build();
      }

      return Response.status(Response.Status.OK)
          .entity(user).build();
    }

    /**
     * Sample deleteUser method. The email and password must be present.
     */
    @DELETE
    @TestDouble
    @Path("users")
    public Response deleteUser(@QueryParam("email") String email,
                               @HeaderParam("password") String password) {
      if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(null).build();
      }

      return Response.status(Response.Status.OK)
          .entity(user).build();
    }

    /**
     * Sample sendEmail method. The email and password must be present.
     */
    @POST
    @TestDouble
    @Path("verify")
    public Response sendEmail(@QueryParam("email") String email,
                              @HeaderParam("password") String password) {
      if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(null).build();
      }

      return Response.status(Response.Status.OK)
          .entity(user).build();
    }

    /**
     * Sample verifyUser method. The email and token must be present.
     */
    @GET
    @TestDouble
    @Path("verify")
    public Response verifyUser(@QueryParam("email") String email,
                               @QueryParam("token") String token,
                               @QueryParam("response_type") @DefaultValue("json")
                                       ResponseType responseType) {
      if (email == null || email.isEmpty() || token == null || token.isEmpty()) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(null).build();
      }

      if (responseType.equals(ResponseType.HTML)) {
        return Response.ok("HTML Here").build();
      }

      return Response.status(Response.Status.OK)
          .entity(user).build();
    }

    /**
     * Sample resetVerificationStatus method. The email and password must be present.
     */
    @POST
    @TestDouble
    @Path("verify/reset")
    public Response resetVerificationStatus(@QueryParam("email") String email,
                                            @HeaderParam("password") String password) {
      if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(null).build();
      }

      return Response.status(Response.Status.OK)
          .entity(user).build();
    }
  }

  private static final DropwizardClientExtension extension =
      new DropwizardClientExtension(TestResource.class);

  private final ThunderClient client = new ThunderClientBuilder()
      .endpoint(extension.baseUri().toString())
      .authentication("userKey", "userSecret")
      .build();

  @Test
  void testPostUser() throws Exception {
    User response = client.postUser(user).get();

    assertEquals(user.getEmail(), response.getEmail());
  }

  @Test
  void testUpdateUser() throws Exception {
    User response = client.updateUser(user, "email", password).get();

    assertEquals(user.getEmail(), response.getEmail());
  }

  @Test
  void testGetUser() throws Exception {
    User response = client.getUser("email", password).get();

    assertEquals(user.getEmail(), response.getEmail());
  }

  @Test
  void testDeleteUser() throws Exception {
    User response = client.deleteUser("email", password).get();

    assertEquals(user.getEmail(), response.getEmail());
  }

  @Test
  void testSendVerificationEmail() throws Exception {
    User response = client.sendVerificationEmail("email", password).get();

    assertEquals(user.getEmail(), response.getEmail());
  }

  @Test
  void testVerifyUser() throws Exception {
    User response = client.verifyUser("email", "token").get();

    assertEquals(user.getEmail(), response.getEmail());
  }

  @Test
  void testVerifyUserHtml() throws Exception {
    String response = client.verifyUser("email", "token", ResponseType.HTML).get();

    assertEquals("HTML Here", response);
  }

  @Test
  void testVerifyUserJson() throws Exception {
    String response = client.verifyUser("email", "token", ResponseType.JSON).get();

    assertEquals(user, MAPPER.readValue(response, User.class));
  }

  @Test
  void testResetVerificationStatus() throws Exception {
    User response = client.resetVerificationStatus("email", "password").get();

    assertEquals(user.getEmail(), response.getEmail());
  }

  @Test
  void testResetVerificationStatusNull() {
    ExecutionException exception = assertThrows(ExecutionException.class,
        () -> client.resetVerificationStatus(null, "password").get());

    assertAll(
        () -> assertTrue(exception.getCause() instanceof HttpException),
        () -> assertTrue(exception.getMessage().contains("400 Bad Request")));

    ExecutionException secondException = assertThrows(ExecutionException.class,
        () -> client.resetVerificationStatus("email", null).get());

    assertAll(
        () -> assertTrue(secondException.getCause() instanceof HttpException),
        () -> assertTrue(secondException.getMessage().contains("400 Bad Request")));
  }
}
