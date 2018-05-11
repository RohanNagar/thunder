package com.sanction.thunder.email;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmailModuleTest {
  private static final EmailConfiguration EMAIL_CONFIG = mock(EmailConfiguration.class);

  @BeforeAll
  static void setup() {
    when(EMAIL_CONFIG.getEndpoint()).thenReturn("http://localhost:4567");
    when(EMAIL_CONFIG.getRegion()).thenReturn("us-east-1");
    when(EMAIL_CONFIG.getFromAddress()).thenReturn("test@test.com");
  }

  @BeforeEach
  void reset() {
    when(EMAIL_CONFIG.getMessageOptions()).thenReturn(
        new MessageOptions(null, null, null, null, null));
  }

  @Test
  void testProvideSuccessHtmlDefault() throws IOException {
    EmailModule emailModule = new EmailModule(EMAIL_CONFIG);

    String expected = Resources.toString(
        Resources.getResource("success.html"), Charsets.UTF_8);
    assertEquals(expected, emailModule.provideSuccessHtml());
  }

  @Test
  void testProvideSuccessHtmlCustom() throws Exception {
    when(EMAIL_CONFIG.getMessageOptions())
        .thenReturn(new MessageOptions(null, null, null, null,
            new File(Resources.getResource("fixtures/success-page.html").toURI()).getAbsolutePath()));

    EmailModule emailModule = new EmailModule(EMAIL_CONFIG);

    String expected = Resources.toString(
        Resources.getResource("fixtures/success-page.html"), Charsets.UTF_8);
    assertEquals(expected, emailModule.provideSuccessHtml());
  }

  @Test
  void testProvideVerificationHtmlDefault() throws IOException {
    EmailModule emailModule = new EmailModule(EMAIL_CONFIG);

    String expected = Resources.toString(
        Resources.getResource("verification.html"), Charsets.UTF_8);
    assertEquals(expected, emailModule.provideVerificationHtml());
  }

  @Test
  void testProvideVerificationHtmlCustom() throws Exception {
    String path = new File(Resources.getResource(
        "fixtures/verification-email.html").toURI()).getAbsolutePath();

    when(EMAIL_CONFIG.getMessageOptions())
        .thenReturn(new MessageOptions(null, path, null, null, null));

    EmailModule emailModule = new EmailModule(EMAIL_CONFIG);

    String expected = Resources.toString(
        Resources.getResource("fixtures/verification-email.html"), Charsets.UTF_8);
    assertEquals(expected, emailModule.provideVerificationHtml());
  }

  @Test
  void testProvideVerificationTextDefault() throws IOException {
    EmailModule emailModule = new EmailModule(EMAIL_CONFIG);

    String expected = Resources.toString(
        Resources.getResource("verification.txt"), Charsets.UTF_8);
    assertEquals(expected, emailModule.provideVerificationText());
  }

  @Test
  void testProvideVerificationTextCustom() throws Exception {
    String path = new File(
        Resources.getResource("fixtures/verification-email.txt").toURI()).getAbsolutePath();

    when(EMAIL_CONFIG.getMessageOptions())
        .thenReturn(new MessageOptions(null, null, path, null, null));

    EmailModule emailModule = new EmailModule(EMAIL_CONFIG);

    String expected = Resources.toString(
        Resources.getResource("fixtures/verification-email.txt"), Charsets.UTF_8);
    assertEquals(expected, emailModule.provideVerificationText());
  }
}
