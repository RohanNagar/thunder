package com.sanction.thunder;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class ThunderBuilder {
  private final Retrofit retrofit;

  /**
   * Construct a builder connected to the specified endpoint.
   *
   * @param endpoint The base URL of the API endpoint to connect to. Must end in '/'.
   * @param apiUser The API username to use when connecting to the endpoint.
   * @param apiSecret The API secret to use when connecting to the endpoint.
   */
  public ThunderBuilder(String endpoint, String apiUser, String apiSecret) {
    Objects.requireNonNull(endpoint);
    Objects.requireNonNull(apiUser);
    Objects.requireNonNull(apiSecret);

    retrofit = new Retrofit.Builder()
      .baseUrl(endpoint)
      .addConverterFactory(JacksonConverterFactory.create())
      .client(buildHttpClient(apiUser, apiSecret))
      .build();
  }

  /**
   * Build an instance of a ThunderClient.
   */
  public ThunderClient newThunderClient() {
    return retrofit.create(ThunderClient.class);
  }

  /**
   * Create a new HttpClient that injects authorization into the client.
   *
   * @param user The API username to use when connecting to the endpoint.
   * @param secret The API secret to use when connecting to the endpoint.
   * @return The built OkHttpClient.
   */
  private OkHttpClient buildHttpClient(String user, String secret) {
    Objects.requireNonNull(user);
    Objects.requireNonNull(secret);

    String token = Base64.getEncoder()
        .encodeToString(String.format("%s:%s", user, secret).getBytes(Charset.forName("UTF-8")));

    String authorization = "Basic " + token;

    OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    httpClient.addInterceptor((chain) -> {
      Request original = chain.request();

      // Add the authorization header
      Request request = original.newBuilder()
          .header("Authorization", authorization)
          .method(original.method(), original.body())
          .build();

      return chain.proceed(request);
    });

    return httpClient.build();
  }
}
