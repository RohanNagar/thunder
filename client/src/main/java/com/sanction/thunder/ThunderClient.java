package com.sanction.thunder;

import com.sanction.thunder.models.PilotUser;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
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
   * @param existingEmail The existing email of the user.
   * @param password The password required to access the resource.
   * @return The user that was updated in the database.
   */
  @PUT("/users")
  PilotUser updateUser(@Body PilotUser user,
                       @Query("email") String existingEmail,
                       @Header("password") String password);

  /**
   * Gets a PilotUser from the users database.
   *
   * @param email The name of the user to get from the database.
   * @param password The password required to access the resource.
   * @return The user that was found in the database.
   */
  @GET("/users")
  PilotUser getUser(@Query("email") String email,
                    @Header("password") String password);

  /**
   * Deletes a PilotUser from the users database.
   *
   * @param email The name of the user to delete.
   * @param password The password required to access the resource.
   * @return The user that was deleted from the database.
   */
  @DELETE("/users")
  PilotUser deleteUser(@Query("email") String email,
                       @Header("password") String password);
}
