package com.sanctionco.thunder.openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.swagger.v3.oas.integration.SwaggerConfiguration;

import java.io.File;

import javax.validation.Validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenApiConfigurationTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
  private static final Validator VALIDATOR = Validators.newValidator();
  private static final YamlConfigurationFactory<OpenApiConfiguration> FACTORY
      = new YamlConfigurationFactory<>(OpenApiConfiguration.class, VALIDATOR, MAPPER, "dw");

  @Test
  void testFromYaml() throws Exception {
    OpenApiConfiguration configuration = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/openapi/valid-config.yaml").toURI()));

    assertAll("OpenAPI configuration is correct",
        () -> assertFalse(configuration.isEnabled()),
        () -> assertEquals("Test Title", configuration.getTitle()),
        () -> assertEquals("100.0.0", configuration.getVersion()),
        () -> assertEquals("Test Description", configuration.getDescription()),
        () -> assertEquals("Test Contact Name", configuration.getContact()),
        () -> assertEquals("Test Contact Email", configuration.getContactEmail()),
        () -> assertEquals("Test License", configuration.getLicense()),
        () -> assertEquals("Test License URL", configuration.getLicenseUrl())
    );
  }

  @Test
  void testFromYamlOnlyTitle() throws Exception {
    OpenApiConfiguration configuration = FACTORY.build(new File(Resources.getResource(
        "fixtures/configuration/openapi/only-title.yaml").toURI()));

    assertAll("OpenAPI configuration is correct",
        () -> assertTrue(configuration.isEnabled()),
        () -> assertEquals("My New Title", configuration.getTitle()),
        () -> assertEquals("3.0.0", configuration.getVersion()),
        () -> assertEquals("A fully customizable user management REST API",
            configuration.getDescription()),
        () -> assertNull(configuration.getContact()),
        () -> assertNull(configuration.getContactEmail()),
        () -> assertEquals("MIT", configuration.getLicense()),
        () -> assertEquals("https://github.com/RohanNagar/thunder/blob/master/LICENSE.md",
            configuration.getLicenseUrl())
    );
  }

  @Test
  void testBuild() {
    OpenApiConfiguration configuration = new OpenApiConfiguration();

    SwaggerConfiguration swaggerConfiguration = configuration.build();

    assertAll("Swagger is configured correctly",
        () -> assertTrue(swaggerConfiguration.isPrettyPrint()),
        () -> assertTrue(swaggerConfiguration.isReadAllResources()),
        () -> assertTrue(swaggerConfiguration.getResourcePackages()
            .contains("com.sanctionco.thunder.resources")),
        () -> assertTrue(swaggerConfiguration.getIgnoredRoutes().contains("/swagger")),
        () -> assertEquals("users", swaggerConfiguration.getOpenAPI().getTags().get(0).getName()),
        () -> assertEquals("verify", swaggerConfiguration.getOpenAPI().getTags().get(1).getName())
    );
  }
}
