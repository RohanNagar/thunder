package com.sanction.thunder.email;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.sanction.thunder.models.Email;

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EmailServiceTest {

  private final AmazonSimpleEmailService emailService = mock(AmazonSimpleEmailService.class);
  private final AmazonClientException clientException = mock(AmazonClientException.class);
  private final SendEmailResult result = mock(SendEmailResult.class);

  private static final String FROM = "noreply@sanctionco.com";
  private static final String SUBJECT_STRING = "Account Verification";
  private static final String HTML_BODY_STRING = "HTML";
  private static final String BODY_STRING = "TEXT";

  private final Content subjectText = new Content().withCharset("UTF-8").withData(SUBJECT_STRING);
  private final Content htmlBodyText = new Content().withCharset("UTF-8").withData(HTML_BODY_STRING);
  private final Content bodyText = new Content().withCharset("UTF-8").withData(BODY_STRING);

  private final Email mockEmail = new Email("test@test.com", false, "verificationToken");
  private final Destination mockDestination = new Destination()
      .withToAddresses(mockEmail.getAddress());
  private final Body mockBody = new Body().withHtml(htmlBodyText).withText(bodyText);
  private final Message mockMessage = new Message().withSubject(subjectText).withBody(mockBody);
  private final SendEmailRequest mockRequest = new SendEmailRequest()
      .withSource(FROM)
      .withDestination(mockDestination)
      .withMessage(mockMessage);

  private final EmailService resource = new EmailService(emailService);

  @Test
  public void testSendEmailAmazonClientException() {
    when(emailService.sendEmail(mockRequest)).thenThrow(clientException);

    boolean result = resource.sendEmail(mockEmail, SUBJECT_STRING, HTML_BODY_STRING, BODY_STRING);

    assertFalse(result);
  }

  @Test
  public void testSendEmailSuccess() {
    when(emailService.sendEmail(mockRequest)).thenReturn(result);

    boolean result = resource.sendEmail(mockEmail, SUBJECT_STRING, HTML_BODY_STRING, BODY_STRING);

    assertTrue(result);
  }
}
