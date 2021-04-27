package com.sanctionco.thunder.dao;

import com.sanctionco.thunder.TestResources;
import com.sanctionco.thunder.dao.dynamodb.DynamoDbUsersDaoFactory;
import com.sanctionco.thunder.dao.mongodb.MongoDbUsersDaoFactory;

import io.dropwizard.jackson.DiscoverableSubtypeResolver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UsersDaoFactoryTest {

  @Test
  void isDiscoverable() {
    // Make sure the types we specified in META-INF gets picked up
    assertTrue(new DiscoverableSubtypeResolver().getDiscoveredSubtypes()
        .contains(DynamoDbUsersDaoFactory.class));
    assertTrue(new DiscoverableSubtypeResolver().getDiscoveredSubtypes()
        .contains(MongoDbUsersDaoFactory.class));
  }

  @Test
  void testDynamoDbFromYaml() {
    UsersDaoFactory usersDaoFactory = TestResources.readResourceYaml(
        UsersDaoFactory.class,
        "fixtures/configuration/dao/dynamodb-config.yaml");

    assertTrue(usersDaoFactory instanceof DynamoDbUsersDaoFactory);

    DynamoDbUsersDaoFactory dynamoDbUsersDaoFactory = (DynamoDbUsersDaoFactory) usersDaoFactory;
    assertEquals("http://test.dynamo.com", dynamoDbUsersDaoFactory.getEndpoint());
    assertEquals("test-region-1", dynamoDbUsersDaoFactory.getRegion());
    assertEquals("test-table", dynamoDbUsersDaoFactory.getTableName());
  }

  @Test
  void testMongoDbFromYaml() {
    UsersDaoFactory usersDaoFactory = TestResources.readResourceYaml(
        UsersDaoFactory.class,
        "fixtures/configuration/dao/mongodb-config.yaml");

    assertTrue(usersDaoFactory instanceof MongoDbUsersDaoFactory);

    MongoDbUsersDaoFactory mongoDbUsersDaoFactory = (MongoDbUsersDaoFactory) usersDaoFactory;
    assertEquals("mongodb://localhost:27017", mongoDbUsersDaoFactory.getConnectionString());
    assertEquals("test-db", mongoDbUsersDaoFactory.getDatabaseName());
    assertEquals("test-collection", mongoDbUsersDaoFactory.getCollectionName());
  }
}
