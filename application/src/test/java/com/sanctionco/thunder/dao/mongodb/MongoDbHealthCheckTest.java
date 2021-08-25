package com.sanctionco.thunder.dao.mongodb;

import com.codahale.metrics.health.HealthCheck;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class MongoDbHealthCheckTest extends HealthCheck {

  @Test
  void testNullConstructorArgumentThrows() {
    assertThrows(NullPointerException.class,
        () -> new MongoDbHealthCheck(null));
  }

  @Test
  void testHealthy() {
    var client = mock(MongoClient.class);
    var iterable = mock(MongoIterable.class);
    var cursor = mock(MongoCursor.class);

    when(client.listDatabaseNames()).thenReturn(iterable);
    when(iterable.iterator()).thenReturn(cursor);
    when(cursor.hasNext()).thenReturn(true);

    MongoDbHealthCheck healthCheck = new MongoDbHealthCheck(client);

    assertTrue(healthCheck.check()::isHealthy);
  }

  @Test
  void testUnhealthy() {
    var client = mock(MongoClient.class);
    var iterable = mock(MongoIterable.class);
    var cursor = mock(MongoCursor.class);

    when(client.listDatabaseNames()).thenReturn(iterable);
    when(iterable.iterator()).thenReturn(cursor);
    when(cursor.hasNext()).thenReturn(false);

    MongoDbHealthCheck healthCheck = new MongoDbHealthCheck(client);

    assertFalse(healthCheck.check()::isHealthy);
  }

  // Not used - exists in order to extend HealthCheck
  @Override protected Result check() {
    return Result.healthy();
  }
}
