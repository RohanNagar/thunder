package com.sanctionco.thunder.crypto;

import com.sanctionco.thunder.crypto.password.BCryptPasswordVerifier;
import com.sanctionco.thunder.crypto.password.PasswordVerifier;
import com.sanctionco.thunder.crypto.password.SimplePasswordVerifier;

/**
 * Describes the supported password hashing algorithms available in Thunder.
 */
public enum HashingAlgorithm {
  SIMPLE("simple") {
    public PasswordVerifier newPasswordVerifier() {
      return new SimplePasswordVerifier();
    }
  },
  BCRYPT("bcrypt") {
    public PasswordVerifier newPasswordVerifier() {
      return new BCryptPasswordVerifier();
    }
  };

  private final String text;

  HashingAlgorithm(String text) {
    this.text = text;
  }

  /**
   * Provides a HashingAlgorithm representation of a given string.
   *
   * @param text The string to parse into a HashingAlgorithm.
   * @return The corresponding HashingAlgorithm representation or {@code null} if none match.
   */
  public static HashingAlgorithm fromString(String text) {
    for (HashingAlgorithm type : HashingAlgorithm.values()) {
      if (type.text.equalsIgnoreCase(text)) {
        return type;
      }
    }

    return null;
  }

  @Override
  public String toString() {
    return this.text;
  }

  /**
   * Creates a new password verifier that should be used to verify passwords with
   * the hashing algorithm type.
   *
   * @return The new PasswordVerifier object.
   */
  public abstract PasswordVerifier newPasswordVerifier();
}
