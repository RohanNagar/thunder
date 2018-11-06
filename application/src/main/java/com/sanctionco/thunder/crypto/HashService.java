package com.sanctionco.thunder.crypto;

/**
 * Provides the base interface for the {@code HashService}. Provides methods to hash and to
 * verify existing hashes match.
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

  /**
   * Performs a hash of the plaintext.
   *
   * @param plaintext the text to hash
   * @return the computed hash
   */
  String hash(String plaintext);
}
