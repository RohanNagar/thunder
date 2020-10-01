package com.sanctionco.thunder.crypto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;

/**
 * Provides optional configuration options for password hashing, including the hash algorithm.
 * See the {@code ThunderConfiguration} class for more details.
 */
public class PasswordHashConfiguration {
  private static final HashAlgorithm DEFAULT_ALGORITHM = HashAlgorithm.SIMPLE;
  private static final boolean DEFAULT_SERVER_SIDE_HASH = false;
  private static final boolean DEFAULT_HEADER_CHECK = true;
  private static final boolean DEFAULT_ALLOW_COMMON_MISTAKES = false;

  /**
   * Constructs a new instance of {@code PasswordHashConfiguration} with default values.
   */
  public PasswordHashConfiguration() {
    this.algorithm = DEFAULT_ALGORITHM;
    this.serverSideHash = DEFAULT_SERVER_SIDE_HASH;
    this.headerCheck = DEFAULT_HEADER_CHECK;
    this.allowCommonMistakes = DEFAULT_ALLOW_COMMON_MISTAKES;
  }

  @Valid @JsonProperty("algorithm")
  private final HashAlgorithm algorithm;

  public HashAlgorithm getAlgorithm() {
    return algorithm;
  }

  @Valid @JsonProperty("serverSideHash")
  private final Boolean serverSideHash;

  public Boolean serverSideHash() {
    return serverSideHash;
  }

  @Valid @JsonProperty("headerCheck")
  private final Boolean headerCheck;

  public Boolean isHeaderCheckEnabled() {
    return headerCheck;
  }

  @Valid @JsonProperty("allowCommonMistakes")
  private final Boolean allowCommonMistakes;

  public Boolean allowCommonMistakes() {
    return allowCommonMistakes;
  }
}
