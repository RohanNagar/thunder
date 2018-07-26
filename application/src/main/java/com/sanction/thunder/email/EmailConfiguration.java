package com.sanction.thunder.email;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.NotEmpty;

import javax.ws.rs.DefaultValue;

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

  @JsonProperty("messageOptions")
  private final MessageOptionsConfiguration messageOptions = null;

  public MessageOptionsConfiguration getMessageOptionsConfiguration() {
    return messageOptions;
  }
}
