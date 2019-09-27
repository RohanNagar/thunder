package com.sanctionco.thunder.openapi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SwaggerResourceTest {
  private final SwaggerResource resource = new SwaggerResource();

  @Test
  void testGetView() {
    SwaggerView response = resource.get();

    assertEquals("Thunder Swagger UI", response.getTitle());
    assertEquals("/swagger-static", response.getSwaggerAssetsPath());
  }
}
