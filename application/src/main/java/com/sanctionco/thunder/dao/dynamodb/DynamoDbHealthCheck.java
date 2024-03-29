package com.sanctionco.thunder.dao.dynamodb;

import com.sanctionco.thunder.dao.DatabaseHealthCheck;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

/**
 * Provides the health check service for DynamoDB. Provides a method to check the health of
 * the DynamoDB instance that Thunder is connected to. See {@code HealthCheck} in
 * {@code com.codahale.metrics.health} for more information on the base class. Additionally, see
 * <a href="https://www.dropwizard.io/1.3.5/docs/manual/core.html#health-checks">The Dropwizard
 * manual</a> for more information on Dropwizard health checks.
 */
public class DynamoDbHealthCheck extends DatabaseHealthCheck {
  private static final Logger LOG = LoggerFactory.getLogger(DynamoDbHealthCheck.class);

  private final DynamoDbAsyncClient dynamoDbClient;

  public DynamoDbHealthCheck(DynamoDbAsyncClient dynamoDbClient) {
    this.dynamoDbClient = Objects.requireNonNull(dynamoDbClient);
  }

  /**
   * Checks the connected DynamoDB instance to ensure that it is healthy.
   *
   * @return healthy if the DynamoDB instance is reachable; unhealthy otherwise
   */
  @Override
  protected Result check() {
    LOG.info("Checking health of AWS DynamoDB...");

    return dynamoDbClient.listTables()
        .thenApply(response -> response.tableNames().size() > 0
            ? Result.healthy()
            : Result.unhealthy("No tables listed in Dynamo DB."))
        .exceptionally(throwable -> Result.unhealthy(throwable.getMessage()))
        .join();
  }
}
