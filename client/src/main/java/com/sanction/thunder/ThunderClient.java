package com.sanction.thunder;

import com.sanction.thunder.models.PilotUser;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ThunderClient {

  /**
   * Posts a PilotUser to the users database.
   *
   * @param user The user to create in the database.
   * @return The user that was created in the database.
   */
  @POST("users")
  Call<PilotUser> postUser(@Body PilotUser user);

  /**
   * Updates a PilotUser in the users database.
   *
   * @param user The email address to update with all fields updated.
   * @param existingEmail The existing email of the user.
   * @param password The password of the user, required to access the resource.
   * @return The user that was updated in the database.
   */
  @PUT("users")
  Call<PilotUser> updateUser(@Body PilotUser user,
                             @Query("email") String existingEmail,
                             @Header("password") String password);

  /**
   * Gets a PilotUser from the users database.
   *
   * @param email The email address of the user to get from the database.
   * @param password The password of the user, required to access the resource.
   * @return The user that was found in the database.
   */
  @GET("users")
  Call<PilotUser> getUser(@Query("email") String email,
                          @Header("password") String password);

  /**
   * Deletes a PilotUser from the users database.
   *
   * @param email The email address of the user to delete.
   * @param password The password of the user, required to access the resource.
   * @return The user that was deleted from the database.
   */
  @DELETE("users")
  Call<PilotUser> deleteUser(@Query("email") String email,
                             @Header("password") String password);

  /**
   * Sends a verification email to the user.
   *
   * @param email The email address of the user to send the email to.
   * @param password The password of the user, required to access the resource.
   * @return The updated user object after generating a validation token and sending the email.
   */
  @POST("verify")
  Call<PilotUser> sendVerificationEmail(@Query("email") String email,
                                        @Header("password") String password);

  /**
   * Verifies an already created PilotUser with the given email.
   *
   * @param email The email address of the user to verify.
   * @param token The verification token of the user to verify.
   * @return The user that was successfully verified.
   */
  @GET("verify")
  Call<PilotUser> verifyUser(@Query("email") String email,
                             @Query("token") String token);
}
