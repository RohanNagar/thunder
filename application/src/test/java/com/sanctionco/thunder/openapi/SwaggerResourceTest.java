package com.sanctionco.thunder.openapi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SwaggerResourceTest {

  @Test
  void testGetView() {
    SwaggerView response = new SwaggerResource().get();

    assertAll(
        () -> assertEquals("Thunder Swagger UI", response.getTitle()),
        () -> assertEquals("/swagger-static", response.getSwaggerAssetsPath()),
        () -> assertEquals("", response.getContextPath()),
        () -> assertNull(response.getValidatorUrl())
    );
  }
}
