package com.sanctionco.thunder.email.ses;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.sanctionco.thunder.email.EmailServiceFactory;

import io.dropwizard.configuration.ConfigurationValidationException;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;

import java.io.File;

import javax.validation.Validator;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.ses.SesClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SesEmailServiceFactoryTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
  private static final Validator VALIDATOR = Validators.newValidator();
  private static final YamlConfigurationFactory<EmailServiceFactory> FACTORY
      = new YamlConfigurationFactory<>(EmailServiceFactory.class, VALIDATOR, MAPPER, "dw");

  @Test
  void testFromYaml() throws Exception {
    EmailServiceFactory serviceFactory = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/email/valid-config.yaml").toURI()));

    assertTrue(serviceFactory instanceof SesEmailServiceFactory);

    var emailService = serviceFactory.createEmailService(new MetricRegistry());
    var healthCheck = serviceFactory.createHealthCheck();

    assertTrue(emailService instanceof SesEmailService);
    assertTrue(healthCheck instanceof SesHealthCheck);

    SesEmailServiceFactory sesServiceFactory = (SesEmailServiceFactory) serviceFactory;

    assertAll("Email configuration is correct",
        () -> assertTrue(sesServiceFactory.isEnabled()),
        () -> assertEquals("http://test.email.com", sesServiceFactory.getEndpoint()),
        () -> assertEquals("test-region-2", sesServiceFactory.getRegion()),
        () -> assertEquals("test@sanctionco.com", sesServiceFactory.getFromAddress()),
        () -> assertNotNull(sesServiceFactory.getMessageOptionsConfiguration()));
  }

  @Test
  void testSesClientCreatedOnce() throws Exception {
    EmailServiceFactory serviceFactory = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/email/valid-config.yaml").toURI()));

    assertTrue(serviceFactory instanceof SesEmailServiceFactory);

    SesEmailServiceFactory sesServiceFactory = (SesEmailServiceFactory) serviceFactory;

    sesServiceFactory.createHealthCheck();
    SesClient createdClientAfterOne = sesServiceFactory.sesClient;

    sesServiceFactory.createHealthCheck();
    SesClient createdClientAfterTwo = sesServiceFactory.sesClient;

    assertSame(createdClientAfterOne, createdClientAfterTwo);
  }

  @Test
  void testInvalidConfig() {
    assertThrows(ConfigurationValidationException.class,
        () -> FACTORY.build(new File(Resources.getResource(
            "fixtures/configuration/email/null-endpoint.yaml").toURI())));

    assertThrows(ConfigurationValidationException.class,
        () -> FACTORY.build(new File(Resources.getResource(
            "fixtures/configuration/email/empty-endpoint.yaml").toURI())));

    assertThrows(ConfigurationValidationException.class,
        () -> FACTORY.build(new File(Resources.getResource(
            "fixtures/configuration/email/null-region.yaml").toURI())));

    assertThrows(ConfigurationValidationException.class,
        () -> FACTORY.build(new File(Resources.getResource(
            "fixtures/configuration/email/empty-region.yaml").toURI())));

    assertThrows(ConfigurationValidationException.class,
        () -> FACTORY.build(new File(Resources.getResource(
            "fixtures/configuration/email/null-from-address.yaml").toURI())));

    assertThrows(ConfigurationValidationException.class,
        () -> FACTORY.build(new File(Resources.getResource(
            "fixtures/configuration/email/empty-from-address.yaml").toURI())));
  }
}
