package com.sanctionco.thunder;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Provides methods to build a new instance of {@link ThunderClient}.
 *
 * @see ThunderClient
 */
public class ThunderBuilder {
  private final Retrofit retrofit;

  /**
   * Constructs a builder instance that will be connect to the specified endpoint and use the
   * specified API key information.
   *
   * @param endpoint the base URL of the API endpoint to connect to. The URL must end in '/'.
   * @param apiUser the basic authentication username to use when connecting to the endpoint
   * @param apiSecret the basic authentication secret to use when connecting to the endpoint
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
   * Builds an instance of ThunderClient.
   */
  public ThunderClient newThunderClient() {
    return retrofit.create(ThunderClient.class);
  }

  /**
   * Creates a new HttpClient that injects basic authorization into incoming requests.
   *
   * @param user the basic authentication username to use when connecting to the endpoint
   * @param secret The basic authentication secret to use when connecting to the endpoint
   * @return the built OkHttpClient
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
