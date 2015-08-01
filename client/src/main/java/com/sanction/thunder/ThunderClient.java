package com.sanction.thunder;

import com.sanction.thunder.models.StormUser;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

public interface ThunderClient {

  /**
   * Posts a StormUser to the users database.
   *
   * @param user The user to create in the database.
   * @return True if the user was posted successfully, false otherwise.
   */
  @POST("/users")
  boolean postUser(@Body StormUser user);

  /**
   * Gets a StormUser from the users database.
   *
   * @param username The name of the user to get from the database.
   * @return The user that was found in the database.
   */
  @GET("users")
  StormUser getUser(@Query("username") String username);
}
