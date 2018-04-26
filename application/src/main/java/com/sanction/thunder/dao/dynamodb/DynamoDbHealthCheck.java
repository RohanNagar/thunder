package com.sanction.thunder.dao.dynamodb;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;

import com.codahale.metrics.health.HealthCheck;

import java.util.Objects;
import javax.inject.Inject;

public class DynamoDbHealthCheck extends HealthCheck {
  private final DynamoDB dynamo;

  @Inject
  public DynamoDbHealthCheck(DynamoDB dynamo) {
    this.dynamo = Objects.requireNonNull(dynamo);
  }

  @Override
  protected Result check() {
    return dynamo.listTables().firstPage().size() > 0
        ? Result.healthy()
        : Result.unhealthy("No tables in Dynamo DB");
  }
}
