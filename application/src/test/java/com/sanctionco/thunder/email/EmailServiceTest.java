package com.sanctionco.thunder.email;

import com.codahale.metrics.MetricRegistry;
import com.sanctionco.thunder.email.ses.SesEmailService;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.ses.SesClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class EmailServiceTest {
  private final EmailService emailService = new SesEmailService(
      mock(SesClient.class), "address", mock(MessageOptions.class), new MetricRegistry());

  @Test
  void testReplacePlaceholderNoUrl() {
    String contents = "test contents";
    String url = "http://www.test.com";

    assertEquals(contents, emailService.replaceUrlPlaceholder(contents, "CODEGEN-URL", url));
  }

  @Test
  void testReplacePlaceholderWithUrl() {
    String contents = "test contents CODEGEN-URL";
    String url = "http://www.test.com";

    String expected = "test contents " + url;

    assertEquals(expected, emailService.replaceUrlPlaceholder(contents, "CODEGEN-URL", url));
  }

  @Test
  void testReplaceWithCustomPlaceholder() {
    String contents = "test contents PLACEHOLDER";
    String url = "http://www.test.com";

    String expected = "test contents " + url;

    assertEquals(expected, emailService.replaceUrlPlaceholder(contents, "PLACEHOLDER", url));
  }
}
