package com.sanctionco.thunder.crypto;

import com.sanctionco.thunder.util.HashUtilities;

/**
 * Provides the MD5 implementation for the {@link HashService}. Provides a method that is used to
 * verify that hashed strings match.
 *
 * @see HashService
 */
public class MD5HashService implements HashService {

  @Override
  public boolean isMatch(String plaintext, String hashed) {
    String computedHash = HashUtilities.performHash("MD5", plaintext);

    return computedHash.equals(hashed);
  }
}
