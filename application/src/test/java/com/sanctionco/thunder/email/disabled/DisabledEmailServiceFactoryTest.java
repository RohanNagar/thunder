package com.sanctionco.thunder.email.disabled;

import com.sanctionco.thunder.TestResources;
import com.sanctionco.thunder.email.EmailServiceFactory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisabledEmailServiceFactoryTest {

  @Test
  void testFromYaml() {
    EmailServiceFactory serviceFactory = TestResources.readResourceYaml(
        EmailServiceFactory.class,
        "fixtures/configuration/email/disabled/config.yaml");

    assertTrue(serviceFactory instanceof DisabledEmailServiceFactory);

    assertFalse(serviceFactory.isEnabled());
    assertNull(serviceFactory.createEmailService(TestResources.METRICS));
    assertNull(serviceFactory.createHealthCheck());
  }
}
