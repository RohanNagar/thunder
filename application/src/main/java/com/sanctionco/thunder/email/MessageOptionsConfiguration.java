package com.sanctionco.thunder.email;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Provides optional customization options for email messages, including the subject, the path
 * to the body HTML file, the path to the body text file, the URL placeholder string, and the path
 * to the success HTML file.
 *
 * @see EmailServiceFactory
 */
class MessageOptionsConfiguration {

  @JsonProperty("subject")
  private final String subject = null;

  String getSubject() {
    return subject;
  }

  @JsonProperty("bodyHtmlFilePath")
  private final String bodyHtmlFilePath = null;

  String getBodyHtmlFilePath() {
    return bodyHtmlFilePath;
  }

  @JsonProperty("bodyTextFilePath")
  private final String bodyTextFilePath = null;

  String getBodyTextFilePath() {
    return bodyTextFilePath;
  }

  @JsonProperty("urlPlaceholderString")
  private final String urlPlaceholderString = null;

  String getUrlPlaceholderString() {
    return urlPlaceholderString;
  }

  @JsonProperty("successHtmlFilePath")
  private final String successHtmlFilePath = null;

  String getSuccessHtmlFilePath() {
    return successHtmlFilePath;
  }
}
