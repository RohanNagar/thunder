package com.sanctionco.thunder.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides hashing and verifying methods implemented with MD5.
 */
public class MD5HashService implements HashService {
  private static final Logger LOG = LoggerFactory.getLogger(MD5HashService.class);

  /**
   * Determines if the given string matches the given hashed string.
   *
   * @param plaintext The string to check for a match.
   * @param hashed The hashed string to check against.
   * @return True if they match, false otherwise
   */
  @Override
  public boolean isMatch(String plaintext, String hashed) {
    String computedHash;

    try {
      MessageDigest md = MessageDigest.getInstance("MD5");

      byte[] digest = md.digest(plaintext.getBytes(StandardCharsets.UTF_8));
      computedHash = Hex.encodeHexString(digest);
    } catch (NoSuchAlgorithmException e) {
      LOG.error("An expected error occurred while computing an MD5 hash. "
          + "Requests are going to fail while this continues.", e);
      return false;
    }
    
    return computedHash.equals(hashed);
  }
}
