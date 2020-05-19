package com.sanctionco.thunder.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BCryptHashServiceTest {
  private final HashService hashService = new BCryptHashService(true, false);

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

  @Test
  void testHashWithMistakesDisabled() {
    String plaintext = "Password";
    String hashed = hashService.hash(plaintext);

    assertTrue(hashService.isMatch(plaintext, hashed));

    String capsLock = "pASSWORD";
    assertFalse(hashService.isMatch(capsLock, hashed));

    String extraFirstCharacter = "*Password";
    assertFalse(hashService.isMatch(extraFirstCharacter, hashed));

    String extraLastCharacter = "Password-";
    assertFalse(hashService.isMatch(extraLastCharacter, hashed));

    String firstCharacterCaseSwap = "password";
    assertFalse(hashService.isMatch(firstCharacterCaseSwap, hashed));
  }

  @Test
  void testHashWithMistakesEnabled() {
    HashService hashService = new BCryptHashService(true, true);

    String plaintext = "Password";
    String hashed = hashService.hash(plaintext);

    assertTrue(hashService.isMatch(plaintext, hashed));

    String capsLock = "pASSWORD";
    assertTrue(hashService.isMatch(capsLock, hashed));

    String extraFirstCharacter = "*Password";
    assertTrue(hashService.isMatch(extraFirstCharacter, hashed));

    String extraLastCharacter = "Password-";
    assertTrue(hashService.isMatch(extraLastCharacter, hashed));

    String firstCharacterCaseSwap = "password";
    assertTrue(hashService.isMatch(firstCharacterCaseSwap, hashed));
  }

  @Test
  void testServerSideHashDisabled() {
    HashService hashService = new BCryptHashService(false, false);

    String plaintext = "password";
    String result = hashService.hash(plaintext);

    assertEquals(plaintext, result);
  }
}
