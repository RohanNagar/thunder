package com.sanctionco.thunder.crypto;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Provides the BCrypt implementation for the {@link HashService}. Provides methods to hash and to
 * verify existing hashes match.
 *
 * @see HashService
 */
public class BCryptHashService extends HashService {

  BCryptHashService(boolean serverSideHashEnabled) {
    super(serverSideHashEnabled);
  }

  @Override
  public boolean isMatch(String plaintext, String hashed) {
    return BCrypt.checkpw(plaintext, hashed);
  }

  @Override
  public String hash(String plaintext) {
    if (serverSideHashEnabled()) {
      return BCrypt.hashpw(plaintext, BCrypt.gensalt());
    }

    return plaintext;
  }
}
