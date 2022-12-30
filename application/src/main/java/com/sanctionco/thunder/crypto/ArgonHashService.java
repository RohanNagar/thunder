package com.sanctionco.thunder.crypto;

import com.password4j.Argon2Function;
import com.password4j.Password;

/**
 * Provides the BCrypt implementation for the {@link HashService}. Provides methods to hash and to
 * verify existing hashes match.
 *
 * @see HashService
 */
public class ArgonHashService extends HashService {

  ArgonHashService(boolean serverSideHashEnabled, boolean allowCommonMistakes) {
    super(serverSideHashEnabled, allowCommonMistakes);
  }

  @Override
  boolean isMatchExact(String plaintext, String hashed) {
    return Password.check(plaintext, hashed).with(Argon2Function.getInstanceFromHash(hashed));
  }

  @Override
  public String hash(String plaintext) {
    if (serverSideHashEnabled()) {
      return Password.hash(plaintext).withArgon2().getResult();
    }

    return plaintext;
  }
}
