package com.sanction.thunder.email;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * Provides configuration options for email verification, including provider information
 * and customizable message information.
 *
 * @see EmailModule
 */
public class EmailConfiguration {

  @NotEmpty
  @JsonProperty("endpoint")
  private final String endpoint = null;

  public String getEndpoint() {
    return endpoint;
  }

  @NotEmpty
  @JsonProperty("region")
  private final String region = null;

  public String getRegion() {
    return region;
  }

  @NotEmpty
  @JsonProperty("fromAddress")
  private final String fromAddress = null;

  public String getFromAddress() {
    return fromAddress;
  }

  /* Optional configuration options */

  @JsonProperty("successHtml")
  private final String successHtmlPath = null;

  public String getSuccessHtmlPath() {
    return successHtmlPath;
  }

  @JsonProperty("verificationHtml")
  private final String verificationHtmlPath = null;

  public String getVerificationHtmlPath() {
    return verificationHtmlPath;
  }

  @JsonProperty("verificationText")
  private final String verificationTextPath = null;

  public String getVerificationTextPath() {
    return verificationTextPath;
  }
}
