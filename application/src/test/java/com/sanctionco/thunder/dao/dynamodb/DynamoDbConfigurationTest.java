package com.sanctionco.thunder.dao.dynamodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;

import java.io.File;

import javax.validation.Validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DynamoDbConfigurationTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
  private static final Validator VALIDATOR = Validators.newValidator();
  private static final YamlConfigurationFactory<DynamoDbConfiguration> FACTORY
      = new YamlConfigurationFactory<>(DynamoDbConfiguration.class, VALIDATOR, MAPPER, "dw");

  @Test
  void testFromYaml() throws Exception {
    DynamoDbConfiguration configuration = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/dao/dynamodb-config.yaml").toURI()));

    assertAll("All configuration options are set",
        () -> assertEquals("test.dynamo.com", configuration.getEndpoint()),
        () -> assertEquals("test-region-1", configuration.getRegion()),
        () -> assertEquals("test-table", configuration.getTableName()));
  }
}
