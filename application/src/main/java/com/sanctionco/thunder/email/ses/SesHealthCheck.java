package com.sanctionco.thunder.email.ses;

import com.sanctionco.thunder.email.EmailHealthCheck;

import java.util.Objects;

import javax.inject.Inject;

import software.amazon.awssdk.services.ses.SesClient;

/**
 * Provides the health check service for AWS SES. Provides a method to check that the current
 * SES sender account is enabled to send emails. See {@code HealthCheck} in
 * {@code com.codahale.metrics.health} for more information on the base class. Additionally, see
 * <a href="https://www.dropwizard.io/1.3.5/docs/manual/core.html#health-checks">The Dropwizard
 * manual</a> for more information on Dropwizard health checks.
 */
public class SesHealthCheck extends EmailHealthCheck {
  private final SesClient sesClient;

  @Inject
  public SesHealthCheck(SesClient sesClient) {
    this.sesClient = Objects.requireNonNull(sesClient);
  }

  /**
   * Checks the connected SES account to ensure that sending emails is enabled.
   *
   * @return healthy if the SES client is enabled for sending emails; unhealthy otherwise
   */
  @Override
  protected Result check() {
    return sesClient.getAccountSendingEnabled().enabled()
        ? Result.healthy()
        : Result.unhealthy("The configured SES account is not enabled for sending emails.");
  }
}
