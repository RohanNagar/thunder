package com.sanctionco.thunder.email.ses;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sanctionco.thunder.email.EmailHealthCheck;
import com.sanctionco.thunder.email.EmailService;
import com.sanctionco.thunder.email.EmailServiceFactory;

import io.dropwizard.validation.ValidationMethod;

import java.net.URI;
import java.util.Objects;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesAsyncClient;

/**
 * Provides the Amazon SES implementation for the {@link EmailServiceFactory}. Provides methods
 * to construct new UsersDao and {@link EmailHealthCheck} objects that interact with SES.
 *
 * <p>The application configuration file should use {@code type: ses} in order to use this
 * factory.
 *
 * <p>This class must be registered in
 * {@code /resources/META-INF/services/com.sanctionco.thunder.email.EmailServiceFactory}.
 *
 * @see EmailServiceFactory
 */
@JsonTypeName("ses")
@SuppressWarnings("ConstantConditions")
public class SesEmailServiceFactory extends EmailServiceFactory {
  private static final Logger LOG = LoggerFactory.getLogger(SesEmailServiceFactory.class);

  SesAsyncClient sesClient;

  @JsonProperty("endpoint")
  private final String endpoint = null;

  @JsonProperty("region")
  private final String region = null;

  public String getEndpoint() {
    return endpoint;
  }

  public String getRegion() {
    return region;
  }

  @Override
  public EmailService createEmailService(MetricRegistry metrics) {
    LOG.info("Creating SES implementation of EmailService");
    LOG.info("Configuration: {}", this);

    initializeSesClient();

    return new SesEmailService(sesClient, getFromAddress(), getMessageOptions(), metrics);
  }

  @Override
  public EmailHealthCheck createHealthCheck() {
    LOG.info("Creating SES implementation of EmailHealthCheck");

    initializeSesClient();

    return new SesHealthCheck(sesClient);
  }

  private synchronized void initializeSesClient() {
    if (this.sesClient != null) {
      return;
    }

    Objects.requireNonNull(region);
    Objects.requireNonNull(endpoint);

    this.sesClient = SesAsyncClient.builder()
        .region(Region.of(region))
        .endpointOverride(URI.create(endpoint))
        .build();
  }

  /**
   * Validates the email configuration to ensure the configuration is correctly set.
   *
   * @return {@code true} if validation is successful; {@code false} otherwise
   */
  @JsonIgnore
  @ValidationMethod(message = "The endpoint, region, and fromAddress fields must be filled out "
      + "to use SES as the email service.")
  public boolean isFilledOut() {
    return endpoint != null && !endpoint.isEmpty()
        && region != null && !region.isEmpty()
        && getFromAddress() != null && !getFromAddress().isEmpty();
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", "SesEmailServiceFactory [", "]")
        .add(String.format("enabled=%s", isEnabled()))
        .add(String.format("endpoint=%s", endpoint))
        .add(String.format("region=%s", region))
        .add(String.format("fromAddress=%s", getFromAddress()))
        .add(String.format("isFullyConfigured=%s", isFilledOut()))
        .toString();
  }
}
