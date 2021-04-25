package com.sanctionco.thunder.authentication.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sanctionco.thunder.authentication.AuthConfiguration;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.setup.Environment;

import java.security.Principal;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Provides the OAuth2 Authentication implementation of {@link AuthConfiguration}.
 */
@JsonTypeName("oauth")
public class OAuthConfiguration implements AuthConfiguration {
  private static final String DEFAULT_AUDIENCE = "thunder";

  @NotNull @Valid @JsonProperty("hmacSecret")
  private final String hmacSecret = null;

  public String getHmacSecret() {
    return hmacSecret;
  }

  @NotNull @Valid @JsonProperty("issuer")
  private final String issuer = null;

  public String getIssuer() {
    return issuer;
  }

  @Valid @JsonProperty("audience")
  private final String audience = null;

  public String getAudience() {
    return Optional.ofNullable(audience)
        .orElse(DEFAULT_AUDIENCE);
  }

  @Override
  public void registerAuthentication(Environment environment) {
    var authenticator = new OAuthAuthenticator(hmacSecret, issuer, audience, environment.metrics());

    environment.jersey().register(new AuthDynamicFeature(
        new OAuthCredentialAuthFilter.Builder<>()
            .setAuthenticator(authenticator)
            .setPrefix("Bearer")
            .buildAuthFilter()));

    environment.jersey().register(new AuthValueFactoryProvider.Binder<>(Principal.class));
  }
}
