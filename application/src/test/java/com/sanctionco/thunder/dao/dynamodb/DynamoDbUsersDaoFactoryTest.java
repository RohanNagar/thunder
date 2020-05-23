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
    var usersDao = usersDaoFactory.createUsersDao(OBJECT_MAPPER);

    assertTrue(healthCheck instanceof DynamoDbHealthCheck);
    assertTrue(usersDao instanceof DynamoDbUsersDao);
  }
}
