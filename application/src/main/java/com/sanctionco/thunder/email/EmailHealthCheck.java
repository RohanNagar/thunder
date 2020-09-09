package com.sanctionco.thunder.email;

import com.codahale.metrics.health.HealthCheck;

/**
 * The base class for all email service health check classes. This class should not be used
 * as an actual health check for the application. See {@code SesHealthCheck} for an
 * implementation example.
 */
public class EmailHealthCheck extends HealthCheck {

  /**
   * Implements the {@code check()} method for a Dropwizard HealthCheck. This method will always
   * throw an {@code IllegalStateException} because this method should not be used at application
   * runtime.
   *
   * @throws IllegalStateException always
   */
  @Override
  protected Result check() {
    throw new IllegalStateException("Cannot check the health of a generic Email provider! "
        + "Something went wrong during Thunder configuration initialization.");
  }
}
