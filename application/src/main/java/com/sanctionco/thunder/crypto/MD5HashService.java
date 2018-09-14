package com.sanctionco.thunder.crypto;

/**
 * Provides hashing and verifying methods implemented with MD5.
 */
public class MD5HashService implements HashService {

  /**
   * Determines if the given string matches the given hashed string.
   *
   * @param plaintext The string to check for a match.
   * @param hashed The hashed string to check against.
   * @return True if they match, false otherwise
   */
  @Override
  public boolean isMatch(String plaintext, String hashed) {
    // TODO
    return plaintext.equals(hashed);
  }
}
