package com.sanctionco.thunder.openapi;

import com.sanctionco.thunder.TestResources;

import io.swagger.v3.oas.integration.SwaggerConfiguration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenApiConfigurationTest {

  @Test
  void testFromYaml() {
    OpenApiConfiguration configuration = TestResources.readResourceYaml(
        OpenApiConfiguration.class,
        "fixtures/configuration/openapi/valid-config.yaml");

    String expectedVersion = TestResources.readResourceFile("version.txt");

    assertAll("OpenAPI configuration is correct",
        () -> assertFalse(configuration.isEnabled()),
        () -> assertEquals("Test Title", configuration.getTitle()),
        () -> assertEquals(expectedVersion, configuration.getVersion()),
        () -> assertEquals("Test Description", configuration.getDescription()),
        () -> assertEquals("Test Contact Name", configuration.getContact()),
        () -> assertEquals("Test Contact Email", configuration.getContactEmail()),
        () -> assertEquals("Test License", configuration.getLicense()),
        () -> assertEquals("Test License URL", configuration.getLicenseUrl())
    );
  }

  @Test
  void testFromYamlOnlyTitle() {
    OpenApiConfiguration configuration = TestResources.readResourceYaml(
        OpenApiConfiguration.class,
        "fixtures/configuration/openapi/only-title.yaml");

    String expectedVersion = TestResources.readResourceFile("version.txt");

    assertAll("OpenAPI configuration is correct",
        () -> assertTrue(configuration.isEnabled()),
        () -> assertEquals("My New Title", configuration.getTitle()),
        () -> assertEquals(expectedVersion, configuration.getVersion()),
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
