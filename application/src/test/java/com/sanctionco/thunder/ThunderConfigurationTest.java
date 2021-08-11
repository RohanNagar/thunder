package com.sanctionco.thunder;

import com.sanctionco.thunder.authentication.basic.BasicAuthConfiguration;
import com.sanctionco.thunder.authentication.basic.Key;
import com.sanctionco.thunder.crypto.HashAlgorithm;
import com.sanctionco.thunder.dao.dynamodb.DynamoDbUsersDaoFactory;
import com.sanctionco.thunder.email.disabled.DisabledEmailServiceFactory;
import com.sanctionco.thunder.email.ses.SesEmailServiceFactory;
import com.sanctionco.thunder.secrets.local.EnvironmentSecretProvider;
import com.sanctionco.thunder.validation.PropertyValidationRule;

import java.time.Duration;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThunderConfigurationTest {

  @Test
  void testFromYaml() {
    ThunderConfiguration configuration = TestResources.readResourceYaml(
        ThunderConfiguration.class,
        "fixtures/configuration/thunder-config.yaml");

    assertTrue(configuration.getUsersDaoFactory() instanceof DynamoDbUsersDaoFactory);

    assertAll("Email configuration is correct",
        () -> assertTrue(configuration.getEmailServiceFactory().isEnabled()),
        () -> assertTrue(configuration.getEmailServiceFactory() instanceof SesEmailServiceFactory),
        () -> assertEquals("test.email.com",
            ((SesEmailServiceFactory) configuration.getEmailServiceFactory()).getEndpoint()),
        () -> assertEquals("test-region-2",
            ((SesEmailServiceFactory) configuration.getEmailServiceFactory()).getRegion()),
        () -> assertEquals("test@sanctionco.com",
            configuration.getEmailServiceFactory().getFromAddress()));

    assertNotNull(configuration.getEmailServiceFactory().getMessageOptionsConfiguration());

    assertAll("Auth configuration is correct",
        () -> assertTrue(configuration.getAuthConfiguration() instanceof BasicAuthConfiguration),
        () -> assertEquals(1,
            ((BasicAuthConfiguration) configuration.getAuthConfiguration()).getKeys().size()),
        () -> assertEquals(
            Collections.singletonList(new Key("test-app", "test-secret")),
            ((BasicAuthConfiguration) configuration.getAuthConfiguration()).getKeys()));

    assertTrue(configuration.getValidationConfiguration().allowSubset());
    assertFalse(configuration.getValidationConfiguration().allowSuperset());
    assertEquals(1, configuration.getValidationConfiguration().getValidationRules().size());
    assertEquals(
        new PropertyValidationRule("testProperty", "list"),
        configuration.getValidationConfiguration().getValidationRules().get(0));

    // This config should use the default hash configuration
    assertEquals(HashAlgorithm.SIMPLE, configuration.getHashConfiguration().getAlgorithm());
    assertFalse(configuration.getHashConfiguration().serverSideHash());
    assertTrue(configuration.getHashConfiguration().isHeaderCheckEnabled());
    assertFalse(configuration.getHashConfiguration().allowCommonMistakes());

    // This config should use the default secret fetcher
    assertTrue(configuration.getSecretProvider() instanceof EnvironmentSecretProvider);

    // This config should use the default OpenAPI configuration
    assertAll("OpenAPI configuration is correct",
        () -> assertTrue(configuration.getOpenApiConfiguration().isEnabled()),
        () -> assertEquals("Thunder API", configuration.getOpenApiConfiguration().getTitle()));

    // This config should use the default request options
    assertEquals(Duration.ofSeconds(30), configuration.getRequestOptions().operationTimeout());
  }

  @Test
  void testFromYamlDisabledEmail() {
    ThunderConfiguration configuration = TestResources.readResourceYaml(
        ThunderConfiguration.class,
        "fixtures/configuration/thunder-config-disabled-email.yaml");

    assertTrue(configuration.getUsersDaoFactory() instanceof DynamoDbUsersDaoFactory);

    assertAll("Email configuration is disabled",
        () -> assertFalse(configuration.getEmailServiceFactory().isEnabled()),
        () -> assertTrue(configuration.getEmailServiceFactory()
            instanceof DisabledEmailServiceFactory),
        () -> assertNull(configuration.getEmailServiceFactory().getFromAddress()),
        () -> assertNull(configuration.getEmailServiceFactory().getMessageOptionsConfiguration()));

    assertAll("Auth configuration is correct",
        () -> assertTrue(configuration.getAuthConfiguration() instanceof BasicAuthConfiguration),
        () -> assertEquals(1,
            ((BasicAuthConfiguration) configuration.getAuthConfiguration()).getKeys().size()),
        () -> assertEquals(
            Collections.singletonList(new Key("test-app", "test-secret")),
            ((BasicAuthConfiguration) configuration.getAuthConfiguration()).getKeys()));

    assertFalse(configuration.getValidationConfiguration().allowSubset());
    assertTrue(configuration.getValidationConfiguration().allowSuperset());
    assertEquals(1, configuration.getValidationConfiguration().getValidationRules().size());
    assertEquals(
        new PropertyValidationRule("testProperty", "list"),
        configuration.getValidationConfiguration().getValidationRules().get(0));

    // This config should use BCrypt as the hash algorithm
    assertEquals(HashAlgorithm.BCRYPT, configuration.getHashConfiguration().getAlgorithm());

    // This config should use an explicit local secrets fetcher
    assertTrue(configuration.getSecretProvider() instanceof EnvironmentSecretProvider);

    // This config should have a 20s default timeout
    assertEquals(Duration.ofSeconds(20), configuration.getRequestOptions().operationTimeout());
  }

  @Test
  void testFromYamlNullEmail() {
    ThunderConfiguration configuration = TestResources.readResourceYaml(
        ThunderConfiguration.class,
        "fixtures/configuration/thunder-config-null-email.yaml");

    assertTrue(configuration.getEmailServiceFactory() instanceof DisabledEmailServiceFactory);
  }

  @Test
  void testFromYamlDisabledOpenApi() {
    ThunderConfiguration configuration = TestResources.readResourceYaml(
        ThunderConfiguration.class,
        "fixtures/configuration/thunder-config-disabled-openapi.yaml");

    assertFalse(configuration.getOpenApiConfiguration().isEnabled());
  }
}
