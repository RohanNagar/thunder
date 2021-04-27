package com.sanctionco.thunder.authentication.basic;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.sanctionco.thunder.TestResources;
import com.sanctionco.thunder.authentication.AuthConfiguration;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BasicAuthConfigurationTest {
  private static final YamlConfigurationFactory<AuthConfiguration> FACTORY =
      new YamlConfigurationFactory<>(
          AuthConfiguration.class, TestResources.VALIDATOR, TestResources.MAPPER, "dw");

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
  void testRegisterAuthentication() throws Exception {
    var environment = mock(Environment.class);
    var jersey = mock(JerseyEnvironment.class);

    when(environment.jersey()).thenReturn(jersey);
    when(environment.metrics()).thenReturn(TestResources.METRICS);

    AuthConfiguration configuration = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/auth/basic-auth-with-keys.yaml").toURI()));

    assertTrue(configuration instanceof BasicAuthConfiguration);

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
