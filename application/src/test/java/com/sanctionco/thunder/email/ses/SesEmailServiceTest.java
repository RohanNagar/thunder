package com.sanctionco.thunder.email.ses;

import com.codahale.metrics.MetricRegistry;
import com.sanctionco.thunder.email.EmailService;
import com.sanctionco.thunder.email.MessageOptions;
import com.sanctionco.thunder.models.Email;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.ses.SesAsyncClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SesEmailServiceTest {
  private static final String SUBJECT_STRING = "Account Verification";
  private static final String HTML_BODY_STRING = "HTML Placeholder";
  private static final String BODY_STRING = "TEXT Placeholder";
  private static final String SUCCESS_HTML = "<html>success!</html>";
  private static final String PLACEHOLDER = "Placeholder";
  private static final String VERIFICATION_URL = "verification.com";
  private static final Email MOCK_EMAIL = new Email("test@test.com", false, "verificationToken");
  private static final MessageOptions MESSAGE_OPTIONS = new MessageOptions(
      SUBJECT_STRING, HTML_BODY_STRING, BODY_STRING, PLACEHOLDER, PLACEHOLDER, SUCCESS_HTML);

  @Test
  void testSendEmailAmazonClientException() {
    SesAsyncClient sesClient = mock(SesAsyncClient.class);
    when(sesClient.sendEmail(any(SendEmailRequest.class)))
        .thenReturn(CompletableFuture.failedFuture(mock(SdkException.class)));

    MetricRegistry metrics = new MetricRegistry();

    EmailService resource = new SesEmailService(sesClient, "testAddress", MESSAGE_OPTIONS, metrics);
    EmailService resourceSpy = spy(resource);

    boolean result = resourceSpy.sendVerificationEmail(MOCK_EMAIL, VERIFICATION_URL);

    assertFalse(result);
    assertEquals(0, metrics.counter(
        MetricRegistry.name(EmailService.class, "email-send-success")).getCount());
    assertEquals(1, metrics.counter(
        MetricRegistry.name(EmailService.class, "email-send-failure")).getCount());

    verify(resourceSpy).sendEmail(eq(MOCK_EMAIL), eq(SUBJECT_STRING),
        eq("HTML verification.com"), eq("TEXT verification.com"));
  }

  @Test
  void testSendEmailSuccess() {
    SesAsyncClient sesClient = mock(SesAsyncClient.class);
    when(sesClient.sendEmail(any(SendEmailRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(
            SendEmailResponse.builder().messageId("1234").build()));

    MetricRegistry metrics = new MetricRegistry();

    EmailService resource = new SesEmailService(sesClient, "testAddress", MESSAGE_OPTIONS, metrics);
    EmailService resourceSpy = spy(resource);

    boolean result = resourceSpy.sendVerificationEmail(MOCK_EMAIL, VERIFICATION_URL);

    assertTrue(result);
    assertEquals(1, metrics.counter(
        MetricRegistry.name(EmailService.class, "email-send-success")).getCount());
    assertEquals(0, metrics.counter(
        MetricRegistry.name(EmailService.class, "email-send-failure")).getCount());

    verify(resourceSpy).sendEmail(eq(MOCK_EMAIL), eq(SUBJECT_STRING),
        eq("HTML verification.com"), eq("TEXT verification.com"));
  }

  @Test
  void testGetSuccessHtml() {
    EmailService resource = new SesEmailService(
        mock(SesAsyncClient.class), "testAddress", MESSAGE_OPTIONS, new MetricRegistry());

    String result = resource.getSuccessHtml();

    assertEquals(SUCCESS_HTML, result);
  }
}
