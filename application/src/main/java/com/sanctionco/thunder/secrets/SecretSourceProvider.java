package com.sanctionco.thunder.secrets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanctionco.thunder.ThunderConfiguration;

import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;

import java.io.IOException;
import java.io.InputStream;
import javax.validation.Validator;

import org.apache.commons.text.StringSubstitutor;

/**
 * An implementation of {@link ConfigurationSourceProvider} that replaces secrets in the
 * Thunder configuration with their value.
 *
 * @see ConfigurationSourceProvider
 * @see SubstitutingSourceProvider
 */
public class SecretSourceProvider implements ConfigurationSourceProvider {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
  private static final Validator VALIDATOR = Validators.newValidator();
  private static final YamlConfigurationFactory<ThunderConfiguration> FACTORY
      = new YamlConfigurationFactory<>(ThunderConfiguration.class, VALIDATOR, MAPPER, "dw");

  private final ConfigurationSourceProvider baseProvider;

  public SecretSourceProvider(ConfigurationSourceProvider baseProvider) {
    this.baseProvider = baseProvider;
  }

  /**
   * Open the configuration, substituting secrets with the value retrieved from the configured
   * {@link SecretProvider}.
   *
   * @param path the path to the configuration file
   * @return an {@link InputStream} representing the final configuration
   * @throws IOException if the file cannot be read
   */
  @Override
  public InputStream open(String path) throws IOException {
    // Read the configuration with the base ConfigurationSourceProvider
    // in order to get the correct secret provider.
    SecretProvider secretProvider;
    try {
      secretProvider = FACTORY.build(baseProvider, path).getSecretProvider();
    } catch (ConfigurationException e) {
      // If there is an exception with parsing the configuration using the base provider,
      // we need to open the configuration as normal to show any errors.
      return baseProvider.open(path);
    }

    // Now, build the SubstitutingSourceProvider to substitute all ${...} secrets with the value
    // provided through the SecretProvider.
    var stringSubstitutor = new StringSubstitutor(secretProvider)
        .setEnableUndefinedVariableException(true);

    var substitutingSourceProvider = new SubstitutingSourceProvider(
        baseProvider, stringSubstitutor);

    return substitutingSourceProvider.open(path);
  }
}
