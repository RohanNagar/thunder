package com.sanctionco.thunder.crypto;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Provides the BCrypt implementation for the HashService. Provides a method that is used to verify
 * that hashed strings match.
 *
 * @see HashService
 */
public class BCryptHashService implements HashService {

  @Override
  public boolean isMatch(String plaintext, String hashed) {
    return BCrypt.checkpw(plaintext, hashed);
  }
}
