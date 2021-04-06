package com.sanctionco.thunder;

import com.sanctionco.thunder.models.ResponseType;
import com.sanctionco.thunder.models.User;

import java.util.concurrent.CompletableFuture;

import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * Provides methods to interact with Thunder over HTTP requests. Construct an instance
 * of this class using {@link ThunderClientBuilder}.
 */
public interface ThunderClient {

  /**
   * Creates the user in the user database.
   *
   * @param user the user to create
   * @return a {@link CompletableFuture} that holds the created user after the request completes
   */
  @POST("users")
  CompletableFuture<User> postUser(@Body User user);

  /**
   * Updates the user in the user database.
   *
   * @param user the user to update with all fields updated
   * @param existingEmail the existing email of the user. This may be {@code null} if the user's
   *                      email is not being updated.
   * @param password the user's password
   * @return a {@link CompletableFuture} that holds the updated user after the request completes
   */
  @PUT("users")
  CompletableFuture<User> updateUser(@Body User user,
                                     @Query("email") String existingEmail,
                                     @Header("password") String password);

  /**
   * Gets the user with the given email address from the user database.
   *
   * @param email the user's email address
   * @param password the user's password
   * @return a {@link CompletableFuture} that holds the user after the request completes
   */
  @GET("users")
  CompletableFuture<User> getUser(@Query("email") String email,
                                  @Header("password") String password);

  /**
   * Deletes the user with the given email address from the user database.
   *
   * @param email the user's email address
   * @param password the user's password
   * @return a {@link CompletableFuture} that holds the deleted user after the request completes
   */
  @DELETE("users")
  CompletableFuture<User> deleteUser(@Query("email") String email,
                                     @Header("password") String password);

  /**
   * Sends a verification email to the user with the given email address.
   *
   * @param email the user's email address
   * @param password the user's password
   * @return a {@link CompletableFuture} that holds the user after the request completes
   */
  @POST("verify")
  CompletableFuture<User> sendVerificationEmail(@Query("email") String email,
                                                @Header("password") String password);

  /**
   * Verifies the user with the given email. This method will return the verified user in the
   * response. To get HTML in the response, see
   * {@link ThunderClient#verifyUser(String, String, ResponseType)}.
   *
   * @param email the user's email address
   * @param token the email verification token that was generated from sending an email
   * @return a {@link CompletableFuture} that holds the verified user after the request completes
   */
  @GET("verify")
  CompletableFuture<User> verifyUser(@Query("email") String email,
                                     @Query("token") String token);

  /**
   * Verifies the user with the given email. This method can return HTML in the response.
   * To get the verified user object in the response, see
   * {@link ThunderClient#verifyUser(String, String)}.
   *
   * @param email the user's email address
   * @param token the email verification token that was generated from sending an email
   * @param responseType the type of response to receive (HTML or JSON)
   * @return a {@link CompletableFuture} that holds the response after the request completes.
   *     The response will be an HTML string if responseType was set to HTML, or a JSON string if
   *     responseType was set to JSON.
   */
  @GET("verify")
  CompletableFuture<String> verifyUser(@Query("email") String email,
                                       @Query("token") String token,
                                       @Query("response_type") ResponseType responseType);

  /**
   * Resets the verification status of the user with the given email. This will set the verification
   * status of the user to false and reset the associated verification token.
   *
   * @param email the user's email address
   * @param password the user's password
   * @return a {@link CompletableFuture} that holds the updated user after the request completes
   */
  @POST("verify/reset")
  CompletableFuture<User> resetVerificationStatus(@Query("email") String email,
                                                  @Header("password") String password);
}
