package com.sanction.thunder.email;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.SendEmailResult;

import com.sanction.thunder.models.Email;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmailServiceTest {
  private static final String SUBJECT_STRING = "Account Verification";
  private static final String HTML_BODY_STRING = "HTML";
  private static final String BODY_STRING = "TEXT";

  private final AmazonSimpleEmailService emailService = mock(AmazonSimpleEmailService.class);
  private final SendEmailResult result = mock(SendEmailResult.class);

  private final Email mockEmail = new Email("test@test.com", false, "verificationToken");

  private final EmailService resource = new EmailService(emailService, "testAddress");

  @Test
  @SuppressWarnings("unchecked")
  void testSendEmailAmazonClientException() {
    when(emailService.sendEmail(any())).thenThrow(AmazonClientException.class);

    boolean result = resource.sendEmail(mockEmail, SUBJECT_STRING, HTML_BODY_STRING, BODY_STRING);

    assertFalse(result);
  }

  @Test
  void testSendEmailSuccess() {
    when(emailService.sendEmail(any())).thenReturn(result);

    boolean result = resource.sendEmail(mockEmail, SUBJECT_STRING, HTML_BODY_STRING, BODY_STRING);

    assertTrue(result);
  }
}
