package com.sanctionco.thunder.crypto;

import com.sanctionco.thunder.TestResources;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordHashConfigurationTest {

  @Test
  void testFromYaml() {
    PasswordHashConfiguration configuration = TestResources.readResourceYaml(
        PasswordHashConfiguration.class,
        "fixtures/configuration/crypto/password-hash-config.yaml");

    assertAll("All configuration options are set correctly",
        () -> assertEquals(HashAlgorithm.BCRYPT, configuration.getAlgorithm()),
        () -> assertTrue(configuration.serverSideHash()),
        () -> assertFalse(configuration.isHeaderCheckEnabled()),
        () -> assertTrue(configuration.allowCommonMistakes()));
  }
}
