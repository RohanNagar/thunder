package com.sanction.thunder.dynamodb;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.codahale.metrics.health.HealthCheck;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

public class DynamoDbHealthCheck extends HealthCheck {
  private final DynamoDB dynamo;

  @Inject
  public DynamoDbHealthCheck(DynamoDB dynamo) {
    this.dynamo = checkNotNull(dynamo);
  }

  @Override
  protected Result check() {
    return dynamo.listTables().firstPage().size() > 0
        ? Result.healthy()
        : Result.unhealthy("No tables in Dynamo DB");
  }

}
