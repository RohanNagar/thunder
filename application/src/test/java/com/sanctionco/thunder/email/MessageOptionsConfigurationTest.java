package com.sanctionco.thunder.email;

import com.google.common.io.Resources;
import com.sanctionco.thunder.TestResources;

import io.dropwizard.configuration.YamlConfigurationFactory;

import java.io.File;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageOptionsConfigurationTest {
  private static final YamlConfigurationFactory<MessageOptionsConfiguration> FACTORY
      = new YamlConfigurationFactory<>(
          MessageOptionsConfiguration.class, TestResources.VALIDATOR, TestResources.MAPPER, "dw");

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
