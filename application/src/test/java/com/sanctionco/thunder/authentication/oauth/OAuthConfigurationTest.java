package com.sanctionco.thunder.authentication.oauth;

import com.codahale.metrics.MetricRegistry;
import com.google.common.io.Resources;
import com.sanctionco.thunder.TestResources;
import com.sanctionco.thunder.authentication.AuthConfiguration;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.configuration.ConfigurationValidationException;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OAuthConfigurationTest {
  private static final YamlConfigurationFactory<AuthConfiguration> FACTORY =
      new YamlConfigurationFactory<>(
          AuthConfiguration.class, TestResources.VALIDATOR, TestResources.MAPPER, "dw");

  @Test
  void testOauthFromYamlNoAudience() throws Exception {
    AuthConfiguration configuration = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/auth/oauth-no-audience.yaml").toURI()));

    assertTrue(configuration instanceof OAuthConfiguration);

    var oauthConfiguration = (OAuthConfiguration) configuration;

    assertEquals("test-secret", oauthConfiguration.getHmacSecret());
    assertEquals("thunder", oauthConfiguration.getIssuer());
    assertNull(oauthConfiguration.getAudience());
    assertNull(oauthConfiguration.getRsaPublicKey());
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
    assertNull(oauthConfiguration.getRsaPublicKey());
  }

  @Test
  void testOauthFromYamlWithRsaPublicKey() throws Exception {
    AuthConfiguration configuration = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/auth/oauth-with-rsa.yaml").toURI()));

    assertTrue(configuration instanceof OAuthConfiguration);

    var oauthConfiguration = (OAuthConfiguration) configuration;

    assertEquals("thunder", oauthConfiguration.getIssuer());
    assertNotNull(oauthConfiguration.getRsaPublicKey());
    assertEquals("RSA", oauthConfiguration.getRsaPublicKey().getAlgorithm());

    assertNull(oauthConfiguration.getHmacSecret());
    assertNull(oauthConfiguration.getAudience());
  }

  @Test
  void testInvalidOauthConfig() {
    ConfigurationValidationException e = assertThrows(ConfigurationValidationException.class,
        () -> FACTORY.build(new File(Resources.getResource(
            "fixtures/configuration/auth/oauth-with-no-keys.yaml").toURI())));

    assertTrue(e.getMessage().contains("hmacSecret or rsaPublicKeyFilePath must be set"));
  }

  @Test
  void testBadFilePathThrows() throws Exception {
    AuthConfiguration configuration = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/auth/oauth-with-bad-rsa-file.yaml").toURI()));

    assertTrue(configuration instanceof OAuthConfiguration);

    var oauthConfiguration = (OAuthConfiguration) configuration;

    RuntimeException e = assertThrows(RuntimeException.class, oauthConfiguration::getRsaPublicKey);

    assertTrue(e.getMessage().contains("Unable to read RSA public key"));
  }

  @Test
  void testRegisterAuthentication() throws Exception {
    var environment = mock(Environment.class);
    var jersey = mock(JerseyEnvironment.class);
    var metrics = mock(MetricRegistry.class);

    when(environment.jersey()).thenReturn(jersey);
    when(environment.metrics()).thenReturn(metrics);

    AuthConfiguration configuration = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/auth/oauth-with-audience.yaml").toURI()));

    assertTrue(configuration instanceof OAuthConfiguration);

    configuration.registerAuthentication(environment);

    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    verify(jersey, atLeastOnce()).register(captor.capture());

    List<Object> values = captor.getAllValues();
    assertAll("Assert all objects were registered to Jersey",
        () -> assertEquals(1,
            values.stream().filter(v -> v instanceof AuthDynamicFeature).count()),
        () -> assertEquals(1,
            values.stream().filter(v -> v instanceof AuthValueFactoryProvider.Binder).count()));

    verify(metrics, times(1)).timer(anyString());
    verify(metrics, times(2)).counter(anyString());
  }

  @Test
  void testRegisterAuthenticationOnlyRsa() throws Exception {
    var environment = mock(Environment.class);
    var jersey = mock(JerseyEnvironment.class);
    var metrics = mock(MetricRegistry.class);

    when(environment.jersey()).thenReturn(jersey);
    when(environment.metrics()).thenReturn(metrics);

    AuthConfiguration configuration = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/auth/oauth-with-rsa.yaml").toURI()));

    assertTrue(configuration instanceof OAuthConfiguration);

    configuration.registerAuthentication(environment);

    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    verify(jersey, atLeastOnce()).register(captor.capture());

    List<Object> values = captor.getAllValues();
    assertAll("Assert all objects were registered to Jersey",
        () -> assertEquals(1,
            values.stream().filter(v -> v instanceof AuthDynamicFeature).count()),
        () -> assertEquals(1,
            values.stream().filter(v -> v instanceof AuthValueFactoryProvider.Binder).count()));
  }
}
