package com.sanctionco.thunder.dao.mongodb;

import com.mongodb.MongoClient;
import com.sanctionco.thunder.dao.DatabaseHealthCheck;

import java.util.Objects;

import javax.inject.Inject;

/**
 * Provides the health check service for MongoDB. Provides a method to check the health of
 * the MongoDB instance that Thunder is connected to. See {@code HealthCheck} in
 * {@code com.codahale.metrics.health} for more information on the base class. Additionally, see
 * <a href="https://www.dropwizard.io/1.3.5/docs/manual/core.html#health-checks">The Dropwizard
 * manual</a> for more information on Dropwizard health checks.
 */
public class MongoDbHealthCheck extends DatabaseHealthCheck {
  private final MongoClient mongoClient;

  @Inject
  public MongoDbHealthCheck(MongoClient mongoClient) {
    this.mongoClient = Objects.requireNonNull(mongoClient);
  }

  /**
   * Checks the connected MongoDB instance to ensure that it is healthy.
   *
   * @return healthy if the MongoDB instance is reachable; unhealthy otherwise
   */
  @Override
  protected Result check() {
    return mongoClient.listDatabaseNames().iterator().hasNext()
        ? Result.healthy()
        : Result.unhealthy("No databases in MongoDB");
  }
}
