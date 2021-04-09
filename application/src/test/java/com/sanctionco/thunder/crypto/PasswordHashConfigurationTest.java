package com.sanctionco.thunder.crypto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;

import java.io.File;

import javax.validation.Validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordHashConfigurationTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
  private static final Validator VALIDATOR = Validators.newValidator();
  private static final YamlConfigurationFactory<PasswordHashConfiguration> FACTORY
      = new YamlConfigurationFactory<>(PasswordHashConfiguration.class, VALIDATOR, MAPPER, "dw");

  @Test
  void testFromYaml() throws Exception {
    PasswordHashConfiguration configuration = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/crypto/password-hash-config.yaml").toURI()));

    assertAll("All configuration options are set correctly",
        () -> assertEquals(HashAlgorithm.BCRYPT, configuration.getAlgorithm()),
        () -> assertTrue(configuration.serverSideHash()),
        () -> assertFalse(configuration.isHeaderCheckEnabled()),
        () -> assertTrue(configuration.allowCommonMistakes()));
  }
}
