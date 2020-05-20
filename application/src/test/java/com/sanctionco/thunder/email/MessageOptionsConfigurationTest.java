package com.sanctionco.thunder.email;

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

class MessageOptionsConfigurationTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
  private static final Validator VALIDATOR = Validators.newValidator();
  private static final YamlConfigurationFactory<MessageOptionsConfiguration> FACTORY
      = new YamlConfigurationFactory<>(MessageOptionsConfiguration.class, VALIDATOR, MAPPER, "dw");

  @Test
  void testFromYaml() throws Exception {
    MessageOptionsConfiguration configuration = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/email/message-options-config.yaml").toURI()));

    assertAll("All configuration options are set",
        () -> assertEquals("Test Subject", configuration.getSubject()),
        () -> assertEquals("test-body.html", configuration.getBodyHtmlFilePath()),
        () -> assertEquals("test-body.txt", configuration.getBodyTextFilePath()),
        () -> assertEquals("TEST-PLACEHOLDER", configuration.getUrlPlaceholderString()),
        () -> assertEquals("test-success-page.html", configuration.getSuccessHtmlFilePath()));
  }
}
