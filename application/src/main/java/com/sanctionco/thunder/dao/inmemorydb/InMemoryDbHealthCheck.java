package com.sanctionco.thunder.dao.inmemorydb;

import com.sanctionco.thunder.dao.DatabaseHealthCheck;

/**
 * Provides the health check for an in-memory database. See {@code HealthCheck} in
 * {@code com.codahale.metrics.health} for more information on the base class. Additionally, see
 * <a href="https://www.dropwizard.io/1.3.5/docs/manual/core.html#health-checks">The Dropwizard
 * manual</a> for more information on Dropwizard health checks.
 */
public class InMemoryDbHealthCheck extends DatabaseHealthCheck {

  /**
   * Checks the health of the InMemoryDB. Always returns {@code Result.healthy()}.
   *
   * @return healthy always
   */
  @Override
  protected Result check() {
    return Result.healthy();
  }
}
