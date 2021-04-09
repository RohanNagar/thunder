package com.sanctionco.thunder.authentication.basic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.sanctionco.thunder.authentication.AuthConfiguration;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.setup.Environment;

import java.io.File;
import java.util.List;

import javax.validation.Validator;

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
  private static final ObjectMapper OBJECT_MAPPER = Jackson.newObjectMapper();
  private static final Validator VALIDATOR = Validators.newValidator();
  private static final YamlConfigurationFactory<AuthConfiguration> FACTORY =
      new YamlConfigurationFactory<>(AuthConfiguration.class, VALIDATOR, OBJECT_MAPPER, "dw");

  @Test
  void testRegisterAuthentication() throws Exception {
    var environment = mock(Environment.class);
    var jersey = mock(JerseyEnvironment.class);

    when(environment.jersey()).thenReturn(jersey);

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
