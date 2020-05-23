package com.sanctionco.thunder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.sanctionco.thunder.authentication.Key;
import com.sanctionco.thunder.crypto.HashAlgorithm;
import com.sanctionco.thunder.dao.dynamodb.DynamoDbUsersDaoFactory;
import com.sanctionco.thunder.validation.PropertyValidationRule;

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
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
  private static final Validator VALIDATOR = Validators.newValidator();
  private static final YamlConfigurationFactory<ThunderConfiguration> FACTORY
      = new YamlConfigurationFactory<>(ThunderConfiguration.class, VALIDATOR, MAPPER, "dw");

  @Test
  void testFromYaml() throws Exception {
    ThunderConfiguration configuration = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/thunder-config.yaml").toURI()));

    assertTrue(configuration.getUsersDaoFactory() instanceof DynamoDbUsersDaoFactory);

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

    // This config should use the default hash configuration
    assertEquals(HashAlgorithm.SIMPLE, configuration.getHashConfiguration().getAlgorithm());
    assertFalse(configuration.getHashConfiguration().serverSideHash());
    assertTrue(configuration.getHashConfiguration().isHeaderCheckEnabled());
    assertFalse(configuration.getHashConfiguration().allowCommonMistakes());

    // This config should use the default OpenAPI configuration
    assertAll("OpenAPI configuration is correct",
        () -> assertTrue(configuration.getOpenApiConfiguration().isEnabled()),
        () -> assertEquals("Thunder API", configuration.getOpenApiConfiguration().getTitle()));
  }

  @Test
  void testFromYamlDisabledEmail() throws Exception {
    ThunderConfiguration configuration = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/thunder-config-disabled-email.yaml").toURI()));

    assertTrue(configuration.getUsersDaoFactory() instanceof DynamoDbUsersDaoFactory);

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

    // This config should use BCrypt as the hash algorithm
    assertEquals(HashAlgorithm.BCRYPT, configuration.getHashConfiguration().getAlgorithm());
  }

  @Test
  void testFromYamlDisabledOpenApi() throws Exception {
    ThunderConfiguration configuration = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/thunder-config-disabled-openapi.yaml").toURI()));

    assertFalse(configuration.getOpenApiConfiguration().isEnabled());
  }
}
