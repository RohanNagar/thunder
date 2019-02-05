package com.sanctionco.thunder.dao.dynamodb;

import com.codahale.metrics.health.HealthCheck;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DynamoDbHealthCheckTest extends HealthCheck {
  private static final DynamoDbClient client = mock(DynamoDbClient.class);

  private final DynamoDbHealthCheck healthCheck = new DynamoDbHealthCheck(client);

  @Test
  void testNullConstructorArgumentThrows() {
    assertThrows(NullPointerException.class,
        () -> new DynamoDbHealthCheck(null));
  }

  @Test
  void testCheckHealthy() {
    when(client.listTables()).thenReturn(
        ListTablesResponse.builder()
            .tableNames(Collections.singletonList("testTable"))
            .build());

    assertTrue(healthCheck.check()::isHealthy);
  }

  @Test
  void testCheckUnhealthy() {
    when(client.listTables()).thenReturn(
        ListTablesResponse.builder()
            .tableNames(Collections.emptyList())
            .build());

    assertFalse(healthCheck.check()::isHealthy);
  }

  // Not used - exists in order to extend HealthCheck
  @Override protected Result check() {
    return Result.healthy();
  }
}
