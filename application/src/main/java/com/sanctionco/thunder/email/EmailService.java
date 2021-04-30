package com.sanctionco.thunder.email;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.sanctionco.thunder.models.Email;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the base interface for the {@code EmailService}. Provides a method to send an email
 * message.
 */
public abstract class EmailService {
  private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

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
  public CompletableFuture<Boolean> sendVerificationEmail(Email to, String verificationUrl) {
    var htmlBody = replaceUrlPlaceholder(messageOptions.bodyHtml(),
        messageOptions.bodyHtmlUrlPlaceholder(), verificationUrl);
    var textBody = replaceUrlPlaceholder(messageOptions.bodyText(),
        messageOptions.bodyTextUrlPlaceholder(), verificationUrl);

    return sendEmail(to, messageOptions.subject(), htmlBody, textBody)
        .thenApply(res -> {
          if (res) {
            emailSendSuccessCounter.inc();
          } else {
            emailSendFailureCounter.inc();
          }

          return res;
        });
  }

  /**
   * Provides the configured success HTML page to provide for successful email verification.
   *
   * @return the configured HTML
   */
  public String getSuccessHtml() {
    return messageOptions.successHtml();
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
  public abstract CompletableFuture<Boolean> sendEmail(Email to,
                                                       String subjectString,
                                                       String htmlBodyString,
                                                       String bodyString);

  /**
   * Replaces the placeholder inside the given file contents with the given URL.
   *
   * @param fileContents the file contents that contain the placeholder
   * @param placeholder the placeholder string that exists in the file contents
   * @param url the URL to insert in place of the placeholder
   * @return the modified file contents
   */
  String replaceUrlPlaceholder(String fileContents, String placeholder, String url) {
    if (!fileContents.contains(placeholder)) {
      LOG.warn("The file contents do not contain any instances of the URL placeholder {}",
          placeholder);
    }

    return fileContents.replaceAll(placeholder, url);
  }
}
