package com.sanctionco.thunder.crypto;

import com.sanctionco.thunder.util.HashUtilities;

/**
 * Provides the MD5 implementation for the {@link HashService}. Provides methods to hash and to
 * verify existing hashes match.
 *
 * @see HashService
 */
public class MD5HashService extends HashService {

  MD5HashService(boolean serverSideHashEnabled, boolean allowCommonMistakes) {
    super(serverSideHashEnabled, allowCommonMistakes);
  }

  @Override
  boolean isMatchExact(String plaintext, String hashed) {
    String computedHash = HashUtilities.performHash("MD5", plaintext).toLowerCase();

    return computedHash.equalsIgnoreCase(hashed);
  }

  @Override
  public String hash(String plaintext) {
    if (serverSideHashEnabled()) {
      return HashUtilities.performHash("MD5", plaintext).toLowerCase();
    }

    return plaintext;
  }
}
