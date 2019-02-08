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

  private final SesClient sesClient = mock(SesClient.class);

  private final Email mockEmail = new Email("test@test.com", false, "verificationToken");

  private final EmailService resource = new SesEmailService(sesClient, "testAddress");

  @Test
  void testSendEmailAmazonClientException() {
    when(sesClient.sendEmail(any(SendEmailRequest.class))).thenThrow(SdkException.class);

    boolean result = resource.sendEmail(mockEmail, SUBJECT_STRING, HTML_BODY_STRING, BODY_STRING);

    assertFalse(result);
  }

  @Test
  void testSendEmailSuccess() {
    when(sesClient.sendEmail(any(SendEmailRequest.class)))
        .thenReturn(SendEmailResponse.builder().messageId("1234").build());

    boolean result = resource.sendEmail(mockEmail, SUBJECT_STRING, HTML_BODY_STRING, BODY_STRING);

    assertTrue(result);
  }
}
