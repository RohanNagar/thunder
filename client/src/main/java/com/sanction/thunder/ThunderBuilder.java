package com.sanction.thunder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import org.apache.commons.codec.binary.Base64;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.JacksonConverter;

import static com.google.common.base.Preconditions.checkNotNull;

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
    checkNotNull(endpoint);
    checkNotNull(apiUser);
    checkNotNull(apiSecret);

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new GuavaModule());

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
      checkNotNull(user);
      checkNotNull(secret);

      String token = Base64.encodeBase64String(String.format("%s:%s", user, secret).getBytes());
      authorization = "Basic " + token;
    }

    @Override
    public void intercept(RequestFacade request) {
      request.addHeader("Authorization", authorization);
    }
  }
}
