package com.sanctionco.thunder.secrets;

import com.sanctionco.thunder.TestResources;
import com.sanctionco.thunder.secrets.local.EnvironmentSecretProvider;
import com.sanctionco.thunder.secrets.secretsmanager.SecretsManagerSecretProvider;

import io.dropwizard.jackson.DiscoverableSubtypeResolver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecretProviderTest {

  @Test
  void isDiscoverable() {
    // Make sure the types we specified in META-INF gets picked up
    assertTrue(new DiscoverableSubtypeResolver().getDiscoveredSubtypes()
        .contains(EnvironmentSecretProvider.class));
    assertTrue(new DiscoverableSubtypeResolver().getDiscoveredSubtypes()
        .contains(SecretsManagerSecretProvider.class));
  }

  @Test
  void testEnvFromYaml() {
    SecretProvider secretProvider = TestResources.readResourceYaml(
        SecretProvider.class,
        "fixtures/configuration/secrets/env-config.yaml");

    assertTrue(secretProvider instanceof EnvironmentSecretProvider);
  }

  @Test
  void testSecretsManagerFromYamlNoOptionals() {
    SecretProvider secretProvider = TestResources.readResourceYaml(
        SecretProvider.class,
        "fixtures/configuration/secrets/secretsmanager-config.yaml");

    assertTrue(secretProvider instanceof SecretsManagerSecretProvider);

    var secretsManagerProvider = (SecretsManagerSecretProvider) secretProvider;

    assertEquals("test-region", secretsManagerProvider.getRegion());
    assertEquals("http://www.test.com", secretsManagerProvider.getEndpoint());

    // Default retry config
    assertEquals(1, secretsManagerProvider.getRetryDelaySeconds());
    assertEquals(0, secretsManagerProvider.getMaxRetries());
  }

  @Test
  void testSecretsManagerFromYamlWithOptionals() {
    SecretProvider secretProvider = TestResources.readResourceYaml(
        SecretProvider.class,
        "fixtures/configuration/secrets/secretsmanager-config-optionals.yaml");

    assertTrue(secretProvider instanceof SecretsManagerSecretProvider);

    var secretsManagerProvider = (SecretsManagerSecretProvider) secretProvider;

    assertEquals("test-region", secretsManagerProvider.getRegion());
    assertEquals("http://www.test.com", secretsManagerProvider.getEndpoint());

    // Non-default retry config
    assertEquals(5, secretsManagerProvider.getRetryDelaySeconds());
    assertEquals(10, secretsManagerProvider.getMaxRetries());
  }
}
