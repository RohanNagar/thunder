package com.sanction.thunder.email;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.sanction.thunder.models.Email;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailService {
  private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

  private final AmazonSimpleEmailService emailService;
  private final String fromAddress;

  @Inject
  public EmailService(AmazonSimpleEmailService emailService, String fromAddress) {
    this.emailService = emailService;
    this.fromAddress = fromAddress;
  }

  /**
   * Sends an email to the specified user.
   *
   * @param to The user to send an email to.
   * @param subjectString The subject of the email to be sent.
   * @param htmlBodyString The HTML body of the email to be sent.
   * @param bodyString The text body of the email to be sent.
   *
   * @return A boolean indicating email send success or failure.
   */
  public boolean sendEmail(Email to,
                           String subjectString,
                           String htmlBodyString,
                           String bodyString) {
    Destination destination = new Destination().withToAddresses(to.getAddress());

    Content subjectText = new Content().withCharset("UTF-8").withData(subjectString);
    Content htmlBodyText = new Content().withCharset("UTF-8").withData(htmlBodyString);
    Content bodyText = new Content().withCharset("UTF-8").withData(bodyString);

    Body body = new Body().withHtml(htmlBodyText).withText(bodyText);

    Message message = new Message().withSubject(subjectText).withBody(body);

    SendEmailRequest request = new SendEmailRequest()
        .withSource(fromAddress).withDestination(destination)
        .withMessage(message);

    try {
      emailService.sendEmail(request);
    } catch (AmazonClientException e) {
      LOG.error("There was an error sending email to {}", to.getAddress(), e);
      return false;
    }

    return true;
  }

}