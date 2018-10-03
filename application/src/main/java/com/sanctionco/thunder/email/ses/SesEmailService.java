package com.sanctionco.thunder.email.ses;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

import com.sanctionco.thunder.email.EmailService;
import com.sanctionco.thunder.models.Email;

import java.util.Objects;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the Amazon Simple Email Service implementation for the EmailService. Provides
 * a method to send an email message.
 */
public class SesEmailService implements EmailService {
  private static final Logger LOG = LoggerFactory.getLogger(SesEmailService.class);

  private final AmazonSimpleEmailService emailService;
  private final String fromAddress;

  /**
   * Constructs a new SesEmailService with the given AWS email service and sender address.
   *
   * @param emailService the connected Amazon SES email service
   * @param fromAddress the email address to send email messages from
   */
  @Inject
  public SesEmailService(AmazonSimpleEmailService emailService, String fromAddress) {
    this.emailService = Objects.requireNonNull(emailService);
    this.fromAddress = Objects.requireNonNull(fromAddress);
  }

  @Override
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
        .withSource(fromAddress)
        .withDestination(destination)
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
