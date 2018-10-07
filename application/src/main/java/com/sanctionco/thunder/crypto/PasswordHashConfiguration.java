package com.sanctionco.thunder.crypto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;

/**
 * Provides optional configuration options for password hashing, including the hash algorithm.
 *
 * @see com.sanctionco.thunder.ThunderConfiguration ThunderConfiguration
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
