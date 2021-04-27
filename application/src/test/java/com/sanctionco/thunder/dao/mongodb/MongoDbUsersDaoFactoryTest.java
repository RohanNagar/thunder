package com.sanctionco.thunder.dao.mongodb;

import com.google.common.io.Resources;
import com.sanctionco.thunder.TestResources;
import com.sanctionco.thunder.dao.UsersDaoFactory;

import io.dropwizard.configuration.YamlConfigurationFactory;

import java.io.File;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MongoDbUsersDaoFactoryTest {
  private static final YamlConfigurationFactory<UsersDaoFactory> FACTORY =
      new YamlConfigurationFactory<>(
          UsersDaoFactory.class, TestResources.VALIDATOR, TestResources.MAPPER, "dw");

  @Test
  void testMongoDbCreation() throws Exception {
    UsersDaoFactory usersDaoFactory = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/dao/mongodb-config.yaml").toURI()));

    assertTrue(usersDaoFactory instanceof MongoDbUsersDaoFactory);

    var healthCheck = usersDaoFactory.createHealthCheck();
    var usersDao = usersDaoFactory.createUsersDao(TestResources.MAPPER);

    assertTrue(healthCheck instanceof MongoDbHealthCheck);
    assertTrue(usersDao instanceof MongoDbUsersDao);
  }
}
