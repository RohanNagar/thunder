package com.sanctionco.thunder.dao.inmemorydb;

import com.sanctionco.thunder.TestResources;
import com.sanctionco.thunder.dao.UsersDaoFactory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryDbUsersDaoFactoryTest {

  @Test
  void testInMemoryDbCreation() {
    var usersDaoFactory = TestResources.readResourceYaml(
        UsersDaoFactory.class,
        "fixtures/configuration/dao/inmemorydb-config.yaml");

    assertTrue(usersDaoFactory instanceof InMemoryDbUsersDaoFactory);

    var healthCheck = usersDaoFactory.createHealthCheck();
    var usersDao = usersDaoFactory.createUsersDao(TestResources.MAPPER);

    assertTrue(healthCheck instanceof InMemoryDbHealthCheck);
    assertTrue(usersDao instanceof InMemoryDbUsersDao);
  }
}
