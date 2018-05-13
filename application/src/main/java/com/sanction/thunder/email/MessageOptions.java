package com.sanction.thunder.email;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * Holds optional Email message configuration such as email content and success HTML.
 */
public class MessageOptions {
  private final String subject;
  private final String bodyHtml;
  private final String bodyText;
  private final String urlPlaceholderString;
  private final String successHtml;

  /**
   * Constructs a new MessageOptions instance.
   *
   * @param subject The subject of the email message.
   * @param bodyHtml The body of the email message in HTML form.
   * @param bodyText The body of the email message in plaintext form.
   * @param urlPlaceholderString The placeholder string found in the body that should be replaced
   *                             by a custom URL on each message request.
   * @param successHtml The HTML contents to display on successful verification.
   */
  public MessageOptions(@JsonProperty("subject") String subject,
                        @JsonProperty("bodyHtmlFile") String bodyHtml,
                        @JsonProperty("bodyTextFile") String bodyText,
                        @JsonProperty("urlPlaceholderString") String urlPlaceholderString,
                        @JsonProperty("successHtmlFile") String successHtml) {
    this.subject = subject;
    this.bodyHtml = bodyHtml;
    this.bodyText = bodyText;
    this.urlPlaceholderString = urlPlaceholderString;
    this.successHtml = successHtml;
  }

  public String getSubject() {
    return subject;
  }

  public String getBodyHtml() {
    return bodyHtml;
  }

  public String getBodyText() {
    return bodyText;
  }

  public String getUrlPlaceholderString() {
    return urlPlaceholderString;
  }

  public String getSuccessHtml() {
    return successHtml;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof MessageOptions)) {
      return false;
    }

    MessageOptions other = (MessageOptions) obj;
    return Objects.equals(this.subject, other.subject)
        && Objects.equals(this.bodyHtml, other.bodyHtml)
        && Objects.equals(this.bodyText, other.bodyText)
        && Objects.equals(this.urlPlaceholderString, other.urlPlaceholderString)
        && Objects.equals(this.successHtml, other.successHtml);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        subject, bodyHtml, bodyText, urlPlaceholderString, successHtml);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", "MessageOptions [", "]")
        .add(String.format("subject=%s", subject))
        .add(String.format("bodyHtml=%s", bodyHtml))
        .add(String.format("bodyText=%s", bodyText))
        .add(String.format("urlPlaceholderString=%s", urlPlaceholderString))
        .add(String.format("successHtml=%s", successHtml))
        .toString();
  }
}
