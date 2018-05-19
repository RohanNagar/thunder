package com.sanction.thunder.dao.dynamodb;

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
  private final ObjectMapper mapper = Jackson.newObjectMapper();
  private final Validator validator = Validators.newValidator();
  private final YamlConfigurationFactory<DynamoDbConfiguration> factory
      = new YamlConfigurationFactory<>(DynamoDbConfiguration.class, validator, mapper, "dw");

  @Test
  void testFromYaml() throws Exception {
    DynamoDbConfiguration configuration = factory.build(new File(Resources.getResource(
        "fixtures/configuration/dynamodb-config.yaml").toURI()));

    assertAll("All configuration options are set",
        () -> assertEquals("test.dynamo.com", configuration.getEndpoint()),
        () -> assertEquals("test-region-1", configuration.getRegion()),
        () -> assertEquals("test-table", configuration.getTableName()));
  }
}
