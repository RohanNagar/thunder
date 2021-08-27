package com.sanctionco.thunder.email;

import com.codahale.metrics.health.HealthCheck;

/**
 * The base class for all email service health check classes. See {@code SesHealthCheck} for an
 * implementation example.
 */
public abstract class EmailHealthCheck extends HealthCheck {

  /**
   * The {@code check()} method necessary for a Dropwizard HealthCheck.
   */
  @Override
  protected abstract Result check();
}
