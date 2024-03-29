package com.sanctionco.thunder.crypto;

import com.sanctionco.thunder.util.HashUtilities;

/**
 * Provides the MD5 implementation for the {@link HashService}. Provides methods to hash and to
 * verify existing hashes match.
 *
 * @see HashService
 */
public class Sha256HashService extends HashService {
  private static final int SALT_LENGTH = 16;

  Sha256HashService(boolean serverSideHashEnabled, boolean allowCommonMistakes) {
    super(serverSideHashEnabled, allowCommonMistakes);
  }

  @Override
  boolean isMatchExact(String plaintext, String hashed) {
    var salt = hashed.substring(0, SALT_LENGTH);
    var pureHashed = hashed.substring(SALT_LENGTH);

    String computedHash = HashUtilities.performHash("SHA-256", salt + plaintext).toLowerCase();

    return computedHash.equalsIgnoreCase(pureHashed);
  }

  @Override
  public String hash(String plaintext) {
    if (!serverSideHashEnabled()) {
      return plaintext;
    }

    var salt = generateSalt(SALT_LENGTH);
    var hashed = HashUtilities.performHash("SHA-256", salt + plaintext).toLowerCase();

    return salt + hashed;
  }
}
