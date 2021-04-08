package com.sanctionco.thunder.secrets.local;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sanctionco.thunder.secrets.SecretFetcher;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the local environment variable based implementation for {@link SecretFetcher}.
 *
 * <p>The application configuration file should use {@code provider: local} in order to use this.
 *
 * <p>This class must be registered in
 * {@code /resources/META-INF/services/com.sanctionco.thunder.secrets.SecretFetcher}.
 *
 * @see SecretFetcher
 */
@JsonTypeName("local")
public class LocalSecretFetcher implements SecretFetcher {
  private static final Logger LOG = LoggerFactory.getLogger(LocalSecretFetcher.class);

  /**
   * Gets the secret value from the system environment variables.
   *
   * @param name the name of the secret to fetch
   * @return the value of the secret if it exists, otherwise an empty optional
   */
  @Override
  public Optional<String> getSecretValue(String name) {
    String value = System.getenv(name);

    if (value == null) {
      LOG.error("Secret {} does not exist in environment variables.", name);
      return Optional.empty();
    }

    LOG.info("Read secret {} from environment variables.", name);
    return Optional.of(value);
  }
}
