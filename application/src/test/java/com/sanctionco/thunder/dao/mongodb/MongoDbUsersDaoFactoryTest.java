package com.sanctionco.thunder.dao.mongodb;

import com.sanctionco.thunder.TestResources;
import com.sanctionco.thunder.dao.UsersDaoFactory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MongoDbUsersDaoFactoryTest {

  @Test
  void testMongoDbCreation() {
    UsersDaoFactory usersDaoFactory = TestResources.readResourceYaml(
        UsersDaoFactory.class,
        "fixtures/configuration/dao/mongodb-config.yaml");

    assertTrue(usersDaoFactory instanceof MongoDbUsersDaoFactory);

    var healthCheck = usersDaoFactory.createHealthCheck();
    var usersDao = usersDaoFactory.createUsersDao(TestResources.MAPPER);

    assertTrue(healthCheck instanceof MongoDbHealthCheck);
    assertTrue(usersDao instanceof MongoDbUsersDao);
  }
}
