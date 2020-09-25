package com.sanctionco.thunder.email.disabled;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.sanctionco.thunder.email.EmailServiceFactory;

import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;

import java.io.File;

import javax.validation.Validator;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

class DisabledEmailServiceFactoryTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
  private static final Validator VALIDATOR = Validators.newValidator();
  private static final YamlConfigurationFactory<EmailServiceFactory> FACTORY
      = new YamlConfigurationFactory<>(EmailServiceFactory.class, VALIDATOR, MAPPER, "dw");

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
