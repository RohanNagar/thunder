package com.sanctionco.thunder.crypto;

/**
 * Defines an interface that provides hashing and verifying methods to ensure that
 * hashed strings match.
 */
public interface HashService {

  /**
   * Determines if the given string matches the given hashed string.
   *
   * @param plaintext The string to check for a match.
   * @param hashed The hashed string to check against.
   * @return True if they match, false otherwise
   */
  boolean isMatch(String plaintext, String hashed);
}
