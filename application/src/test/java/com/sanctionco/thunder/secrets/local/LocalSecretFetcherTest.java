package com.sanctionco.thunder.secrets.local;

import com.sanctionco.thunder.secrets.SecretFetcher;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalSecretFetcherTest {

  @Test
  void shouldThrowWhenEnvVarIsNotSet() {
    SecretFetcher secretFetcher = new LocalSecretFetcher();

    var value = secretFetcher.getSecretValue("THUNDER_ENV_NOT_EXIST");

    assertNotNull(value);
    assertTrue(value.isEmpty());
  }

  @Test
  void shouldReadFromSystemEnvVars() {
    SecretFetcher secretFetcher = new LocalSecretFetcher();

    var value = secretFetcher.getSecretValue("JAVA_HOME");

    assertNotNull(value);
    assertTrue(value.isPresent());
    assertNotEquals("", value.get());
  }

}
