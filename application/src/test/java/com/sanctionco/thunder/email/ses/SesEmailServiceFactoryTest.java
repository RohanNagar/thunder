package com.sanctionco.thunder.email.ses;

import com.sanctionco.thunder.TestResources;
import com.sanctionco.thunder.email.EmailServiceFactory;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.ses.SesClient;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SesEmailServiceFactoryTest {

  @Test
  void testFromYaml() {
    EmailServiceFactory serviceFactory = TestResources.readResourceYaml(
        EmailServiceFactory.class,
        "fixtures/configuration/email/valid-config.yaml");

    assertTrue(serviceFactory instanceof SesEmailServiceFactory);

    var emailService = serviceFactory.createEmailService(TestResources.METRICS);
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
  void testSesClientCreatedOnce() {
    EmailServiceFactory serviceFactory = TestResources.readResourceYaml(
        EmailServiceFactory.class,
        "fixtures/configuration/email/valid-config.yaml");

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
    TestResources.readResourceYaml(
        EmailServiceFactory.class,
        "fixtures/configuration/email/null-endpoint.yaml",
        true);

    TestResources.readResourceYaml(
        EmailServiceFactory.class,
        "fixtures/configuration/email/empty-endpoint.yaml",
        true);

    TestResources.readResourceYaml(
        EmailServiceFactory.class,
        "fixtures/configuration/email/null-region.yaml",
        true);

    TestResources.readResourceYaml(
        EmailServiceFactory.class,
        "fixtures/configuration/email/empty-region.yaml",
        true);

    TestResources.readResourceYaml(
        EmailServiceFactory.class,
        "fixtures/configuration/email/null-from-address.yaml",
        true);

    TestResources.readResourceYaml(
        EmailServiceFactory.class,
        "fixtures/configuration/email/empty-from-address.yaml",
        true);
  }
}
