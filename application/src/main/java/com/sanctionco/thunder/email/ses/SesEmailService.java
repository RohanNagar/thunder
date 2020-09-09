package com.sanctionco.thunder.email.ses;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.sanctionco.thunder.email.EmailService;
import com.sanctionco.thunder.models.Email;

import java.util.Objects;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

/**
 * Provides the Amazon Simple Email Service implementation for the {@link EmailService}. Provides
 * a method to send an email message.
 *
 * @see EmailService
 */
public class SesEmailService implements EmailService {
  private static final Logger LOG = LoggerFactory.getLogger(SesEmailService.class);

  private final SesClient sesClient;
  private final String fromAddress;

  private final Counter emailSendSuccessCounter;
  private final Counter emailSendFailureCounter;

  /**
   * Constructs a new {@code SesEmailService} with the given AWS email service and sender address.
   *
   * @param sesClient the connected Amazon SES email service
   * @param fromAddress the email address to send email messages from
   * @param metrics the metric registry used to initialize metrics
   */
  @Inject
  public SesEmailService(SesClient sesClient, String fromAddress, MetricRegistry metrics) {
    this.sesClient = Objects.requireNonNull(sesClient);
    this.fromAddress = Objects.requireNonNull(fromAddress);

    emailSendSuccessCounter = metrics.counter(MetricRegistry.name(
        SesEmailService.class, "email-send-success"));
    emailSendFailureCounter = metrics.counter(MetricRegistry.name(
        SesEmailService.class, "email-send-failure"));
  }

  @Override
  public boolean sendEmail(Email to,
                           String subjectString,
                           String htmlBodyString,
                           String bodyString) {
    Destination destination = Destination.builder().toAddresses(to.getAddress()).build();

    Content subjectText = Content.builder().charset("UTF-8").data(subjectString).build();
    Content htmlBodyText = Content.builder().charset("UTF-8").data(htmlBodyString).build();
    Content bodyText = Content.builder().charset("UTF-8").data(bodyString).build();

    Body body = Body.builder().html(htmlBodyText).text(bodyText).build();

    Message message = Message.builder().subject(subjectText).body(body).build();

    SendEmailRequest request = SendEmailRequest.builder()
        .source(fromAddress)
        .destination(destination)
        .message(message)
        .build();

    try {
      sesClient.sendEmail(request);
    } catch (SdkException e) {
      LOG.error("There was an error sending email to {}", to.getAddress(), e);

      emailSendFailureCounter.inc();
      return false;
    }

    emailSendSuccessCounter.inc();
    return true;
  }
}
