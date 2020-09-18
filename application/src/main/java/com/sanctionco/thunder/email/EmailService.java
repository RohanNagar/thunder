package com.sanctionco.thunder.email;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.util.EmailUtilities;

import java.util.Objects;

/**
 * Provides the base interface for the {@code EmailService}. Provides a method to send an email
 * message.
 */
public abstract class EmailService {
  private final MessageOptions messageOptions;

  private final Counter emailSendSuccessCounter;
  private final Counter emailSendFailureCounter;

  /**
   * Constructs a new instance of {@code EmailService}.
   *
   * @param messageOptions the configurable content of email messages
   * @param metrics the metric registry used to initialize metrics
   */
  public EmailService(MessageOptions messageOptions, MetricRegistry metrics) {
    this.messageOptions = Objects.requireNonNull(messageOptions);

    emailSendSuccessCounter = metrics.counter(MetricRegistry.name(
        EmailService.class, "email-send-success"));
    emailSendFailureCounter = metrics.counter(MetricRegistry.name(
        EmailService.class, "email-send-failure"));
  }

  /**
   * Sends a verification email to the provided email address.
   *
   * @param to the message recipient's email information
   * @param verificationUrl the URL that the recipient should click to verify their email address
   * @return {@code true} if the message was successfully sent; {@code false} otherwise
   */
  public boolean sendVerificationEmail(Email to, String verificationUrl) {
    var result = sendEmail(to, messageOptions.getSubject(),
        EmailUtilities.replaceUrlPlaceholder(messageOptions.getBodyHtml(),
            messageOptions.getBodyHtmlUrlPlaceholder(), verificationUrl),
        EmailUtilities.replaceUrlPlaceholder(messageOptions.getBodyText(),
            messageOptions.getBodyTextUrlPlaceholder(), verificationUrl));

    if (result) {
      emailSendSuccessCounter.inc();
    } else {
      emailSendFailureCounter.inc();
    }

    return result;
  }

  /**
   * Provides the configured success HTML page to provide for successful email verification.
   *
   * @return the configured HTML
   */
  public String getSuccessHtml() {
    return messageOptions.getSuccessHtml();
  }

  /**
   * Sends an email with the given subject and body to the given email address.
   *
   * @param to the message recipient's email information
   * @param subjectString the subject of the message
   * @param htmlBodyString the HTML body of the message
   * @param bodyString the text body of the message
   * @return {@code true} if the message was successfully sent; {@code false} otherwise
   */
  public abstract boolean sendEmail(Email to,
                                    String subjectString,
                                    String htmlBodyString,
                                    String bodyString);
}
