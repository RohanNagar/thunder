package com.sanctionco.thunder;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.sanctionco.thunder.authentication.Key;
import com.sanctionco.thunder.dao.dynamodb.DynamoDbConfiguration;
import com.sanctionco.thunder.email.EmailConfiguration;
import com.sanctionco.thunder.validation.PropertyValidationRule;

import io.dropwizard.Configuration;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Provides configuration options that are defined at the top level of the
 * configuration file.
 *
 * @see ThunderModule
 */
class ThunderConfiguration extends Configuration {

  @NotNull
  @Valid
  @JsonProperty("dynamo")
  private final DynamoDbConfiguration dynamoConfiguration = null;

  DynamoDbConfiguration getDynamoConfiguration() {
    return dynamoConfiguration;
  }

  @NotNull
  @Valid
  @JsonProperty("email")
  private final EmailConfiguration emailConfiguration = null;

  EmailConfiguration getEmailConfiguration() {
    return emailConfiguration;
  }

  @NotNull
  @Valid
  @JsonProperty("approvedKeys")
  private final List<Key> approvedKeys = null;

  List<Key> getApprovedKeys() {
    return approvedKeys;
  }

  /* Optional configuration options */

  @Valid
  @JsonProperty("properties")
  private final List<PropertyValidationRule> validationRules = null;

  List<PropertyValidationRule> getValidationRules() {
    return validationRules;
  }
}
