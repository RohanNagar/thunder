package com.sanctionco.thunder.dao;

import com.codahale.metrics.health.HealthCheck;

/**
 * The base class for all database health check classes. This class should not be used
 * as an actual health check for the application. See {@code DynamoDbHealthCheck} for more
 * information.
 */
public class DatabaseHealthCheck extends HealthCheck {

  /**
   * Implements the {@code check()} method for a Dropwizard HealthCheck. This method will always
   * throw an {@code IllegalStateException} because this method should not be used at application
   * runtime.
   *
   * @throws IllegalStateException always
   */
  @Override
  protected Result check() {
    throw new IllegalStateException("Cannot check the health of a generic Database! "
        + "Something went wrong during Thunder configuration initialization.");
  }
}
