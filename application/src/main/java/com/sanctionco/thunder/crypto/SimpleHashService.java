package com.sanctionco.thunder.crypto;

/**
 * Provides an implementation of HashService that does not actually do
 * hashing, in the case a user does not want to use hashing.
 */
public class SimpleHashService implements HashService {

  /**
   * Determines if the given string matches the given hashed string.
   *
   * @param plaintext The string to check for a match.
   * @param hashed The hashed string to check against.
   * @return True if they match, false otherwise
   */
  @Override
  public boolean isMatch(String plaintext, String hashed) {
    return plaintext.equals(hashed);
  }
}
