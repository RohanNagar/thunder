package com.sanctionco.thunder.dao.dynamodb;

import com.sanctionco.thunder.TestResources;
import com.sanctionco.thunder.dao.UsersDaoFactory;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DynamoDbUsersDaoFactoryTest {

  @Test
  void testDynamoDbCreation() {
    UsersDaoFactory usersDaoFactory = TestResources.readResourceYaml(
        UsersDaoFactory.class,
        "fixtures/configuration/dao/dynamodb-config.yaml");

    assertTrue(usersDaoFactory instanceof DynamoDbUsersDaoFactory);

    var healthCheck = usersDaoFactory.createHealthCheck();

    assertTrue(healthCheck instanceof DynamoDbHealthCheck);
  }

  @Test
  void testDynamoClientCreatedOnce() {
    UsersDaoFactory usersDaoFactory = TestResources.readResourceYaml(
        UsersDaoFactory.class,
        "fixtures/configuration/dao/dynamodb-config.yaml");

    assertTrue(usersDaoFactory instanceof DynamoDbUsersDaoFactory);

    DynamoDbUsersDaoFactory dynamoDbUsersDaoFactory = (DynamoDbUsersDaoFactory) usersDaoFactory;

    // Create healthcheck twice. The first one should create the DynamoDB instance
    // and the second should re-use the created one.
    usersDaoFactory.createHealthCheck();
    DynamoDbAsyncClient createdClientAfterOne = dynamoDbUsersDaoFactory.dynamoDbClient;

    usersDaoFactory.createHealthCheck();
    DynamoDbAsyncClient createdClientAfterTwo = dynamoDbUsersDaoFactory.dynamoDbClient;

    assertSame(createdClientAfterOne, createdClientAfterTwo);
  }

  @Test
  void testCreateUsersDaoTableExists() {
    UsersDaoFactory usersDaoFactory = TestResources.readResourceYaml(
        UsersDaoFactory.class,
        "fixtures/configuration/dao/dynamodb-config.yaml");

    assertTrue(usersDaoFactory instanceof DynamoDbUsersDaoFactory);

    DynamoDbUsersDaoFactory dynamoDbUsersDaoFactory = (DynamoDbUsersDaoFactory) usersDaoFactory;

    // Set the client to already be created
    DynamoDbAsyncClient client = mock(DynamoDbAsyncClient.class);
    when(client.listTables())
        .thenReturn(CompletableFuture.completedFuture(
            ListTablesResponse.builder().tableNames("test-table").build()));

    dynamoDbUsersDaoFactory.dynamoDbClient = client;

    usersDaoFactory.createUsersDao(TestResources.MAPPER);

    // List tables would have returned the right table
    verify(client, times(1)).listTables();
    verify(client, times(0)).createTable(any(CreateTableRequest.class));
  }

  @Test
  void testCreateUsersDaoTableNotExists() {
    UsersDaoFactory usersDaoFactory = TestResources.readResourceYaml(
        UsersDaoFactory.class,
        "fixtures/configuration/dao/dynamodb-config.yaml");

    assertTrue(usersDaoFactory instanceof DynamoDbUsersDaoFactory);

    DynamoDbUsersDaoFactory dynamoDbUsersDaoFactory = (DynamoDbUsersDaoFactory) usersDaoFactory;

    // Set the client to already be created
    DynamoDbAsyncClient client = mock(DynamoDbAsyncClient.class);
    when(client.listTables())
        .thenReturn(CompletableFuture.completedFuture(
            ListTablesResponse.builder().tableNames("wrong-test-table").build()));

    dynamoDbUsersDaoFactory.dynamoDbClient = client;

    usersDaoFactory.createUsersDao(TestResources.MAPPER);

    // The table should have been created
    verify(client, times(1)).listTables();
    verify(client, times(1)).createTable(any(CreateTableRequest.class));
  }
}
