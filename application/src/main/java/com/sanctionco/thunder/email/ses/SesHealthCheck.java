package com.sanctionco.thunder.email.ses;

import com.sanctionco.thunder.email.EmailHealthCheck;

import java.util.Objects;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.ses.SesAsyncClient;

/**
 * Provides the health check service for AWS SES. Provides a method to check that the current
 * SES sender account is enabled to send emails. See {@code HealthCheck} in
 * {@code com.codahale.metrics.health} for more information on the base class. Additionally, see
 * <a href="https://www.dropwizard.io/1.3.5/docs/manual/core.html#health-checks">The Dropwizard
 * manual</a> for more information on Dropwizard health checks.
 */
public class SesHealthCheck extends EmailHealthCheck {
  private static final Logger LOG = LoggerFactory.getLogger(SesHealthCheck.class);

  private final SesAsyncClient sesClient;

  @Inject
  public SesHealthCheck(SesAsyncClient sesClient) {
    this.sesClient = Objects.requireNonNull(sesClient);
  }

  /**
   * Checks the connected SES account to ensure that sending emails is enabled.
   *
   * @return healthy if the SES client is enabled for sending emails; unhealthy otherwise
   */
  @Override
  protected Result check() {
    LOG.info("Checking health of AWS Simple Email Service...");

    return sesClient.getAccountSendingEnabled()
        .thenApply(response -> response.enabled()
            ? Result.healthy()
            : Result.unhealthy("The configured SES account is not enabled for sending emails."))
        .exceptionally(throwable -> {
          LOG.error("There was an exception when checking health of SES.", throwable);

          return Result.unhealthy("There is an issue communicating with SES.");
        }).join();
  }
}
