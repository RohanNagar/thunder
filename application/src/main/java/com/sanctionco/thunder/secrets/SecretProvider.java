package com.sanctionco.thunder.secrets;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.dropwizard.jackson.Discoverable;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Provides the base interface for the {@code SecretFetcher}.
 *
 * <p>This class is to be used within the Dropwizard configuration and provides polymorphic
 * configuration - which allows us to implement the {@code secrets} section of our configuration
 * with multiple configuration classes.
 *
 * <p>The {@code provider} property on the configuration object is used to determine which
 * implementing class to construct.
 *
 * <p>This class must be registered in
 * {@code /resources/META-INF/services/io.dropwizard.jackson.Discoverable}.
 *
 * <p>See the {@code ThunderConfiguration} class for usage.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "provider")
public interface SecretProvider extends Discoverable {
  Pattern secretIdentifier = Pattern.compile("\\$\\{\\s*(\\S+)\\s*}");

  /**
   * Fetches the value of the secret with the given name.
   *
   * @param name the name of the secret to fetch
   * @return an optional that contains the value of the secret if it exists
   */
  Optional<String> getSecretValue(String name);

  /**
   * Given a secret identifier string, returns the name of the secret contained
   * within ${@code ${}}. If the identifier is not formed correctly, returns an
   * empty {@link Optional}.
   *
   * @param identifier the secret identifier to parse
   * @return an optional that contains the name of the secret if it is a valid identifier
   */
  default Optional<String> parseSecretNameFromIdentifier(String identifier) {
    var matcher = secretIdentifier.matcher(identifier);

    if (matcher.matches()) {
      return Optional.of(matcher.group(1));
    }

    return Optional.empty();
  }
}
