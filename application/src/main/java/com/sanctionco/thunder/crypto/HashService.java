package com.sanctionco.thunder.crypto;

/**
 * Provides the base interface for the {@code HashService}. Provides methods to hash and to
 * verify existing hashes match.
 */
public abstract class HashService {
  private final boolean serverSideHashEnabled;

  public HashService(boolean serverSideHashEnabled) {
    this.serverSideHashEnabled = serverSideHashEnabled;
  }

  /**
   * Determines if the plaintext matches the given hashed string.
   *
   * @param plaintext the plaintext string
   * @param hashed the hashed string to check against
   * @return {@code true} if the plaintext is a match; {@code false} otherwise
   */
  public abstract boolean isMatch(String plaintext, String hashed);

  /**
   * Performs a hash of the plaintext if server side hashing is enabled. If server side
   * hashing is disabled, the plaintext will be returned without modification.
   *
   * @param plaintext the text to hash
   * @return the computed hash or the original plaintext if server side hashing is disabled
   */
  public abstract String hash(String plaintext);

  /**
   * Determines if server side password hashing is currently enabled.
   *
   * @return {@code true} if server side hashing is enabled; {@code false} otherwise
   */
  boolean serverSideHashEnabled() {
    return serverSideHashEnabled;
  }
}
