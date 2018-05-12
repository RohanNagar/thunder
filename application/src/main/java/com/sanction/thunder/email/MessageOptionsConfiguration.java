package com.sanction.thunder.email;

import com.fasterxml.jackson.annotation.JsonProperty;

class MessageOptionsConfiguration {

  /* Optional configuration options */

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
