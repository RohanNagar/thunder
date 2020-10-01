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
@SuppressWarnings("ConstantConditions")
public class ThunderConfiguration extends Configuration {

  /**
   * Required database configuration.
   */
  @NotNull @Valid @JsonProperty("database")
  private final UsersDaoFactory usersDaoFactory = null;

  UsersDaoFactory getUsersDaoFactory() {
    return usersDaoFactory;
  }

  /**
   * Optional email configuration. Defaults to disabled.
   */
  @Valid @JsonProperty("email")
  private final EmailServiceFactory emailServiceFactory = null;

  EmailServiceFactory getEmailServiceFactory() {
    return Optional.ofNullable(emailServiceFactory)
        .orElse(new DisabledEmailServiceFactory());
  }

  /**
   * Optional auth configuration. Defaults to basic with no approved API keys.
   */
  @Valid @JsonProperty("auth")
  private final AuthConfiguration authConfiguration = null;

  AuthConfiguration getAuthConfiguration() {
    return Optional.ofNullable(authConfiguration)
        .orElse(new BasicAuthConfiguration());
  }

  /**
   * Optional property validation rules. Defaults to no property validation.
   */
  @Valid @JsonProperty("properties")
  private final List<PropertyValidationRule> validationRules = null;

  List<PropertyValidationRule> getValidationRules() {
    return validationRules;
  }

  /**
   * Optional server-side hash configuration. Defaults to no hashing.
   */
  @Valid @JsonProperty("passwordHash")
  private final PasswordHashConfiguration hashConfiguration = null;

  PasswordHashConfiguration getHashConfiguration() {
    return Optional.ofNullable(hashConfiguration)
        .orElse(new PasswordHashConfiguration());
  }

  /**
   * Optional OpenAPI configuration. Default values provided in {@link OpenApiConfiguration}.
   */
  @Valid @JsonProperty("openApi")
  private final OpenApiConfiguration openApiConfiguration = null;

  OpenApiConfiguration getOpenApiConfiguration() {
    return Optional.ofNullable(openApiConfiguration)
        .orElse(new OpenApiConfiguration());
  }
}
