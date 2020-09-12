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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

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
}
