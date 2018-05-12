package com.sanction.thunder.email;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmailModuleTest {
  private static final String DEFAULT_SUCCESS_HTML_RESOURCE_FILE = "success.html";
  private static final String DEFAULT_BODY_HTML_RESOURCE_FILE = "verification.html";
  private static final String DEFAULT_BODY_TEXT_RESOURCE_FILE = "verification.txt";

  private static final String CUSTOM_SUCCESS_HTML_RESOURCE_FILE = "fixtures/success-page.html";
  private static final String CUSTOM_BODY_HTML_RESOURCE_FILE = "fixtures/verification-email.html";
  private static final String CUSTOM_BODY_TEXT_RESOURCE_FILE = "fixtures/verification-email.txt";

  private static final EmailConfiguration EMAIL_CONFIG = mock(EmailConfiguration.class);
  private static final MessageOptionsConfiguration OPTIONS_CONFIG
      = mock(MessageOptionsConfiguration.class);

  @BeforeAll
  static void setup() throws Exception {
    when(EMAIL_CONFIG.getEndpoint()).thenReturn("http://localhost:4567");
    when(EMAIL_CONFIG.getRegion()).thenReturn("us-east-1");
    when(EMAIL_CONFIG.getFromAddress()).thenReturn("test@test.com");

    String customSuccessHtmlFilePath = new File(
        Resources.getResource(CUSTOM_SUCCESS_HTML_RESOURCE_FILE).toURI()).getAbsolutePath();
    String customBodyHtmlFilePath = new File(
        Resources.getResource(CUSTOM_BODY_HTML_RESOURCE_FILE).toURI()).getAbsolutePath();
    String customBodyTextFilePath = new File(
        Resources.getResource(CUSTOM_BODY_TEXT_RESOURCE_FILE).toURI()).getAbsolutePath();

    when(OPTIONS_CONFIG.getBodyHtmlFilePath()).thenReturn(customBodyHtmlFilePath);
    when(OPTIONS_CONFIG.getBodyTextFilePath()).thenReturn(customBodyTextFilePath);
    when(OPTIONS_CONFIG.getSuccessHtmlFilePath()).thenReturn(customSuccessHtmlFilePath);
  }

  /* provideMessageOptions() */

  @Test
  void testProvideMessageOptionsNull() {
    when(EMAIL_CONFIG.getMessageOptionsConfiguration()).thenReturn(null);

    MessageOptions expected = new MessageOptions(
        "Account Verification", "bodyHtml", "bodyText", "CODEGEN-URL", "successHtml");

    MessageOptions result = new EmailModule(EMAIL_CONFIG)
        .provideMessageOptions("bodyHtml", "bodyText", "successHtml");

    assertEquals(expected, result);
  }

  @Test
  void testProvideMessageOptionsCustom() {
    when(OPTIONS_CONFIG.getSubject()).thenReturn("Test Subject");
    when(OPTIONS_CONFIG.getUrlPlaceholderString()).thenReturn("Test Placeholder");
    when(EMAIL_CONFIG.getMessageOptionsConfiguration()).thenReturn(OPTIONS_CONFIG);

    MessageOptions expected = new MessageOptions(
        "Test Subject", "bodyHtml", "bodyText", "Test Placeholder", "successHtml");

    MessageOptions result = new EmailModule(EMAIL_CONFIG)
        .provideMessageOptions("bodyHtml", "bodyText", "successHtml");

    assertEquals(expected, result);
  }

  /* provideSuccessHtml() */

  @Test
  void testProvideSuccessHtmlNullOptions() throws IOException {
    when(EMAIL_CONFIG.getMessageOptionsConfiguration()).thenReturn(null);

    EmailModule emailModule = new EmailModule(EMAIL_CONFIG);

    String expected = Resources.toString(
        Resources.getResource(DEFAULT_SUCCESS_HTML_RESOURCE_FILE), Charsets.UTF_8);
    assertEquals(expected, emailModule.provideSuccessHtml());
  }

  @Test
  void testProvideSuccessHtmlDefault() throws IOException {
    when(EMAIL_CONFIG.getMessageOptionsConfiguration())
        .thenReturn(new MessageOptionsConfiguration());

    EmailModule emailModule = new EmailModule(EMAIL_CONFIG);

    String expected = Resources.toString(
        Resources.getResource(DEFAULT_SUCCESS_HTML_RESOURCE_FILE), Charsets.UTF_8);
    assertEquals(expected, emailModule.provideSuccessHtml());
  }

  @Test
  void testProvideSuccessHtmlCustom() throws Exception {
    when(EMAIL_CONFIG.getMessageOptionsConfiguration()).thenReturn(OPTIONS_CONFIG);

    EmailModule emailModule = new EmailModule(EMAIL_CONFIG);

    String expected = Resources.toString(
        Resources.getResource(CUSTOM_SUCCESS_HTML_RESOURCE_FILE), Charsets.UTF_8);
    assertEquals(expected, emailModule.provideSuccessHtml());
  }

  /* provideBodyHtml() */

  @Test
  void testProvideBodyHtmlNullOptions() throws IOException {
    when(EMAIL_CONFIG.getMessageOptionsConfiguration()).thenReturn(null);

    EmailModule emailModule = new EmailModule(EMAIL_CONFIG);

    String expected = Resources.toString(
        Resources.getResource(DEFAULT_BODY_HTML_RESOURCE_FILE), Charsets.UTF_8);
    assertEquals(expected, emailModule.provideBodyHtml());
  }

  @Test
  void testProvideBodyHtmlDefault() throws IOException {
    when(EMAIL_CONFIG.getMessageOptionsConfiguration())
        .thenReturn(new MessageOptionsConfiguration());

    EmailModule emailModule = new EmailModule(EMAIL_CONFIG);

    String expected = Resources.toString(
        Resources.getResource(DEFAULT_BODY_HTML_RESOURCE_FILE), Charsets.UTF_8);
    assertEquals(expected, emailModule.provideBodyHtml());
  }

  @Test
  void testProvideBodyHtmlCustom() throws Exception {
    when(EMAIL_CONFIG.getMessageOptionsConfiguration()).thenReturn(OPTIONS_CONFIG);

    EmailModule emailModule = new EmailModule(EMAIL_CONFIG);

    String expected = Resources.toString(
        Resources.getResource(CUSTOM_BODY_HTML_RESOURCE_FILE), Charsets.UTF_8);
    assertEquals(expected, emailModule.provideBodyHtml());
  }

  /* provideBodyText() */

  @Test
  void testProvideBodyTextNullOptions() throws IOException {
    when(EMAIL_CONFIG.getMessageOptionsConfiguration()).thenReturn(null);

    EmailModule emailModule = new EmailModule(EMAIL_CONFIG);

    String expected = Resources.toString(
        Resources.getResource(DEFAULT_BODY_TEXT_RESOURCE_FILE), Charsets.UTF_8);
    assertEquals(expected, emailModule.provideBodyText());
  }

  @Test
  void testProvideBodyTextDefault() throws IOException {
    when(EMAIL_CONFIG.getMessageOptionsConfiguration())
        .thenReturn(new MessageOptionsConfiguration());

    EmailModule emailModule = new EmailModule(EMAIL_CONFIG);

    String expected = Resources.toString(
        Resources.getResource(DEFAULT_BODY_TEXT_RESOURCE_FILE), Charsets.UTF_8);
    assertEquals(expected, emailModule.provideBodyText());
  }

  @Test
  void testProvideBodyTextCustom() throws Exception {
    when(EMAIL_CONFIG.getMessageOptionsConfiguration()).thenReturn(OPTIONS_CONFIG);

    EmailModule emailModule = new EmailModule(EMAIL_CONFIG);

    String expected = Resources.toString(
        Resources.getResource(CUSTOM_BODY_TEXT_RESOURCE_FILE), Charsets.UTF_8);
    assertEquals(expected, emailModule.provideBodyText());
  }
}
