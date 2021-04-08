package com.sanctionco.thunder.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.sanctionco.thunder.dao.dynamodb.DynamoDbUsersDaoFactory;
import com.sanctionco.thunder.dao.mongodb.MongoDbUsersDaoFactory;

import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;

import java.io.File;

import javax.validation.Validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UsersDaoFactoryTest {
  private static final ObjectMapper OBJECT_MAPPER = Jackson.newObjectMapper();
  private static final Validator VALIDATOR = Validators.newValidator();
  private static final YamlConfigurationFactory<UsersDaoFactory> FACTORY =
      new YamlConfigurationFactory<>(UsersDaoFactory.class, VALIDATOR, OBJECT_MAPPER, "dw");

  @Test
  void isDiscoverable() {
    // Make sure the types we specified in META-INF gets picked up
    assertTrue(new DiscoverableSubtypeResolver().getDiscoveredSubtypes()
        .contains(DynamoDbUsersDaoFactory.class));
    assertTrue(new DiscoverableSubtypeResolver().getDiscoveredSubtypes()
        .contains(MongoDbUsersDaoFactory.class));
  }

  @Test
  void testDynamoDbFromYaml() throws Exception {
    UsersDaoFactory usersDaoFactory = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/dao/dynamodb-config.yaml").toURI()));

    assertTrue(usersDaoFactory instanceof DynamoDbUsersDaoFactory);

    DynamoDbUsersDaoFactory dynamoDbUsersDaoFactory = (DynamoDbUsersDaoFactory) usersDaoFactory;
    assertEquals("http://test.dynamo.com", dynamoDbUsersDaoFactory.getEndpoint());
    assertEquals("test-region-1", dynamoDbUsersDaoFactory.getRegion());
    assertEquals("test-table", dynamoDbUsersDaoFactory.getTableName());
  }

  @Test
  void testMongoDbFromYaml() throws Exception {
    UsersDaoFactory usersDaoFactory = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/dao/mongodb-config.yaml").toURI()));

    assertTrue(usersDaoFactory instanceof MongoDbUsersDaoFactory);

    MongoDbUsersDaoFactory mongoDbUsersDaoFactory = (MongoDbUsersDaoFactory) usersDaoFactory;
    assertEquals("mongodb://localhost:27017", mongoDbUsersDaoFactory.getConnectionString());
    assertEquals("test-db", mongoDbUsersDaoFactory.getDatabaseName());
    assertEquals("test-collection", mongoDbUsersDaoFactory.getCollectionName());
  }
}
