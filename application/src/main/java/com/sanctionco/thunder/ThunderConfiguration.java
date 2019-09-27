package com.sanctionco.thunder;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.sanctionco.thunder.authentication.Key;
import com.sanctionco.thunder.crypto.PasswordHashConfiguration;
import com.sanctionco.thunder.dao.dynamodb.DynamoDbConfiguration;
import com.sanctionco.thunder.email.EmailConfiguration;
import com.sanctionco.thunder.openapi.OpenApiConfiguration;
import com.sanctionco.thunder.validation.PropertyValidationRule;

import io.dropwizard.Configuration;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Provides Thunder configuration options that are defined in the configuration file. The
 * configuration objects are passed into the application's {@code Module} classes in order to
 * provide information necessary to construct the application dependencies. For more information
 * on Dropwizard Configuration, see the {@code Configuration} class in the {@code io.dropwizard}
 * module.
 *
 * @see ThunderModule
 */
public class ThunderConfiguration extends Configuration {

  @NotNull
  @Valid
  @JsonProperty("database")
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

  @Valid
  @JsonProperty("passwordHash")
  private final PasswordHashConfiguration hashConfiguration = null;

  PasswordHashConfiguration getHashConfiguration() {
    return hashConfiguration == null
        ? new PasswordHashConfiguration()
        : hashConfiguration;
  }

  @Valid
  @JsonProperty("openApi")
  private final OpenApiConfiguration openApiConfiguration = null;

  OpenApiConfiguration getOpenApiConfiguration() {
    return openApiConfiguration == null
        ? new OpenApiConfiguration()
        : openApiConfiguration;
  }
}
