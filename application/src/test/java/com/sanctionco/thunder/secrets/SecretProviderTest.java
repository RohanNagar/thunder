package com.sanctionco.thunder.secrets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.sanctionco.thunder.secrets.local.LocalSecretProvider;

import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;

import java.io.File;
import java.util.List;
import java.util.Optional;

import javax.validation.Validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecretProviderTest {
  private static final ObjectMapper OBJECT_MAPPER = Jackson.newObjectMapper();
  private static final Validator VALIDATOR = Validators.newValidator();
  private static final YamlConfigurationFactory<SecretProvider> FACTORY =
      new YamlConfigurationFactory<>(SecretProvider.class, VALIDATOR, OBJECT_MAPPER, "dw");

  @Test
  void isDiscoverable() {
    // Make sure the types we specified in META-INF gets picked up
    assertTrue(new DiscoverableSubtypeResolver().getDiscoveredSubtypes()
        .contains(LocalSecretProvider.class));
  }

  @Test
  void testDynamoDbFromYaml() throws Exception {
    SecretProvider secretProvider = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/secrets/local-config.yaml").toURI()));

    assertTrue(secretProvider instanceof LocalSecretProvider);
  }
}
