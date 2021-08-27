package com.sanctionco.thunder.dao;

import com.sanctionco.thunder.TestResources;
import com.sanctionco.thunder.dao.dynamodb.DynamoDbUsersDaoFactory;
import com.sanctionco.thunder.dao.inmemorydb.InMemoryDbUsersDaoFactory;
import com.sanctionco.thunder.dao.mongodb.MongoDbUsersDaoFactory;

import io.dropwizard.jackson.DiscoverableSubtypeResolver;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsersDaoFactoryTest {

  @Test
  void isDiscoverable() {
    var discoveredTypes = new DiscoverableSubtypeResolver().getDiscoveredSubtypes();

    // Make sure the types we specified in META-INF gets picked up
    assertTrue(discoveredTypes.containsAll(List.of(
        DynamoDbUsersDaoFactory.class,
        InMemoryDbUsersDaoFactory.class,
        MongoDbUsersDaoFactory.class)));
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
  void testInMemoryDbFromYaml() {
    UsersDaoFactory usersDaoFactory = TestResources.readResourceYaml(
        UsersDaoFactory.class,
        "fixtures/configuration/dao/inmemorydb-config.yaml");

    assertTrue(usersDaoFactory instanceof InMemoryDbUsersDaoFactory);

    InMemoryDbUsersDaoFactory inMemoryDaoFactory = (InMemoryDbUsersDaoFactory) usersDaoFactory;
    assertEquals(40, inMemoryDaoFactory.getMaxMemoryPercentage());
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
