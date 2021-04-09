package com.sanctionco.thunder.secrets;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SecretSourceProviderTest {

  @Test
  void testOpen() throws Exception {
    SecretSourceProvider provider = new SecretSourceProvider(
        new ResourceConfigurationSourceProvider());

    InputStream stream = provider.open("fixtures/configuration/thunder-config-secrets.yaml");
    String result = new BufferedReader(new InputStreamReader(stream))
        .lines().collect(Collectors.joining("\n"));

    assertNotEquals("""
        database:
          type: dynamodb
          endpoint: ${JAVA_HOME}
          region: test-region-1
          tableName: test-table""", result);
  }

  @Test
  void badConfigShouldOpenWithoutReplacement() throws Exception {
    SecretSourceProvider provider = new SecretSourceProvider(
        new ResourceConfigurationSourceProvider());

    InputStream stream = provider.open("fixtures/configuration/broken-thunder-config.yaml");
    String result = new BufferedReader(new InputStreamReader(stream))
        .lines().collect(Collectors.joining("\n"));

    assertEquals("""
        database:
          type: unknowndb
          endpoint: ${JAVA_HOME}
          region: test-region-1
          tableName: test-table""", result);
  }

  @Test
  void emptyConfigShouldReturnNull() throws Exception {
    SecretSourceProvider provider = new SecretSourceProvider(
        new ResourceConfigurationSourceProvider());

    InputStream stream = provider.open("fixtures/configuration/not-exist.yaml");

    assertNull(stream);
  }
}
