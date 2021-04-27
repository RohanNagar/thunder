package com.sanctionco.thunder.authentication;

import com.sanctionco.thunder.authentication.basic.BasicAuthConfiguration;
import com.sanctionco.thunder.authentication.oauth.OAuthConfiguration;

import io.dropwizard.jackson.DiscoverableSubtypeResolver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthConfigurationTest {

  @Test
  void isDiscoverable() {
    // Make sure the types we specified in META-INF gets picked up
    assertTrue(new DiscoverableSubtypeResolver().getDiscoveredSubtypes()
        .contains(BasicAuthConfiguration.class));
    assertTrue(new DiscoverableSubtypeResolver().getDiscoveredSubtypes()
        .contains(OAuthConfiguration.class));
  }
}
