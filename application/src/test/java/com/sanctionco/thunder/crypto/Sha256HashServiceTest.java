package com.sanctionco.thunder.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Sha256HashServiceTest {
  private final HashService hashService = new Sha256HashService(true, false);

  @Test
  void testHashMatch() {
    String plaintext = "password";
    String hashed = "saltysaltysalt22"
        + "6cb4d24e21a9955515d52d6dc86449202f55f5b1463a800d2803cdda90298530";
    String uppercase = "saltysaltysalt22"
        + "6cb4d24e21a9955515d52d6dc86449202f55f5b1463a800d2803cdda90298530".toUpperCase();

    assertTrue(hashService.isMatch(plaintext, hashed));
    assertTrue(hashService.isMatch(plaintext, uppercase));
  }

  @Test
  void testHashMismatch() {
    String plaintext = "password";
    String hashed = "saltysaltysalt22"
        + "5f06eb8e60c449cba9e348a1e52f0a00f77bc48af2d3911363d8f66bda4d29dc";
    String uppercase = "saltysaltysalt22"
        + "5f06eb8e60c449cba9e348a1e52f0a00f77bc48af2d3911363d8f66bda4d29dc".toUpperCase();

    assertFalse(hashService.isMatch(plaintext, hashed));
    assertFalse(hashService.isMatch(plaintext, uppercase));
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
    HashService hashService = new Sha256HashService(true, true);

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
  void testHashWithMistakesEnabledIncorrect() {
    HashService hashService = new Sha256HashService(true, true);

    String plaintext = "Password";
    String hashed = hashService.hash(plaintext);

    assertTrue(hashService.isMatch(plaintext, hashed));

    String incorrectCapsLock = "PASSWORD";
    assertFalse(hashService.isMatch(incorrectCapsLock, hashed));

    String incorrectExtraFirstCharacter = "*2Password";
    assertFalse(hashService.isMatch(incorrectExtraFirstCharacter, hashed));

    String incorrectExtraLastCharacter = "Password-x";
    assertFalse(hashService.isMatch(incorrectExtraLastCharacter, hashed));

    String incorrectFirstCharacterCaseSwap = "passworD";
    assertFalse(hashService.isMatch(incorrectFirstCharacterCaseSwap, hashed));
  }

  @Test
  void testServerSideHashDisabled() {
    HashService hashService = new Sha256HashService(false, false);

    String plaintext = "password";
    String result = hashService.hash(plaintext);

    // No hashing should be performed
    assertEquals(plaintext, result);
  }
}
