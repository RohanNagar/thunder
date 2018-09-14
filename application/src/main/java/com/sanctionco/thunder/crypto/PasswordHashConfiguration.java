package com.sanctionco.thunder.crypto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;

/**
 * Provides optional customization options for password hashing.
 */
public class PasswordHashConfiguration {

  /* Optional configuration options */

  @Valid
  @JsonProperty("algorithm")
  private final HashAlgorithm algorithm = HashAlgorithm.SIMPLE;

  public HashAlgorithm getAlgorithm() {
    return algorithm;
  }
}
