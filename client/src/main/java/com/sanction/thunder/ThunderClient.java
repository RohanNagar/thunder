package com.sanction.thunder;

import com.sanction.thunder.models.StormUser;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Query;

public interface ThunderClient {

  /**
   * Posts a StormUser to the users database.
   *
   * @param user The user to create in the database.
   * @return The user that was created in the database.
   */
  @POST("/users")
  StormUser postUser(@Body StormUser user);

  /**
   * Updates a StormUser in the users database.
   *
   * @param user The user to update with all fields updated.
   * @return The user that was updated in the database.
   */
  @PUT("/users")
  StormUser updateUser(@Body StormUser user);

  /**
   * Gets a StormUser from the users database.
   *
   * @param username The name of the user to get from the database.
   * @return The user that was found in the database.
   */
  @GET("/users")
  StormUser getUser(@Query("username") String username);

  /**
   * Deletes a StormUser from the users database.
   *
   * @param username The name of the user to delete.
   * @return The user that was deleted from the database.
   */
  @DELETE("/users")
  StormUser deleteUser(@Query("username") String username);
}
