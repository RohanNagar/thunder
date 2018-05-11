package com.sanction.thunder.email;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * Holds optional Email message configuration.
 */
public class MessageOptions {
  private final String subject;
  private final String bodyHtmlFilePath;
  private final String bodyTextFilePath;
  private final String urlPlaceholderString;
  private final String successHtmlFilePath;

  public MessageOptions(@JsonProperty("subject") String subject,
                        @JsonProperty("bodyHtmlFile") String bodyHtmlFilePath,
                        @JsonProperty("bodyTextFile") String bodyTextFilePath,
                        @JsonProperty("urlPlaceholderString") String urlPlaceholderString,
                        @JsonProperty("successHtmlFile") String successHtmlFilePath) {
    this.subject = subject;
    this.bodyHtmlFilePath = bodyHtmlFilePath;
    this.bodyTextFilePath = bodyTextFilePath;
    this.urlPlaceholderString = urlPlaceholderString;
    this.successHtmlFilePath = successHtmlFilePath;
  }

  public String getSubject() {
    return subject;
  }

  public String getBodyHtmlFilePath() {
    return bodyHtmlFilePath;
  }

  public String getBodyTextFilePath() {
    return bodyTextFilePath;
  }

  public String getUrlPlaceholderString() {
    return urlPlaceholderString;
  }

  public String getSuccessHtmlFilePath() {
    return successHtmlFilePath;
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
        && Objects.equals(this.bodyHtmlFilePath, other.bodyHtmlFilePath)
        && Objects.equals(this.bodyTextFilePath, other.bodyTextFilePath)
        && Objects.equals(this.urlPlaceholderString, other.urlPlaceholderString)
        && Objects.equals(this.successHtmlFilePath, other.successHtmlFilePath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        subject, bodyHtmlFilePath, bodyTextFilePath, urlPlaceholderString, successHtmlFilePath);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", "MessageOptions [", "]")
        .add(String.format("subject=%s", subject))
        .add(String.format("bodyHtmlFilePath=%s", bodyHtmlFilePath))
        .add(String.format("bodyTextFilePath=%s", bodyTextFilePath))
        .add(String.format("urlPlaceholderString=%s", urlPlaceholderString))
        .add(String.format("successHtmlFilePath=%s", successHtmlFilePath))
        .toString();
  }
}
