package com.sanction.thunder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sanction.thunder.authentication.Key;
import io.dropwizard.Configuration;

import java.util.List;
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

  @NotNull
  @Valid
  @JsonProperty("dynamo-table-name")
  private final String dynamoTableName = null;

  public String getDynamoTableName() {
    return dynamoTableName;
  }

  @NotNull
  @Valid
  @JsonProperty("approved-keys")
  private final List<Key> approvedKeys = null;

  public List<Key> getApprovedKeys() {
    return approvedKeys;
  }
}
