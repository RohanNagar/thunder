package com.sanctionco.thunder;

import java.nio.charset.StandardCharsets;
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
public class ThunderClientBuilder {
  private String endpoint;
  private OkHttpClient httpClient;

  /**
   * Constructs a new builder instance that can be configured to build a {@link ThunderClient}.
   */
  public ThunderClientBuilder() {
  }

  /**
   * Sets the endpoint to use when making calls to Thunder.
   *
   * @param endpoint the base URL of the API endpoint to connect to
   * @return this
   */
  public ThunderClientBuilder endpoint(String endpoint) {
    Objects.requireNonNull(endpoint);

    this.endpoint = ensureTrailingSlashExists(endpoint);

    return this;
  }

  /**
   * Sets basic authentication information for this client to use.
   *
   * @param apiUser the basic authentication username to use when connecting to the endpoint
   * @param apiSecret the basic authentication secret to use when connecting to the endpoint
   * @return this
   */
  public ThunderClientBuilder authentication(String apiUser, String apiSecret) {
    Objects.requireNonNull(apiUser);
    Objects.requireNonNull(apiSecret);

    this.httpClient = buildHttpClient(apiUser, apiSecret);

    return this;
  }

  /**
   * Builds an instance of {@link ThunderClient}.
   *
   * @return the new {@link ThunderClient} instance
   */
  public ThunderClient build() {
    Objects.requireNonNull(endpoint,
        "You must provide an endpoint with the ThunderClientBuilder.endpoint() method"
            + " in order to build a ThunderClient.");
    Objects.requireNonNull(httpClient,
        "You must provide an authentication mechanism with the"
            + " ThunderClientBuilder.authentication() method in order to build a ThunderClient.");

    var retrofit = new Retrofit.Builder()
        .baseUrl(endpoint)
        .addConverterFactory(JacksonConverterFactory.create())
        .client(httpClient)
        .build();

    return retrofit.create(ThunderClient.class);
  }

  /**
   * Creates a new HttpClient that injects basic authorization into incoming requests.
   *
   * @param user the basic authentication username to use when connecting to the endpoint
   * @param secret the basic authentication secret to use when connecting to the endpoint
   * @return the built OkHttpClient
   */
  private OkHttpClient buildHttpClient(String user, String secret) {
    String token = Base64.getEncoder()
        .encodeToString(String.format("%s:%s", user, secret).getBytes(StandardCharsets.UTF_8));

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

  /**
   * Ensures that the given URL ends with a trailing slash ('/').
   *
   * @param url the url to verify contains a trailing slash
   * @return the original url with a trailing slash added if necessary
   */
  static String ensureTrailingSlashExists(String url) {
    return url.endsWith("/")
        ? url
        : url + "/";
  }
}
