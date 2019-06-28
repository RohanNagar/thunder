package com.sanctionco.thunder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.ResponseType;
import com.sanctionco.thunder.models.User;

import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.junit5.DropwizardClientExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import java.io.IOException;
import java.util.Collections;

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

import okhttp3.ResponseBody;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
  @Produces(MediaType.APPLICATION_JSON)
  public static final class TestResource {

    /**
     * Sample postUser method. The user object must be present.
     */
    @POST
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

  private final ThunderBuilder builder =
      new ThunderBuilder(extension.baseUri().toString(), "userKey", "userSecret");
  private final ThunderClient client = builder.newThunderClient();

  @Test
  @SuppressWarnings("ConstantConditions")
  void testPostUser() throws IOException {
    User response = client.postUser(user)
        .execute()
        .body();

    assertEquals(user.getEmail(), response.getEmail());
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  void testUpdateUser() throws IOException {
    User response = client.updateUser(user, "email", password)
        .execute()
        .body();

    assertEquals(user.getEmail(), response.getEmail());
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  void testGetUser() throws IOException {
    User response = client.getUser("email", password)
        .execute()
        .body();

    assertEquals(user.getEmail(), response.getEmail());
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  void testDeleteUser() throws IOException {
    User response = client.deleteUser("email", password)
        .execute()
        .body();

    assertEquals(user.getEmail(), response.getEmail());
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  void testSendVerificationEmail() throws IOException {
    User response = client.sendVerificationEmail("email", password)
        .execute()
        .body();

    assertEquals(user.getEmail(), response.getEmail());
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  void testVerifyUser() throws IOException {
    User response = client.verifyUser("email", "token")
        .execute()
        .body();

    assertEquals(user.getEmail(), response.getEmail());
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  void testVerifyUserHtml() throws IOException {
    ResponseBody response = client.verifyUser("email", "token", ResponseType.HTML)
        .execute()
        .body();

    assertEquals("HTML Here", response.string());
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  void testVerifyUserJson() throws IOException {
    ResponseBody response = client.verifyUser("email", "token", ResponseType.JSON)
        .execute()
        .body();

    assertEquals(user, MAPPER.readValue(response.string(), User.class));
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  void testResetVerificationStatus() throws IOException {
    User response = client.resetVerificationStatus("email", "password")
        .execute()
        .body();

    assertEquals(user.getEmail(), response.getEmail());
  }

  @Test
  void testResetVerificationStatusNull() throws IOException {
    User response = client.resetVerificationStatus(null, "password")
        .execute()
        .body();

    assertNull(response);

    response = client.resetVerificationStatus("email", null)
        .execute()
        .body();

    assertNull(response);
  }
}
