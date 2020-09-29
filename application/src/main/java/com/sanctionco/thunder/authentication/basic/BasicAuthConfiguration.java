package com.sanctionco.thunder.authentication.basic;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sanctionco.thunder.authentication.AuthConfiguration;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.setup.Environment;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Provides the HTTP Basic Authentication implementation of {@link AuthConfiguration}.
 */
@JsonTypeName("basic")
public class BasicAuthConfiguration implements AuthConfiguration {

  @NotNull
  @Valid
  @JsonProperty("keys")
  private final List<Key> keys = new ArrayList<>();

  public List<Key> getKeys() {
    return keys;
  }

  @Override
  public void registerAuthentication(Environment environment) {
    var authenticator = new ThunderAuthenticator(keys);

    environment.jersey().register(new AuthDynamicFeature(
        new BasicCredentialAuthFilter.Builder<Key>()
            .setAuthenticator(authenticator)
            .setRealm("THUNDER - AUTHENTICATION")
            .buildAuthFilter()));

    environment.jersey().register(new AuthValueFactoryProvider.Binder<>(Key.class));
  }
}
