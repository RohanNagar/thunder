package com.sanctionco.thunder.crypto;

import com.sanctionco.thunder.util.HashUtilities;

/**
 * Provides the MD5 implementation for the {@link HashService}. Provides methods to hash and to
 * verify existing hashes match.
 *
 * @see HashService
 */
public class MD5HashService implements HashService {

  @Override
  public boolean isMatch(String plaintext, String hashed) {
    String computedHash = HashUtilities.performHash("MD5", plaintext);

    return computedHash.equals(hashed);
  }

  @Override
  public String hash(String plaintext) {
    return HashUtilities.performHash("MD5", plaintext);
  }
}
