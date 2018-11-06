package com.sanctionco.thunder.crypto;

/**
 * Provides the simple implementation for the {@link HashService}.  Provides methods to hash and to
 * verify existing hashes match. This implementation can be used to compare without actually
 * performing a hash.
 *
 * @see HashService
 */
public class SimpleHashService implements HashService {

  @Override
  public boolean isMatch(String plaintext, String hashed) {
    return plaintext.equals(hashed);
  }

  @Override
  public String hash(String plaintext) {
    return plaintext;
  }
}
