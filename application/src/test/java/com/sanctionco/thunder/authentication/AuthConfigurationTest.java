package com.sanctionco.thunder.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.sanctionco.thunder.authentication.basic.BasicAuthConfiguration;
import com.sanctionco.thunder.authentication.basic.Key;
import com.sanctionco.thunder.authentication.oauth.OAuthConfiguration;

import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;

import java.io.File;

import javax.validation.Validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthConfigurationTest {
  private static final ObjectMapper OBJECT_MAPPER = Jackson.newObjectMapper();
  private static final Validator VALIDATOR = Validators.newValidator();
  private static final YamlConfigurationFactory<AuthConfiguration> FACTORY =
      new YamlConfigurationFactory<>(AuthConfiguration.class, VALIDATOR, OBJECT_MAPPER, "dw");

  @Test
  void isDiscoverable() {
    // Make sure the types we specified in META-INF gets picked up
    assertTrue(new DiscoverableSubtypeResolver().getDiscoveredSubtypes()
        .contains(BasicAuthConfiguration.class));
    assertTrue(new DiscoverableSubtypeResolver().getDiscoveredSubtypes()
        .contains(OAuthConfiguration.class));
  }

  @Test
  void testFromYamlNoKeys() throws Exception {
    AuthConfiguration configuration = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/auth/basic-auth-no-keys.yaml").toURI()));

    assertTrue(configuration instanceof BasicAuthConfiguration);

    var basicAuthConfiguration = (BasicAuthConfiguration) configuration;

    assertEquals(0, basicAuthConfiguration.getKeys().size());
  }

  @Test
  void testFromYamlWithKeys() throws Exception {
    AuthConfiguration configuration = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/auth/basic-auth-with-keys.yaml").toURI()));

    assertTrue(configuration instanceof BasicAuthConfiguration);

    var basicAuthConfiguration = (BasicAuthConfiguration) configuration;

    assertEquals(2, basicAuthConfiguration.getKeys().size());
    assertEquals(
        Lists.newArrayList(
            new Key("app1", "secret1"),
            new Key("app2", "secret2")),
        basicAuthConfiguration.getKeys());
  }

  @Test
  void testOauthFromYamlNoAudience() throws Exception {
    AuthConfiguration configuration = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/auth/oauth-no-audience.yaml").toURI()));

    assertTrue(configuration instanceof OAuthConfiguration);

    var oauthConfiguration = (OAuthConfiguration) configuration;

    assertEquals("test-secret", oauthConfiguration.getHmacSecret());
    assertEquals("thunder", oauthConfiguration.getIssuer());
    assertNull(oauthConfiguration.getAudience());
  }

  @Test
  void testOauthFromYamlWithAudience() throws Exception {
    AuthConfiguration configuration = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/auth/oauth-with-audience.yaml").toURI()));

    assertTrue(configuration instanceof OAuthConfiguration);

    var oauthConfiguration = (OAuthConfiguration) configuration;

    assertEquals("test-secret", oauthConfiguration.getHmacSecret());
    assertEquals("thunder", oauthConfiguration.getIssuer());
    assertEquals("customAudience", oauthConfiguration.getAudience());
  }
}
