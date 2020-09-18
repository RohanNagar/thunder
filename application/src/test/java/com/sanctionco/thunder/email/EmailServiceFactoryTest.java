package com.sanctionco.thunder.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.sanctionco.thunder.email.ses.SesEmailServiceFactory;

import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;

import java.io.File;
import java.nio.charset.StandardCharsets;

import javax.validation.Validator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("UnstableApiUsage")
public class EmailServiceFactoryTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
  private static final Validator VALIDATOR = Validators.newValidator();
  private static final YamlConfigurationFactory<EmailServiceFactory> FACTORY
      = new YamlConfigurationFactory<>(EmailServiceFactory.class, VALIDATOR, MAPPER, "dw");

  private static final String DEFAULT_SUCCESS_HTML_RESOURCE_FILE = "success.html";
  private static final String DEFAULT_BODY_HTML_RESOURCE_FILE = "verification.html";
  private static final String DEFAULT_BODY_TEXT_RESOURCE_FILE = "verification.txt";

  private static String DEFAULT_SUCCESS_HTML;
  private static String DEFAULT_BODY_HTML;
  private static String DEFAULT_BODY_TEXT;

  private static final String CUSTOM_SUCCESS_HTML_RESOURCE_FILE = "fixtures/success-page.html";
  private static final String CUSTOM_BODY_HTML_RESOURCE_FILE = "fixtures/verification-email.html";
  private static final String CUSTOM_BODY_TEXT_RESOURCE_FILE = "fixtures/verification-email.txt";

  private static String CUSTOM_SUCCESS_HTML_FILE_PATH;
  private static String CUSTOM_BODY_HTML_FILE_PATH;
  private static String CUSTOM_BODY_TEXT_FILE_PATH;

  @BeforeAll
  static void setup() throws Exception {
    CUSTOM_SUCCESS_HTML_FILE_PATH = new File(
        Resources.getResource(CUSTOM_SUCCESS_HTML_RESOURCE_FILE).toURI()).getAbsolutePath();
    CUSTOM_BODY_HTML_FILE_PATH = new File(
        Resources.getResource(CUSTOM_BODY_HTML_RESOURCE_FILE).toURI()).getAbsolutePath();
    CUSTOM_BODY_TEXT_FILE_PATH = new File(
        Resources.getResource(CUSTOM_BODY_TEXT_RESOURCE_FILE).toURI()).getAbsolutePath();

    DEFAULT_SUCCESS_HTML = Resources.toString(
        Resources.getResource(DEFAULT_SUCCESS_HTML_RESOURCE_FILE), StandardCharsets.UTF_8);
    DEFAULT_BODY_HTML = Resources.toString(
        Resources.getResource(DEFAULT_BODY_HTML_RESOURCE_FILE), StandardCharsets.UTF_8);
    DEFAULT_BODY_TEXT = Resources.toString(
        Resources.getResource(DEFAULT_BODY_TEXT_RESOURCE_FILE), StandardCharsets.UTF_8);
  }

  @Test
  void isDiscoverable() {
    // Make sure the types we specified in META-INF gets picked up
    assertTrue(new DiscoverableSubtypeResolver().getDiscoveredSubtypes()
        .contains(SesEmailServiceFactory.class));
  }

  @Test
  void testDefaultMessageOptions() throws Exception {
    EmailServiceFactory serviceFactory = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/email/message-options/default.yaml").toURI()));

    MessageOptions expected = new MessageOptions(
        "Account Verification", DEFAULT_BODY_HTML, DEFAULT_BODY_TEXT,
        "CODEGEN-URL", "CODEGEN-URL", DEFAULT_SUCCESS_HTML);

    assertEquals(expected, serviceFactory.getMessageOptions());
  }

  @Test
  void testMessageOptionsCustomPlaceholderWithNoCustomBody() throws Exception {
    EmailServiceFactory serviceFactory = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/email/message-options/placeholder-no-body.yaml").toURI()));

    MessageOptions expected = new MessageOptions(
        "Test Subject", DEFAULT_BODY_HTML, DEFAULT_BODY_TEXT,
        "CODEGEN-URL", "CODEGEN-URL", DEFAULT_SUCCESS_HTML);

    assertEquals(expected, serviceFactory.getMessageOptions());
  }

  @Test
  void testMessageOptionsCustomPlaceholderWithCustomBodyText() throws Exception {
    EmailServiceFactory serviceFactory = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/email/message-options/placeholder-body-text.yaml").toURI()));

    MessageOptions expected = new MessageOptions(
        "Test Subject", "bodyHtml", "bodyText",
        "CODEGEN-URL", "TEST-PLACEHOLDER", "successHtml");

    assertEquals(expected, serviceFactory.getMessageOptions("bodyHtml", "bodyText", "successHtml"));
  }

  @Test
  void testMessageOptionsCustomPlaceholderWithCustomBodyHtml() throws Exception {
    EmailServiceFactory serviceFactory = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/email/message-options/placeholder-body-html.yaml").toURI()));

    MessageOptions expected = new MessageOptions(
        "Test Subject", "bodyHtml", "bodyText",
        "TEST-PLACEHOLDER", "CODEGEN-URL", "successHtml");

    assertEquals(expected, serviceFactory.getMessageOptions("bodyHtml", "bodyText", "successHtml"));
  }

  @Test
  void testMessageOptionsCustom() throws Exception {
    EmailServiceFactory serviceFactory = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/email/message-options/custom.yaml").toURI()));

    MessageOptions expected = new MessageOptions(
        "Test Subject", "bodyHtml", "bodyText",
        "TEST-PLACEHOLDER", "TEST-PLACEHOLDER", "successHtml");

    assertEquals(expected, serviceFactory.getMessageOptions("bodyHtml", "bodyText", "successHtml"));
  }

  @Test
  void testGetFileContentsFromPath() throws Exception {
    EmailServiceFactory serviceFactory = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/email/message-options/default.yaml").toURI()));

    String expected = Resources.toString(
        Resources.getResource(CUSTOM_BODY_TEXT_RESOURCE_FILE), StandardCharsets.UTF_8);

    assertEquals(expected,
        serviceFactory.getFileContents(CUSTOM_BODY_TEXT_FILE_PATH, CUSTOM_BODY_HTML_FILE_PATH));
  }
}
