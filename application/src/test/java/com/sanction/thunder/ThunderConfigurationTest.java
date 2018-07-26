package com.sanction.thunder;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.io.Resources;

import com.sanction.thunder.authentication.Key;
import com.sanction.thunder.validation.PropertyValidationRule;

import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;

import java.io.File;
import java.util.Collections;
import javax.validation.Validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThunderConfigurationTest {
  private final ObjectMapper mapper = Jackson.newObjectMapper();
  private final Validator validator = Validators.newValidator();
  private final YamlConfigurationFactory<ThunderConfiguration> factory
      = new YamlConfigurationFactory<>(ThunderConfiguration.class, validator, mapper, "dw");

  @Test
  void testFromYaml() throws Exception {
    ThunderConfiguration configuration = factory.build(new File(Resources.getResource(
        "fixtures/configuration/thunder-config.yaml").toURI()));

    assertAll("DynamoDbConfiguration exists",
        () -> assertNotNull(configuration.getDynamoConfiguration()),
        () -> assertEquals("test.dynamo.com", configuration.getDynamoConfiguration().getEndpoint()),
        () -> assertEquals("test-region-1", configuration.getDynamoConfiguration().getRegion()),
        () -> assertEquals("test-table", configuration.getDynamoConfiguration().getTableName()));

    assertAll("Email configuration is correct",
        () -> assertTrue(configuration.getEmailConfiguration().isEnabled()),
        () -> assertEquals("test.email.com", configuration.getEmailConfiguration().getEndpoint()),
        () -> assertEquals("test-region-2", configuration.getEmailConfiguration().getRegion()),
        () -> assertEquals("test@sanctionco.com",
            configuration.getEmailConfiguration().getFromAddress()));

    assertNotNull(configuration.getEmailConfiguration().getMessageOptionsConfiguration());

    assertEquals(1, configuration.getApprovedKeys().size());
    assertEquals(
        Collections.singletonList(new Key("test-app", "test-secret")),
        configuration.getApprovedKeys());

    assertEquals(1, configuration.getValidationRules().size());
    assertEquals(
        new PropertyValidationRule("testProperty", "list"),
        configuration.getValidationRules().get(0));
  }

  @Test
  void testFromYamlDisabledEmail() throws Exception {
    ThunderConfiguration configuration = factory.build(new File(Resources.getResource(
        "fixtures/configuration/thunder-config-disabled-email.yaml").toURI()));

    assertAll("DynamoDbConfiguration exists",
        () -> assertNotNull(configuration.getDynamoConfiguration()),
        () -> assertEquals("test.dynamo.com", configuration.getDynamoConfiguration().getEndpoint()),
        () -> assertEquals("test-region-1", configuration.getDynamoConfiguration().getRegion()),
        () -> assertEquals("test-table", configuration.getDynamoConfiguration().getTableName()));

    assertAll("Email configuration is disabled",
        () -> assertFalse(configuration.getEmailConfiguration().isEnabled()),
        () -> assertNull(configuration.getEmailConfiguration().getEndpoint()),
        () -> assertNull(configuration.getEmailConfiguration().getRegion()),
        () -> assertNull(configuration.getEmailConfiguration().getFromAddress()),
        () -> assertNull(configuration.getEmailConfiguration().getMessageOptionsConfiguration()));

    assertEquals(1, configuration.getApprovedKeys().size());
    assertEquals(
        Collections.singletonList(new Key("test-app", "test-secret")),
        configuration.getApprovedKeys());

    assertEquals(1, configuration.getValidationRules().size());
    assertEquals(
        new PropertyValidationRule("testProperty", "list"),
        configuration.getValidationRules().get(0));
  }
}
