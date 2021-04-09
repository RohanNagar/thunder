package com.sanctionco.thunder.secrets.secretsmanager;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sanctionco.thunder.secrets.SecretProvider;

import java.net.URI;

import javax.validation.constraints.NotEmpty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

/**
 * Provides the AWS Secrets Manager based implementation for {@link SecretProvider}.
 *
 * <p>The application configuration file should use {@code provider: secretsmanager} in order
 * to use this.
 *
 * <p>This class must be registered in
 * {@code /resources/META-INF/services/com.sanctionco.thunder.secrets.SecretFetcher}.
 *
 * @see SecretProvider
 */
@JsonTypeName("secretsmanager")
public class SecretsManagerSecretProvider implements SecretProvider {
  private static final Logger LOG = LoggerFactory.getLogger(SecretsManagerSecretProvider.class);

  private SecretsManagerClient secretsClient = null;

  @NotEmpty
  @JsonProperty("endpoint")
  private final String endpoint = null;

  public String getEndpoint() {
    return endpoint;
  }

  @NotEmpty
  @JsonProperty("region")
  private final String region = null;

  public String getRegion() {
    return region;
  }

  /**
   * Gets the secret value from AWS secrets manager.
   *
   * @param name the name of the secret to fetch
   * @return the value of the secret if it exists, otherwise null
   */
  @Override
  public String lookup(String name) {
    if (secretsClient == null) {
      initializeClient();
    }

    GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
        .secretId(name)
        .build();

    try {
      GetSecretValueResponse valueResponse = secretsClient.getSecretValue(valueRequest);

      return valueResponse.secretString();
    } catch (SecretsManagerException e) {
      LOG.error("Secret {} could not be read from AWS Secrets Manager", name, e);

      return null;
    }
  }

  @SuppressWarnings("ConstantConditions")
  private synchronized void initializeClient() {
    if (secretsClient == null) {
      secretsClient = SecretsManagerClient.builder()
          .region(Region.of(region))
          .endpointOverride(URI.create(endpoint))
          .build();
    }
  }
}
