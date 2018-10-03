package com.sanctionco.thunder.crypto;

/**
 * Provides the base interface for the HashService. Provides a method that is used to verify
 * that hashed strings match.
 */
public interface HashService {

  /**
   * Determines if the plaintext matches the given hashed string.
   *
   * @param plaintext the plaintext string
   * @param hashed the hashed string to check against
   * @return {@code true} if the plaintext is a match; {@code false} otherwise
   */
  boolean isMatch(String plaintext, String hashed);
}
