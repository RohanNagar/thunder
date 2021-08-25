package com.sanctionco.thunder.dao.inmemorydb;

import com.codahale.metrics.health.HealthCheck;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryDbHealthCheckTest extends HealthCheck {

  @Test
  void checkReturnsHealthy() {
    var healthCheck = new InMemoryDbHealthCheck();

    assertTrue(healthCheck.check()::isHealthy);
  }

  // Not used - exists in order to extend HealthCheck
  @Override protected Result check() {
    return Result.healthy();
  }
}
