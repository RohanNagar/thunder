package com.sanctionco.thunder.dao;

import com.codahale.metrics.health.HealthCheck;

/**
 * The base class for all database health check classes. See {@code DynamoDbHealthCheck} for an
 * implementation example.
 */
public abstract class DatabaseHealthCheck extends HealthCheck {

  /**
   * The {@code check()} method necessary for a Dropwizard HealthCheck.
   */
  @Override
  protected abstract Result check();
}
