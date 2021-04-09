package com.sanctionco.thunder.secrets.local;

import com.sanctionco.thunder.secrets.SecretProvider;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class EnvironmentSecretProviderTest {

  @Test
  void shouldReturnNullWhenEnvVarIsNotSet() {
    SecretProvider secretProvider = new EnvironmentSecretProvider();

    var value = secretProvider.lookup("THUNDER_ENV_NOT_EXIST");

    assertNull(value);
  }

  @Test
  void shouldReadFromSystemEnvVars() {
    SecretProvider secretProvider = new EnvironmentSecretProvider();

    var value = secretProvider.lookup("JAVA_HOME");

    assertNotNull(value);
    assertNotEquals("", value);
  }

}
