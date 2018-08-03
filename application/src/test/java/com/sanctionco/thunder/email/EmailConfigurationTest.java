package com.sanctionco.thunder.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

import io.dropwizard.configuration.ConfigurationValidationException;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;

import java.io.File;
import javax.validation.Validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailConfigurationTest {
  private final ObjectMapper mapper = Jackson.newObjectMapper();
  private final Validator validator = Validators.newValidator();
  private final YamlConfigurationFactory<EmailConfiguration> factory
      = new YamlConfigurationFactory<>(EmailConfiguration.class, validator, mapper, "dw");

  @Test
  void testFromYaml() throws Exception {
    EmailConfiguration configuration = factory.build(new File(Resources.getResource(
        "fixtures/configuration/email/valid-config.yaml").toURI()));

    assertAll("Email configuration is correct",
        () -> assertTrue(configuration.isEnabled()),
        () -> assertEquals("test.email.com", configuration.getEndpoint()),
        () -> assertEquals("test-region-2", configuration.getRegion()),
        () -> assertEquals("test@sanctionco.com", configuration.getFromAddress()),
        () -> assertNotNull(configuration.getMessageOptionsConfiguration()));
  }

  @Test
  void testInvalidConfig() {
    assertThrows(ConfigurationValidationException.class,
        () -> factory.build(new File(Resources.getResource(
            "fixtures/configuration/email/null-endpoint.yaml").toURI())));

    assertThrows(ConfigurationValidationException.class,
        () -> factory.build(new File(Resources.getResource(
            "fixtures/configuration/email/empty-endpoint.yaml").toURI())));

    assertThrows(ConfigurationValidationException.class,
        () -> factory.build(new File(Resources.getResource(
            "fixtures/configuration/email/null-region.yaml").toURI())));

    assertThrows(ConfigurationValidationException.class,
        () -> factory.build(new File(Resources.getResource(
            "fixtures/configuration/email/empty-region.yaml").toURI())));

    assertThrows(ConfigurationValidationException.class,
        () -> factory.build(new File(Resources.getResource(
            "fixtures/configuration/email/null-from-address.yaml").toURI())));

    assertThrows(ConfigurationValidationException.class,
        () -> factory.build(new File(Resources.getResource(
            "fixtures/configuration/email/empty-from-address.yaml").toURI())));
  }
}
