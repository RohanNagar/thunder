package com.sanction.thunder.email;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.validation.ValidationMethod;

/**
 * Provides configuration options for email verification, including provider information
 * and customizable message information.
 *
 * @see EmailModule
 */
public class EmailConfiguration {

  @JsonProperty("enabled")
  private final Boolean enabled = true;

  public Boolean isEnabled() {
    return enabled;
  }

  @JsonProperty("endpoint")
  private final String endpoint = null;

  public String getEndpoint() {
    return endpoint;
  }

  @JsonProperty("region")
  private final String region = null;

  public String getRegion() {
    return region;
  }

  @JsonProperty("fromAddress")
  private final String fromAddress = null;

  public String getFromAddress() {
    return fromAddress;
  }

  /* Optional configuration options */

  @JsonProperty("messageOptions")
  private final MessageOptionsConfiguration messageOptions = null;

  public MessageOptionsConfiguration getMessageOptionsConfiguration() {
    return messageOptions;
  }

  /* Validation Methods */

  /**
   * Validates the EmailConfiguration class to ensure the configuration is correctly filled out.
   *
   * @return True if validation is successful, false otherwise.
   */
  @JsonIgnore
  @ValidationMethod(message = "When email is enabled, properties must be filled out")
  public boolean isFilledOut() {
    if (enabled) {
      return endpoint != null && !endpoint.isEmpty()
          && region != null && !region.isEmpty()
          && fromAddress != null && !fromAddress.isEmpty();
    }

    // If not enabled, the other properties can be anything since they won't be used.
    return true;
  }
}
