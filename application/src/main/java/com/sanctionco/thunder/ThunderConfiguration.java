package com.sanctionco.thunder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sanctionco.thunder.authentication.AuthConfiguration;
import com.sanctionco.thunder.authentication.basic.BasicAuthConfiguration;
import com.sanctionco.thunder.crypto.PasswordHashConfiguration;
import com.sanctionco.thunder.dao.UsersDaoFactory;
import com.sanctionco.thunder.email.EmailServiceFactory;
import com.sanctionco.thunder.email.disabled.DisabledEmailServiceFactory;
import com.sanctionco.thunder.openapi.OpenApiConfiguration;
import com.sanctionco.thunder.secrets.SecretProvider;
import com.sanctionco.thunder.secrets.local.EnvironmentSecretProvider;
import com.sanctionco.thunder.validation.PropertyValidationConfiguration;

import io.dropwizard.Configuration;

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
   * Optional secrets configuration. Defaults to local which will attempt to read
   * secrets from environment variables.
   */
  @Valid @JsonProperty("secrets")
  private final SecretProvider secretProvider = null;

  public SecretProvider getSecretProvider() {
    return Optional.ofNullable(secretProvider)
        .orElse(new EnvironmentSecretProvider());
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
   * Optional property validation configuration. Defaults to no property validation.
   */
  @Valid @JsonProperty("properties")
  private final PropertyValidationConfiguration validationConfiguration = null;

  PropertyValidationConfiguration getValidationConfiguration() {
    return Optional.ofNullable(validationConfiguration)
        .orElse(new PropertyValidationConfiguration());
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
