package com.sanctionco.thunder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sanctionco.thunder.authentication.AuthConfiguration;
import com.sanctionco.thunder.authentication.basic.BasicAuthConfiguration;
import com.sanctionco.thunder.crypto.PasswordHashConfiguration;
import com.sanctionco.thunder.dao.UsersDaoFactory;
import com.sanctionco.thunder.email.EmailServiceFactory;
import com.sanctionco.thunder.email.disabled.DisabledEmailServiceFactory;
import com.sanctionco.thunder.openapi.OpenApiConfiguration;
import com.sanctionco.thunder.validation.PropertyValidationRule;

import io.dropwizard.Configuration;

import java.util.List;
import java.util.Optional;

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
  private final UsersDaoFactory usersDaoFactory = null;

  UsersDaoFactory getUsersDaoFactory() {
    return usersDaoFactory;
  }

  @Valid
  @JsonProperty("email")
  private final EmailServiceFactory emailServiceFactory = null;

  EmailServiceFactory getEmailServiceFactory() {
    return Optional.ofNullable(emailServiceFactory)
        .orElse(new DisabledEmailServiceFactory());
  }

  @NotNull
  @Valid
  @JsonProperty("auth")
  private final AuthConfiguration authConfiguration = null;

  AuthConfiguration getAuthConfiguration() {
    return Optional.ofNullable(authConfiguration)
        .orElse(new BasicAuthConfiguration());
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
    return Optional.ofNullable(hashConfiguration)
        .orElse(new PasswordHashConfiguration());
  }

  @Valid
  @JsonProperty("openApi")
  private final OpenApiConfiguration openApiConfiguration = null;

  OpenApiConfiguration getOpenApiConfiguration() {
    return Optional.ofNullable(openApiConfiguration)
        .orElse(new OpenApiConfiguration());
  }
}
