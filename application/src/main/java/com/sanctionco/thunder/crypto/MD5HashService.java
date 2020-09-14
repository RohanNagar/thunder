package com.sanctionco.thunder.crypto;

import com.sanctionco.thunder.util.HashUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the MD5 implementation for the {@link HashService}. Provides methods to hash and to
 * verify existing hashes match.
 *
 * @see HashService
 */
public class MD5HashService extends HashService {
  private static final Logger LOG = LoggerFactory.getLogger(MD5HashService.class);
  private static final int EXPECTED_SALT_LENGTH = 16;

  MD5HashService(boolean serverSideHashEnabled, boolean allowCommonMistakes) {
    super(serverSideHashEnabled, allowCommonMistakes);
  }

  @Override
  boolean isMatchExact(String plaintext, String hashed) {
    var salt = hashed.substring(0, EXPECTED_SALT_LENGTH);
    var pureHashed = hashed.substring(EXPECTED_SALT_LENGTH);

    String computedHash = HashUtilities.performHash("MD5", salt + plaintext).toLowerCase();

    return computedHash.equalsIgnoreCase(pureHashed);
  }

  @Override
  public String hash(String plaintext) {
    if (!serverSideHashEnabled()) {
      return plaintext;
    }

    var salt = generateSalt(EXPECTED_SALT_LENGTH);

    if (salt.length() != EXPECTED_SALT_LENGTH) {
      LOG.error("Unexpected salt length {} for salt {} when performing MD5 hash! "
              + " Shortening salt to length {} to ensure future verification works.",
          salt.length(), salt, EXPECTED_SALT_LENGTH);

      salt = salt.substring(0, EXPECTED_SALT_LENGTH);
    }

    var hashed = HashUtilities.performHash("MD5", salt + plaintext).toLowerCase();

    return salt + hashed;
  }
}
