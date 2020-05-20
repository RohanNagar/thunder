package com.sanctionco.thunder.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MD5HashServiceTest {
  private final HashService hashService = new MD5HashService(true, false);

  @Test
  void testHashMatch() {
    String plaintext = "password";
    String hashed = "5f4dcc3b5aa765d61d8327deb882cf99";

    assertTrue(hashService.isMatch(plaintext, hashed));
    assertTrue(hashService.isMatch(plaintext, hashed.toUpperCase()));
  }

  @Test
  void testHashMismatch() {
    String plaintext = "password";
    String hashed = "5e9d11a14ad1c8dd77e98ef9b53fd1ba";

    assertFalse(hashService.isMatch(plaintext, hashed));
    assertFalse(hashService.isMatch(plaintext, hashed.toUpperCase()));
  }

  @Test
  void testHashSame() {
    String plaintext = "password";
    String secondPlaintext = "password";

    String result = hashService.hash(plaintext);
    String secondResult = hashService.hash(secondPlaintext);

    assertEquals(result, secondResult);
    assertNotEquals(plaintext, result);
    assertNotEquals(secondPlaintext, secondResult);
  }

  @Test
  void testHashDifferent() {
    String plaintext = "password";
    String secondPlaintext = "secondPassword";

    String result = hashService.hash(plaintext);
    String secondResult = hashService.hash(secondPlaintext);

    assertNotEquals(result, secondResult);
    assertNotEquals(plaintext, result);
    assertNotEquals(secondPlaintext, secondResult);
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
    HashService hashService = new MD5HashService(true, true);

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
    HashService hashService = new MD5HashService(true, true);

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
    HashService hashService = new MD5HashService(false, false);

    String plaintext = "password";
    String result = hashService.hash(plaintext);

    // No hashing should be performed
    assertEquals(plaintext, result);
  }
}
