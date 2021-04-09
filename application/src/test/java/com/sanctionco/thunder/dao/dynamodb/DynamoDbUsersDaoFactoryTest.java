package com.sanctionco.thunder.dao.dynamodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.sanctionco.thunder.dao.UsersDaoFactory;

import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;

import java.io.File;

import javax.validation.Validator;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
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
  private static final ObjectMapper OBJECT_MAPPER = Jackson.newObjectMapper();
  private static final Validator VALIDATOR = Validators.newValidator();
  private static final YamlConfigurationFactory<UsersDaoFactory> FACTORY =
      new YamlConfigurationFactory<>(UsersDaoFactory.class, VALIDATOR, OBJECT_MAPPER, "dw");

  @Test
  void testDynamoDbCreation() throws Exception {
    UsersDaoFactory usersDaoFactory = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/dao/dynamodb-config.yaml").toURI()));

    assertTrue(usersDaoFactory instanceof DynamoDbUsersDaoFactory);

    var healthCheck = usersDaoFactory.createHealthCheck();

    assertTrue(healthCheck instanceof DynamoDbHealthCheck);
  }

  @Test
  void testDynamoClientCreatedOnce() throws Exception {
    UsersDaoFactory usersDaoFactory = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/dao/dynamodb-config.yaml").toURI()));

    assertTrue(usersDaoFactory instanceof DynamoDbUsersDaoFactory);

    DynamoDbUsersDaoFactory dynamoDbUsersDaoFactory = (DynamoDbUsersDaoFactory) usersDaoFactory;

    // Create healthcheck twice. The first one should create the DynamoDB instance
    // and the second should re-use the created one.
    usersDaoFactory.createHealthCheck();
    DynamoDbClient createdClientAfterOne = dynamoDbUsersDaoFactory.dynamoDbClient;

    usersDaoFactory.createHealthCheck();
    DynamoDbClient createdClientAfterTwo = dynamoDbUsersDaoFactory.dynamoDbClient;

    assertSame(createdClientAfterOne, createdClientAfterTwo);
  }

  @Test
  void testCreateUsersDaoTableExists() throws Exception {
    UsersDaoFactory usersDaoFactory = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/dao/dynamodb-config.yaml").toURI()));

    assertTrue(usersDaoFactory instanceof DynamoDbUsersDaoFactory);

    DynamoDbUsersDaoFactory dynamoDbUsersDaoFactory = (DynamoDbUsersDaoFactory) usersDaoFactory;

    // Set the client to already be created
    DynamoDbClient client = mock(DynamoDbClient.class);
    when(client.listTables())
        .thenReturn(ListTablesResponse.builder().tableNames("test-table").build());

    dynamoDbUsersDaoFactory.dynamoDbClient = client;

    usersDaoFactory.createUsersDao(OBJECT_MAPPER);

    // List tables would have returned the right table
    verify(client, times(1)).listTables();
    verify(client, times(0)).createTable(any(CreateTableRequest.class));
  }

  @Test
  void testCreateUsersDaoTableNotExists() throws Exception {
    UsersDaoFactory usersDaoFactory = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/dao/dynamodb-config.yaml").toURI()));

    assertTrue(usersDaoFactory instanceof DynamoDbUsersDaoFactory);

    DynamoDbUsersDaoFactory dynamoDbUsersDaoFactory = (DynamoDbUsersDaoFactory) usersDaoFactory;

    // Set the client to already be created
    DynamoDbClient client = mock(DynamoDbClient.class);
    when(client.listTables())
        .thenReturn(ListTablesResponse.builder().tableNames("wrong-test-table").build());

    dynamoDbUsersDaoFactory.dynamoDbClient = client;

    usersDaoFactory.createUsersDao(OBJECT_MAPPER);

    // The table should have been created
    verify(client, times(1)).listTables();
    verify(client, times(1)).createTable(any(CreateTableRequest.class));
  }
}
