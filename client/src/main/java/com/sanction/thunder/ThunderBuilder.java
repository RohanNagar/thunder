package com.sanction.thunder;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Objects;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.JacksonConverter;


public class ThunderBuilder {
  private final RestAdapter adapter;

  /**
   * Construct a builder connected to the specified endpoint.
   *
   * @param endpoint  The base URL of the API endpoint to connect to.
   * @param apiUser   The API username to use when connecting to the endpoint.
   * @param apiSecret The API secret to use when connecting to the endpoint.
   */
  public ThunderBuilder(String endpoint, String apiUser, String apiSecret) {
    Objects.requireNonNull(endpoint);
    Objects.requireNonNull(apiUser);
    Objects.requireNonNull(apiSecret);

    ObjectMapper mapper = new ObjectMapper();

    adapter = new RestAdapter.Builder()
        .setEndpoint(endpoint)
        .setConverter(new JacksonConverter(mapper))
        .setRequestInterceptor(new ApiKeyInterceptor(apiUser, apiSecret))
        .build();
  }

  /**
   * Build an instance of a ThunderClient.
   */
  public ThunderClient newThunderClient() {
    return adapter.create(ThunderClient.class);
  }

  private static final class ApiKeyInterceptor implements RequestInterceptor {
    private final String authorization;

    ApiKeyInterceptor(String user, String secret) {
      Objects.requireNonNull(user);
      Objects.requireNonNull(secret);

      String token = Base64.getEncoder()
          .encodeToString(String.format("%s:%s", user, secret).getBytes());

      authorization = "Basic " + token;
    }

    @Override
    public void intercept(RequestFacade request) {
      request.addHeader("Authorization", authorization);
    }
  }
}
