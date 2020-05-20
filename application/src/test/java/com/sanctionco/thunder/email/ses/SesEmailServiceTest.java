package com.sanctionco.thunder.email.ses;

import com.sanctionco.thunder.email.EmailService;
import com.sanctionco.thunder.models.Email;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SesEmailServiceTest {
  private static final String SUBJECT_STRING = "Account Verification";
  private static final String HTML_BODY_STRING = "HTML";
  private static final String BODY_STRING = "TEXT";
  private static final Email MOCK_EMAIL = new Email("test@test.com", false, "verificationToken");

  @Test
  void testSendEmailAmazonClientException() {
    SesClient sesClient = mock(SesClient.class);

    when(sesClient.sendEmail(any(SendEmailRequest.class))).thenThrow(SdkException.class);

    EmailService resource = new SesEmailService(sesClient, "testAddress");

    boolean result = resource.sendEmail(MOCK_EMAIL, SUBJECT_STRING, HTML_BODY_STRING, BODY_STRING);

    assertFalse(result);
  }

  @Test
  void testSendEmailSuccess() {
    SesClient sesClient = mock(SesClient.class);

    when(sesClient.sendEmail(any(SendEmailRequest.class)))
        .thenReturn(SendEmailResponse.builder().messageId("1234").build());

    EmailService resource = new SesEmailService(sesClient, "testAddress");

    boolean result = resource.sendEmail(MOCK_EMAIL, SUBJECT_STRING, HTML_BODY_STRING, BODY_STRING);

    assertTrue(result);
  }
}
