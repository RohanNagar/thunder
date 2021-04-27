package com.sanctionco.thunder.email;

import com.sanctionco.thunder.TestResources;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageOptionsConfigurationTest {

  @Test
  void testFromYaml() {
    MessageOptionsConfiguration configuration = TestResources.readResourceYaml(
        MessageOptionsConfiguration.class,
        "fixtures/configuration/email/message-options-config.yaml");

    assertAll("All configuration options are set",
        () -> assertEquals("Test Subject", configuration.getSubject()),
        () -> assertEquals("test-body.html", configuration.getBodyHtmlFilePath()),
        () -> assertEquals("test-body.txt", configuration.getBodyTextFilePath()),
        () -> assertEquals("TEST-PLACEHOLDER", configuration.getUrlPlaceholderString()),
        () -> assertEquals("test-success-page.html", configuration.getSuccessHtmlFilePath()));
  }
}
