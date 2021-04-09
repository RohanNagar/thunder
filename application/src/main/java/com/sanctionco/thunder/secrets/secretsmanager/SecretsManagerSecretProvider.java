package com.sanctionco.thunder.secrets.secretsmanager;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sanctionco.thunder.secrets.SecretProvider;

import java.net.URI;
import java.time.Duration;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.core.exception.SdkClientException;
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

  @Min(1)
  @JsonProperty("retryDelaySeconds")
  private final Integer retryDelaySeconds = 1;

  public Integer getRetryDelaySeconds() {
    return retryDelaySeconds;
  }

  @Min(0)
  @JsonProperty("maxRetries")
  private final Integer maxRetries = 0;

  public Integer getMaxRetries() {
    return maxRetries;
  }

  /**
   * Gets the secret value from AWS secrets manager. If there is an {@link SdkClientException}
   * when connecting to Secrets Manager, this method will retry lookup {@code maxRetries} number
   * of times, each after a {@code retryDelaySeconds} period of time.
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

    // Set up a retry policy to retry fetching secrets when unable to connect.
    RetryPolicy<Object> retryPolicy = new RetryPolicy<>()
        .handle(SdkClientException.class)
        .withDelay(Duration.ofSeconds(retryDelaySeconds))
        .withMaxRetries(maxRetries)
        .onFailedAttempt(e ->
            LOG.error("Unable to connect to AWS Secrets Manager. Retrying after 30 seconds...",
                e.getLastFailure()));

    try {
      GetSecretValueResponse valueResponse = Failsafe.with(retryPolicy)
          .get(() -> secretsClient.getSecretValue(valueRequest));

      return valueResponse.secretString();
    } catch (SecretsManagerException e) {
      LOG.error("Secret {} could not be read from AWS Secrets Manager", name, e);

      return null;
    }
  }

  @SuppressWarnings("ConstantConditions")
  private void initializeClient() {
    secretsClient = SecretsManagerClient.builder()
        .region(Region.of(region))
        .endpointOverride(URI.create(endpoint))
        .build();
  }
}
