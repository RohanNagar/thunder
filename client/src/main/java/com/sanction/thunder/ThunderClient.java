package com.sanction.thunder;

import com.sanction.thunder.models.ResponseType;
import com.sanction.thunder.models.User;

import okhttp3.ResponseBody;

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
   * Posts a User to the users database.
   *
   * @param user The user to create in the database.
   * @return The user that was created in the database.
   */
  @POST("users")
  Call<User> postUser(@Body User user);

  /**
   * Updates a User in the users database.
   *
   * @param user The email address to update with all fields updated.
   * @param existingEmail The existing email of the user.
   * @param password The password of the user, required to access the resource.
   * @return The user that was updated in the database.
   */
  @PUT("users")
  Call<User> updateUser(@Body User user,
                        @Query("email") String existingEmail,
                        @Header("password") String password);

  /**
   * Gets a User from the users database.
   *
   * @param email The email address of the user to get from the database.
   * @param password The password of the user, required to access the resource.
   * @return The user that was found in the database.
   */
  @GET("users")
  Call<User> getUser(@Query("email") String email,
                     @Header("password") String password);

  /**
   * Deletes a User from the users database.
   *
   * @param email The email address of the user to delete.
   * @param password The password of the user, required to access the resource.
   * @return The user that was deleted from the database.
   */
  @DELETE("users")
  Call<User> deleteUser(@Query("email") String email,
                        @Header("password") String password);

  /**
   * Sends a verification email to the user.
   *
   * @param email The email address of the user to send the email to.
   * @param password The password of the user, required to access the resource.
   * @return The updated user object after generating a validation token and sending the email.
   */
  @POST("verify")
  Call<User> sendVerificationEmail(@Query("email") String email,
                                   @Header("password") String password);

  /**
   * Verifies an already created User with the given email.
   * Use this method to get a User back as the returned object.
   *
   * @param email The email address of the user to verify.
   * @param token The verification token of the user to verify.
   * @return The user that was successfully verified.
   */
  @GET("verify")
  Call<User> verifyUser(@Query("email") String email,
                        @Query("token") String token);

  /**
   * Verifies an already created User with the given email.
   * Use this method to get HTML back as the returned object.
   * If you want to get the verified User object back, use the
   * verifyUser() method without the responseType parameter.
   *
   * @param email The email address of the user to verify.
   * @param token The verification token of the user to verify.
   * @param responseType The type of response to receive (HTML or JSON).
   * @return The response in string form. This will be an HTML string if
   *            responseType was set to HTML, or a JSON string if responseType was set to JSON.
   */
  @GET("verify")
  Call<ResponseBody> verifyUser(@Query("email") String email,
                                @Query("token") String token,
                                @Query("response_type") ResponseType responseType);
}
