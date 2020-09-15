package com.sanctionco.thunder.crypto;

/**
 * Describes the supported password hashing algorithms available in Thunder.
 */
public enum HashAlgorithm {
  BCRYPT("bcrypt") {
    @Override
    public HashService newHashService(boolean serverSideHashEnabled,
                                      boolean allowCommonMistakes) {
      return new BCryptHashService(serverSideHashEnabled, allowCommonMistakes);
    }
  },
  SHA256("sha256") {
    @Override
    public HashService newHashService(boolean serverSideHashEnabled,
                                      boolean allowCommonMistakes) {
      return new Sha256HashService(serverSideHashEnabled, allowCommonMistakes);
    }
  },
  SIMPLE("simple") {
    @Override
    public HashService newHashService(boolean serverSideHashEnabled,
                                      boolean allowCommonMistakes) {
      return new SimpleHashService(serverSideHashEnabled, allowCommonMistakes);
    }
  };

  private final String text;

  HashAlgorithm(String text) {
    this.text = text;
  }

  /**
   * Provides a {@code HashAlgorithm} representation of the given text.
   *
   * @param text the text to parse
   * @return the {@code HashAlgorithm} representation or {@code null} if none match
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
   * Creates a new hash service that can be used to verify passwords.
   *
   * @param serverSideHashEnabled {@code true} if server side hashing should be
   *                              enabled; {@code false} otherwise
   * @param allowCommonMistakes {@code true} if the hash service should allow common password
   *                            mistakes when checking for a match; {@code false} otherwise
   * @return the new {@code HashService} object
   */
  public abstract HashService newHashService(boolean serverSideHashEnabled,
                                             boolean allowCommonMistakes);
}
