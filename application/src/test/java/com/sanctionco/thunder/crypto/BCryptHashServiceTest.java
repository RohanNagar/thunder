package com.sanctionco.thunder.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BCryptHashServiceTest {
  private final HashService hashService = new BCryptHashService();

  @Test
  void testHashMatch() {
    String plaintext = "password";
    String hashed = "$2a$10$ARMWj7IH.TENN4iaH2W0Eu8loAX2iAa46GFMmMIuGhvYEGnWYL6Jy";

    assertTrue(hashService.isMatch(plaintext, hashed));
  }

  @Test
  void testHashMismatch() {
    String plaintext = "password";
    String hashed = "$2a$10$DeRCO2XcQ4IDZ19LikkaZOgl5eQWozWPJTCtSf1nJFjNEINR.ZOru";

    assertFalse(hashService.isMatch(plaintext, hashed));
  }

  @Test
  void testHashSame() {
    String plaintext = "password";
    String secondPlaintext = "password";

    String result = hashService.hash(plaintext);
    String secondResult = hashService.hash(secondPlaintext);

    assertTrue(hashService.isMatch(plaintext, result));
    assertTrue(hashService.isMatch(secondPlaintext, secondResult));

    assertTrue(hashService.isMatch(plaintext, secondResult));
    assertTrue(hashService.isMatch(secondPlaintext, result));
  }

  @Test
  void testHashDifferent() {
    String plaintext = "password";
    String secondPlaintext = "secondPassword";

    String result = hashService.hash(plaintext);
    String secondResult = hashService.hash(secondPlaintext);

    assertTrue(hashService.isMatch(plaintext, result));
    assertTrue(hashService.isMatch(secondPlaintext, secondResult));

    assertFalse(hashService.isMatch(plaintext, secondResult));
    assertFalse(hashService.isMatch(secondPlaintext, result));
  }
}
