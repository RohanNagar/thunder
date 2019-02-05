package com.sanctionco.thunder.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utility methods related to hashing String values.
 *
 * @see com.sanctionco.thunder.crypto.HashService HashService
 */
public class HashUtilities {
  private static final Logger LOG = LoggerFactory.getLogger(HashUtilities.class);

  /**
   * Performs a hash of the given plaintext using the given hash algorithm.
   *
   * @param hashAlgorithm the hashing algorithm to use
   * @param plaintext the plaintext to hash
   * @return the hashed value of the plaintext
   */
  public static String performHash(String hashAlgorithm, String plaintext) {
    HexBinaryAdapter adapter = new HexBinaryAdapter();

    try {
      MessageDigest md = MessageDigest.getInstance(hashAlgorithm);

      byte[] digest = md.digest(plaintext.getBytes(StandardCharsets.UTF_8));
      return adapter.marshal(digest);
    } catch (NoSuchAlgorithmException e) {
      LOG.error("Attempted to hash with algorithm {}, which is not supported by the Java library. "
          + "Requests are going to fail while this continues.", hashAlgorithm, e);

      throw new RuntimeException(e);
    }
  }
}
