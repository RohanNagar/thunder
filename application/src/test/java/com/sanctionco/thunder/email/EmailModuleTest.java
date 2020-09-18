package com.sanctionco.thunder.email;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmailModuleTest {
  private static final String DEFAULT_SUCCESS_HTML_RESOURCE_FILE = "success.html";
  private static final String DEFAULT_BODY_HTML_RESOURCE_FILE = "verification.html";
  private static final String DEFAULT_BODY_TEXT_RESOURCE_FILE = "verification.txt";

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
  }

//  @Test
//  void testProvideMessageOptionsCustom() {
//    var configuration = mock(EmailConfiguration.class);
//    var optionsConfiguration = mock(MessageOptionsConfiguration.class);
//
//    when(optionsConfiguration.getSubject()).thenReturn("Test Subject");
//    when(optionsConfiguration.getUrlPlaceholderString()).thenReturn("Test Placeholder");
//    when(optionsConfiguration.getBodyHtmlFilePath()).thenReturn(CUSTOM_BODY_HTML_FILE_PATH);
//    when(optionsConfiguration.getBodyTextFilePath()).thenReturn(CUSTOM_BODY_TEXT_FILE_PATH);
//    when(optionsConfiguration.getSuccessHtmlFilePath()).thenReturn(CUSTOM_SUCCESS_HTML_FILE_PATH);
//    when(configuration.getMessageOptionsConfiguration()).thenReturn(optionsConfiguration);
//
//    MessageOptions expected = new MessageOptions(
//        "Test Subject", "bodyHtml", "bodyText",
//        "Test Placeholder", "Test Placeholder", "successHtml");
//
//    MessageOptions result = new EmailModule(configuration)
//        .provideMessageOptions("bodyHtml", "bodyText", "successHtml");
//
//    assertEquals(expected, result);
//  }
//
//  @Test
//  void testProvideResourcesNullOptions() throws IOException {
//    var configuration = mock(EmailConfiguration.class);
//
//    when(configuration.getMessageOptionsConfiguration()).thenReturn(null);
//
//    EmailModule emailModule = new EmailModule(configuration);
//
//    String successHtmlExpected = Resources.toString(
//        Resources.getResource(DEFAULT_SUCCESS_HTML_RESOURCE_FILE), Charsets.UTF_8);
//    String bodyHtmlExpected = Resources.toString(
//        Resources.getResource(DEFAULT_BODY_HTML_RESOURCE_FILE), Charsets.UTF_8);
//    String bodyTextExpected = Resources.toString(
//        Resources.getResource(DEFAULT_BODY_TEXT_RESOURCE_FILE), Charsets.UTF_8);
//
//    assertEquals(successHtmlExpected, emailModule.provideSuccessHtml());
//    assertEquals(bodyHtmlExpected, emailModule.provideBodyHtml());
//    assertEquals(bodyTextExpected, emailModule.provideBodyText());
//  }
//
//  @Test
//  void testProvideResourcesDefault() throws IOException {
//    var configuration = mock(EmailConfiguration.class);
//
//    when(configuration.getMessageOptionsConfiguration())
//        .thenReturn(new MessageOptionsConfiguration());
//
//    EmailModule emailModule = new EmailModule(configuration);
//
//    String successHtmlExpected = Resources.toString(
//        Resources.getResource(DEFAULT_SUCCESS_HTML_RESOURCE_FILE), Charsets.UTF_8);
//    String bodyHtmlExpected = Resources.toString(
//        Resources.getResource(DEFAULT_BODY_HTML_RESOURCE_FILE), Charsets.UTF_8);
//    String bodyTextExpected = Resources.toString(
//        Resources.getResource(DEFAULT_BODY_TEXT_RESOURCE_FILE), Charsets.UTF_8);
//
//    assertEquals(successHtmlExpected, emailModule.provideSuccessHtml());
//    assertEquals(bodyHtmlExpected, emailModule.provideBodyHtml());
//    assertEquals(bodyTextExpected, emailModule.provideBodyText());
//  }
//
//  @Test
//  void testProvideSuccessHtmlCustom() throws Exception {
//    var configuration = mock(EmailConfiguration.class);
//    var optionsConfiguration = mock(MessageOptionsConfiguration.class);
//
//    when(optionsConfiguration.getBodyHtmlFilePath()).thenReturn(CUSTOM_BODY_HTML_FILE_PATH);
//    when(optionsConfiguration.getBodyTextFilePath()).thenReturn(CUSTOM_BODY_TEXT_FILE_PATH);
//    when(optionsConfiguration.getSuccessHtmlFilePath()).thenReturn(CUSTOM_SUCCESS_HTML_FILE_PATH);
//    when(configuration.getMessageOptionsConfiguration()).thenReturn(optionsConfiguration);
//
//    EmailModule emailModule = new EmailModule(configuration);
//
//    String successHtmlExpected = Resources.toString(
//        Resources.getResource(CUSTOM_SUCCESS_HTML_RESOURCE_FILE), Charsets.UTF_8);
//    String bodyHtmlExpected = Resources.toString(
//        Resources.getResource(CUSTOM_BODY_HTML_RESOURCE_FILE), Charsets.UTF_8);
//    String bodyTextExpected = Resources.toString(
//        Resources.getResource(CUSTOM_BODY_TEXT_RESOURCE_FILE), Charsets.UTF_8);
//
//    assertEquals(successHtmlExpected, emailModule.provideSuccessHtml());
//    assertEquals(bodyHtmlExpected, emailModule.provideBodyHtml());
//    assertEquals(bodyTextExpected, emailModule.provideBodyText());
//  }
}
