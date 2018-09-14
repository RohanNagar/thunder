package com.sanctionco.thunder.crypto;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides hashing and verifying methods implemented with BCrypt.
 */
public class BCryptHashService implements HashService {
  private static final Logger LOG = LoggerFactory.getLogger(BCryptHashService.class);

  /**
   * Determines if the given string matches the given hashed string.
   *
   * @param plaintext The string to check for a match.
   * @param hashed The hashed string to check against.
   * @return True if they match, false otherwise
   */
  @Override
  public boolean isMatch(String plaintext, String hashed) {
    return BCrypt.checkpw(plaintext, hashed);
  }
}
