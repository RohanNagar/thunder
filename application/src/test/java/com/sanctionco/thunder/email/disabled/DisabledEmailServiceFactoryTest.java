package com.sanctionco.thunder.email.disabled;

import com.codahale.metrics.MetricRegistry;
import com.google.common.io.Resources;
import com.sanctionco.thunder.TestResources;
import com.sanctionco.thunder.email.EmailServiceFactory;

import io.dropwizard.configuration.YamlConfigurationFactory;

import java.io.File;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisabledEmailServiceFactoryTest {
  private static final YamlConfigurationFactory<EmailServiceFactory> FACTORY
      = new YamlConfigurationFactory<>(
          EmailServiceFactory.class, TestResources.VALIDATOR, TestResources.MAPPER, "dw");

  @Test
  void testFromYaml() throws Exception {
    EmailServiceFactory serviceFactory = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/email/disabled/config.yaml").toURI()));

    assertTrue(serviceFactory instanceof DisabledEmailServiceFactory);

    assertFalse(serviceFactory.isEnabled());
    assertNull(serviceFactory.createEmailService(new MetricRegistry()));
    assertNull(serviceFactory.createHealthCheck());
  }
}
