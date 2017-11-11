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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EmailServiceTest {

  private final AmazonSimpleEmailService emailService = mock(AmazonSimpleEmailService.class);
  private final AmazonClientException clientException = mock(AmazonClientException.class);
  private final SendEmailResult result = mock(SendEmailResult.class);

  private final String from = "noreply@sanctionco.com";
  private final String subjectString = "Account Verification";
  private final String htmlBodyString = "HTML";
  private final String bodyString = "TEXT";

  private final Content subjectText = new Content().withCharset("UTF-8").withData(subjectString);
  private final Content htmlBodyText = new Content().withCharset("UTF-8").withData(htmlBodyString);
  private final Content bodyText = new Content().withCharset("UTF-8").withData(bodyString);

  private final Email mockEmail = new Email("test@test.com", false, "verificationToken");
  private final Destination mockDestination = new Destination()
      .withToAddresses(mockEmail.getAddress());
  private final Body mockBody = new Body().withHtml(htmlBodyText).withText(bodyText);
  private final Message mockMessage = new Message().withSubject(subjectText).withBody(mockBody);
  private final SendEmailRequest mockRequest = new SendEmailRequest()
      .withSource(from)
      .withDestination(mockDestination)
      .withMessage(mockMessage);

  private final EmailService resource = new EmailService(emailService);

  @Test
  public void testSendEmailAmazonClientException() {
    when(emailService.sendEmail(mockRequest)).thenThrow(clientException);

    boolean result = resource.sendEmail(mockEmail, subjectString, htmlBodyString, bodyString);

    assertEquals(result, false);
  }

  @Test
  public void testSendEmailSuccess() {
    when(emailService.sendEmail(mockRequest)).thenReturn(result);

    boolean result = resource.sendEmail(mockEmail, subjectString, htmlBodyString, bodyString);

    assertEquals(result, true);
  }
}
