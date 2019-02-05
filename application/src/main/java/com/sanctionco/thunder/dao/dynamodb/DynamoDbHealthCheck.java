package com.sanctionco.thunder.dao.dynamodb;

import com.codahale.metrics.health.HealthCheck;

import java.util.Objects;
import javax.inject.Inject;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Provides the health check service for DynamoDB. Provides a method to check the health of
 * the DynamoDB instance that Thunder is connected to. See {@code HealthCheck} in
 * {@code com.codahale.metrics.health} for more information on the base class. Additionally, see
 * <a href="https://www.dropwizard.io/1.3.5/docs/manual/core.html#health-checks">The Dropwizard
 * manual</a> for more information on Dropwizard health checks.
 */
public class DynamoDbHealthCheck extends HealthCheck {
  private final DynamoDbClient dynamoDbClient;

  @Inject
  public DynamoDbHealthCheck(DynamoDbClient dynamoDbClient) {
    this.dynamoDbClient = Objects.requireNonNull(dynamoDbClient);
  }

  /**
   * Checks the connected DynamoDB instance to ensure that it is healthy.
   *
   * @return healthy if the DynamoDB instance is reachable; unhealthy otherwise
   */
  @Override
  protected Result check() {
    return dynamoDbClient.listTables().tableNames().size() > 0
        ? Result.healthy()
        : Result.unhealthy("No tables in Dynamo DB");
  }
}
