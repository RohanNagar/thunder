package com.sanctionco.thunder.crypto;

import com.password4j.Password;

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
    return Password.check(plaintext, hashed).withBcrypt();
  }

  @Override
  public String hash(String plaintext) {
    if (serverSideHashEnabled()) {
      return Password.hash(plaintext).withBcrypt().getResult();
    }

    return plaintext;
  }
}
