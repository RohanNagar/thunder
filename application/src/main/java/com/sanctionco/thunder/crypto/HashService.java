package com.sanctionco.thunder.crypto;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

/**
 * Provides the base interface for the {@code HashService}. Provides methods to hash and to
 * verify existing hashes match.
 */
public abstract class HashService {
  private static final Random RANDOM = new SecureRandom();

  private final boolean serverSideHashEnabled;
  private final boolean allowCommonMistakes;

  public HashService(boolean serverSideHashEnabled,
                     boolean allowCommonMistakes) {
    this.serverSideHashEnabled = serverSideHashEnabled;
    this.allowCommonMistakes = allowCommonMistakes;
  }

  /**
   * Determines if the plaintext matches the given hashed string. If allowCommonMistakes is true,
   * will also check if the plaintext matches after modifying using common password mistakes:
   *
   * <p>1. Mistakenly using caps lock
   * 2. Inserting a random character before or after
   * 3. Capitalizing (or not) the first character
   *
   * @param plaintext the plaintext string
   * @param hashed the hashed string to check against
   * @return {@code true} if the plaintext is a match; {@code false} otherwise
   */
  public boolean isMatch(String plaintext, String hashed) {
    if (!allowCommonMistakes) {
      return isMatchExact(plaintext, hashed);
    }

    var capsLock = StringUtils.swapCase(plaintext);
    var extraCharacterInFront = plaintext.substring(1);
    var extraCharacterInBack = plaintext.substring(0, plaintext.length() - 1);
    var firstCharCase = StringUtils.swapCase(plaintext.substring(0, 1)) + plaintext.substring(1);

    return isMatchExact(plaintext, hashed)
        || isMatchExact(capsLock, hashed)
        || isMatchExact(extraCharacterInFront, hashed)
        || isMatchExact(extraCharacterInBack, hashed)
        || isMatchExact(firstCharCase, hashed);
  }

  /**
   * Determines if the exact plaintext matches the given hashed string.
   *
   * @param plaintext the plaintext string
   * @param hashed the hashed string to check against
   * @return {@code true} if the plaintext is a match; {@code false} otherwise
   */
  abstract boolean isMatchExact(String plaintext, String hashed);

  /**
   * Performs a hash of the plaintext if server side hashing is enabled. If server side
   * hashing is disabled, the plaintext will be returned without modification.
   *
   * @param plaintext the text to hash
   * @return the computed hash or the original plaintext if server side hashing is disabled
   */
  public abstract String hash(String plaintext);

  /**
   * Determines if server side password hashing is currently enabled.
   *
   * @return {@code true} if server side hashing is enabled; {@code false} otherwise
   */
  boolean serverSideHashEnabled() {
    return serverSideHashEnabled;
  }

  /**
   * Generates a new salt.
   *
   * @param length the number of characters that the salt should contain
   * @return the generated salt
   */
  String generateSalt(int length) {
    var salt = new byte[length];

    RANDOM.nextBytes(salt);

    return Base64.getEncoder().encodeToString(salt);
  }
}
