package com.sanction.thunder;

import com.sanction.thunder.models.StormUser;
import io.dropwizard.testing.junit.DropwizardClientRule;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ThunderClientTest {
  private static final StormUser user =
      new StormUser("username", "password", "fbaccess", "twaccess", "twsecret");

  /**
   * Resource to be used as a test double.
   */
  @Path("/users")
  @Produces(MediaType.APPLICATION_JSON)
  public static final class TestResource {

    @POST
    public Response postUser() {
      return Response.status(Response.Status.CREATED)
          .entity(user).build();
    }

    @PUT
    public Response updateUser() {
      return Response.status(Response.Status.OK)
          .entity(user).build();
    }

    @GET
    public Response getUser() {
      return Response.status(Response.Status.OK)
          .entity(user).build();
    }

    @DELETE
    public Response deleteUser() {
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
    StormUser response = client.postUser(user);
    assertEquals(user.getUsername(), response.getUsername());
  }

  @Test
  public void testUpdateUser() {
    StormUser response = client.updateUser(user);
    assertEquals(user.getUsername(), response.getUsername());
  }

  @Test
  public void testGetUser() {
    StormUser response = client.getUser("username");
    assertEquals(user.getUsername(), response.getUsername());
  }

  @Test
  public void testDeleteUser() {
    StormUser response = client.deleteUser("username");
    assertEquals(user.getUsername(), response.getUsername());
  }

}
