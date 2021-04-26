package com.sanctionco.thunder.authentication.basic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sanctionco.thunder.authentication.AuthConfiguration;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.setup.Environment;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Provides the HTTP Basic Authentication implementation of {@link AuthConfiguration}.
 */
@JsonTypeName("basic")
public class BasicAuthConfiguration implements AuthConfiguration {

  @NotNull @Valid @JsonProperty("keys")
  private final List<Key> keys = Collections.emptyList();

  public List<Key> getKeys() {
    return keys;
  }

  @Override
  public void registerAuthentication(Environment environment) {
    var authenticator = new BasicAuthenticator(keys, environment.metrics());

    environment.jersey().register(new AuthDynamicFeature(
        new BasicCredentialAuthFilter.Builder<>()
            .setAuthenticator(authenticator)
            .setRealm("THUNDER - AUTHENTICATION")
            .buildAuthFilter()));

    environment.jersey().register(new AuthValueFactoryProvider.Binder<>(Principal.class));
  }
}
