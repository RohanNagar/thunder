package com.sanctionco.thunder.crypto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;

/**
 * Provides optional configuration options for password hashing, including the hash algorithm.
 * See the {@code ThunderConfiguration} class for more details.
 */
public class PasswordHashConfiguration {

  /* Optional configuration options */

  @Valid
  @JsonProperty("algorithm")
  private final HashAlgorithm algorithm = HashAlgorithm.SIMPLE;

  public HashAlgorithm getAlgorithm() {
    return algorithm;
  }

  @Valid
  @JsonProperty("serverSideHash")
  private final Boolean serverSideHash = false;

  public Boolean serverSideHash() {
    return serverSideHash;
  }

  @Valid
  @JsonProperty("headerCheck")
  private final Boolean headerCheck = true;

  public Boolean isHeaderCheckEnabled() {
    return headerCheck;
  }
}
