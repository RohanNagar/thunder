package com.sanction.thunder;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class ThunderConfiguration extends Configuration {

  @JsonProperty("dynamo-endpoint")
  private final String dynamoEndpoint = null;

  public String getDynamoEndpoint() {
    return dynamoEndpoint;
  }
}
