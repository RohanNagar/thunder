package com.sanctionco.thunder.secrets.local;

import com.sanctionco.thunder.secrets.SecretProvider;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalSecretProviderTest {

  @Test
  void shouldThrowWhenEnvVarIsNotSet() {
    SecretProvider secretProvider = new LocalSecretProvider();

    var value = secretProvider.getSecretValue("THUNDER_ENV_NOT_EXIST");

    assertNotNull(value);
    assertTrue(value.isEmpty());
  }

  @Test
  void shouldReadFromSystemEnvVars() {
    SecretProvider secretProvider = new LocalSecretProvider();

    var value = secretProvider.getSecretValue("JAVA_HOME");

    assertNotNull(value);
    assertTrue(value.isPresent());
    assertNotEquals("", value.get());
  }

}
