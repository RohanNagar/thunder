package com.sanctionco.thunder.openapi;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;

/**
 * Provides optional configuration options for generating OpenAPI documentation,
 * including enabling/disabling OpenAPI generation. See the {@code ThunderConfiguration}
 * class for more details.
 */
public class OpenApiConfiguration {

  /* Optional configuration options */

  @Valid
  @JsonProperty("enabled")
  private final Boolean enabled = true;

  public Boolean isEnabled() {
    return enabled;
  }
}
