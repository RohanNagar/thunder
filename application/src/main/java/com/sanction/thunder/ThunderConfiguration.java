package com.sanction.thunder;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ThunderConfiguration extends Configuration {

  @NotNull
  @Valid
  @JsonProperty("dynamo-endpoint")
  private final String dynamoEndpoint = null;

  public String getDynamoEndpoint() {
    return dynamoEndpoint;
  }
}
