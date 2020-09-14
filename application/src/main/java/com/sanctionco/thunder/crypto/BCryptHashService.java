package com.sanctionco.thunder.crypto;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Provides the BCrypt implementation for the {@link HashService}. Provides methods to hash and to
 * verify existing hashes match.
 *
 * @see HashService
 */
public class BCryptHashService extends HashService {

  BCryptHashService(boolean serverSideHashEnabled, boolean allowCommonMistakes) {
    super(serverSideHashEnabled, allowCommonMistakes);
  }

  @Override
  boolean isMatchExact(String plaintext, String hashed) {
    return BCrypt.verifyer().verify(plaintext.getBytes(), hashed.getBytes()).verified;
  }

  @Override
  public String hash(String plaintext) {
    if (serverSideHashEnabled()) {
      return BCrypt.withDefaults().hashToString(10, plaintext.toCharArray());
    }

    return plaintext;
  }
}
