package com.sanction.thunder;

import com.sanction.thunder.models.PilotUser;
import io.dropwizard.testing.junit.DropwizardClientRule;

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
import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ThunderClientTest {
  private static final PilotUser user =
      new PilotUser("email", "password", "dbaccess", "fbaccess", "twaccess", "twsecret");
  private static final String password = "password";

  /**
   * Resource to be used as a test double. Requests from the ThunderClient interface
   * will be directed here for the unit tests. Use this to verify that all parameters are
   * being set correctly.
   */
  @Path("/users")
  @Produces(MediaType.APPLICATION_JSON)
  public static final class TestResource {

    /**
     * Sample postUser method. The user object must be present.
     */
    @POST
    public Response postUser(PilotUser user) {
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
    public Response updateUser(@HeaderParam("password") String password, PilotUser user) {
      if (password == null || password.equals("") || user == null) {
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
    public Response getUser(@QueryParam("email") String email,
                            @HeaderParam("password") String password) {
      if (email == null || email.equals("") || password == null || password.equals("")) {
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
    public Response deleteUser(@QueryParam("email") String email,
                               @HeaderParam("password") String password) {
      if (email == null || email.equals("") || password == null || password.equals("")) {
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(null).build();
      }

      return Response.status(Response.Status.OK)
          .entity(user).build();
    }
  }

  @ClassRule
  public static final DropwizardClientRule dropwizard
      = new DropwizardClientRule(new TestResource());

  private final ThunderBuilder builder =
      new ThunderBuilder(dropwizard.baseUri().toString(), "userKey", "userSecret");
  private final ThunderClient client = builder.newThunderClient();

  @Test
  public void testPostUser() {
    PilotUser response = client.postUser(user);
    assertEquals(user.getEmail(), response.getEmail());
  }

  @Test
  public void testUpdateUser() {
    PilotUser response = client.updateUser(user, password);
    assertEquals(user.getEmail(), response.getEmail());
  }

  @Test
  public void testGetUser() {
    PilotUser response = client.getUser("email", password);
    assertEquals(user.getEmail(), response.getEmail());
  }

  @Test
  public void testDeleteUser() {
    PilotUser response = client.deleteUser("email", password);
    assertEquals(user.getEmail(), response.getEmail());
  }

}
