package com.sanctionco.thunder.crypto;

/**
 * Describes the supported password hashing algorithms available in Thunder.
 */
public enum HashAlgorithm {
  BCRYPT("bcrypt") {
    public HashService newHashService() {
      return new BCryptHashService();
    }
  },
  MD5("md5") {
    public HashService newHashService() {
      return new MD5HashService();
    }
  },
  NONE("none") {
    public HashService newHashService() {
      return new SimpleHashService();
    }
  },
  SIMPLE("simple") {
    public HashService newHashService() {
      return new SimpleHashService();
    }
  };

  private final String text;

  HashAlgorithm(String text) {
    this.text = text;
  }

  /**
   * Provides a HashAlgorithm representation of a given string.
   *
   * @param text The string to parse into a HashAlgorithm.
   * @return The corresponding HashAlgorithm representation or {@code null} if none match.
   */
  public static HashAlgorithm fromString(String text) {
    for (HashAlgorithm type : HashAlgorithm.values()) {
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
   * @return The new HashService object.
   */
  public abstract HashService newHashService();
}
