package com.sanctionco.thunder.authentication.basic;

import com.sanctionco.thunder.TestResources;
import com.sanctionco.thunder.authentication.AuthConfiguration;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jersey.setup.JerseyEnvironment;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BasicAuthConfigurationTest {

  @Test
  void testFromYamlNoKeys() {
    AuthConfiguration configuration = TestResources.readResourceYaml(
        AuthConfiguration.class,
        "fixtures/configuration/auth/basic-auth-no-keys.yaml");

    assertInstanceOf(BasicAuthConfiguration.class, configuration);

    var basicAuthConfiguration = (BasicAuthConfiguration) configuration;

    assertEquals(0, basicAuthConfiguration.getKeys().size());
  }

  @Test
  void testFromYamlWithKeys() {
    AuthConfiguration configuration = TestResources.readResourceYaml(
        AuthConfiguration.class,
        "fixtures/configuration/auth/basic-auth-with-keys.yaml");

    assertInstanceOf(BasicAuthConfiguration.class, configuration);

    var basicAuthConfiguration = (BasicAuthConfiguration) configuration;

    assertEquals(2, basicAuthConfiguration.getKeys().size());
    assertEquals(
        List.of(new Key("app1", "secret1"), new Key("app2", "secret2")),
        basicAuthConfiguration.getKeys());
  }

  @Test
  void testRegisterAuthentication() {
    var environment = mock(Environment.class);
    var jersey = mock(JerseyEnvironment.class);

    when(environment.jersey()).thenReturn(jersey);
    when(environment.metrics()).thenReturn(TestResources.METRICS);

    AuthConfiguration configuration = TestResources.readResourceYaml(
        AuthConfiguration.class,
        "fixtures/configuration/auth/basic-auth-with-keys.yaml");

    assertInstanceOf(BasicAuthConfiguration.class, configuration);

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
