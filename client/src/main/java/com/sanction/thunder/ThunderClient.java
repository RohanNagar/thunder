package com.sanction.thunder;

import com.sanction.thunder.models.PilotUser;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Query;

public interface ThunderClient {

  /**
   * Posts a PilotUser to the users database.
   *
   * @param user The user to create in the database.
   * @return The user that was created in the database.
   */
  @POST("/users")
  PilotUser postUser(@Body PilotUser user);

  /**
   * Updates a PilotUser in the users database.
   *
   * @param user The user to update with all fields updated.
   * @return The user that was updated in the database.
   */
  @PUT("/users")
  PilotUser updateUser(@Body PilotUser user);

  /**
   * Gets a PilotUser from the users database.
   *
   * @param username The name of the user to get from the database.
   * @return The user that was found in the database.
   */
  @GET("/users")
  PilotUser getUser(@Query("username") String username);

  /**
   * Deletes a PilotUser from the users database.
   *
   * @param username The name of the user to delete.
   * @return The user that was deleted from the database.
   */
  @DELETE("/users")
  PilotUser deleteUser(@Query("username") String username);
}
