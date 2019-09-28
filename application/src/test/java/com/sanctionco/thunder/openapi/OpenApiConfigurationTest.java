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
  private final ObjectMapper mapper = Jackson.newObjectMapper();
  private final Validator validator = Validators.newValidator();
  private final YamlConfigurationFactory<OpenApiConfiguration> factory
      = new YamlConfigurationFactory<>(OpenApiConfiguration.class, validator, mapper, "dw");

  @Test
  void testFromYaml() throws Exception {
    OpenApiConfiguration configuration = factory.build(new File(Resources.getResource(
        "fixtures/configuration/openapi/valid-config.yaml").toURI()));

    assertAll("OpenAPI configuration is correct",
        () -> assertFalse(configuration.isEnabled()),
        () -> assertEquals("com.sanctionco.thunder.resources", configuration.getResourcePackage()),
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
  void testFromYamlOnlyResources() throws Exception {
    OpenApiConfiguration configuration = factory.build(new File(Resources.getResource(
        "fixtures/configuration/openapi/only-resources.yaml").toURI()));

    assertAll("OpenAPI configuration is correct",
        () -> assertTrue(configuration.isEnabled()),
        () -> assertEquals("com.sanctionco.thunder.openapi", configuration.getResourcePackage()),
        () -> assertEquals("Thunder API", configuration.getTitle()),
        () -> assertEquals("2.2.0", configuration.getVersion()),
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
