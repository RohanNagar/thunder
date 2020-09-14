package com.sanctionco.thunder.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class MD5HashServiceTest {
  private final HashService hashService = new MD5HashService(true, false);

  @Test
  void testHashMatch() {
    String plaintext = "password";
    String hashed = "saltysaltysalt22bc9dd5d51b5fc09d9e981cf783603fbe";
    String uppercase = "saltysaltysalt22" + "bc9dd5d51b5fc09d9e981cf783603fbe".toUpperCase();

    assertTrue(hashService.isMatch(plaintext, hashed));
    assertTrue(hashService.isMatch(plaintext, uppercase));
  }

  @Test
  void testHashMismatch() {
    String plaintext = "password";
    String hashed = "saltysaltysalt225e9d11a14ad1c8dd77e98ef9b53fd1ba";
    String uppercase = "saltysaltysalt22" + "5e9d11a14ad1c8dd77e98ef9b53fd1ba".toUpperCase();

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
  void testHashSaltLengthIncorrect() {
    HashService md5HashService = new MD5HashService(true, false);
    HashService hashService = spy(md5HashService);

    when(hashService.generateSalt(anyInt())).thenReturn("toolongofasaltgenerated");

    String plaintext = "password";

    var result = hashService.hash(plaintext);

    assertTrue(result.startsWith("toolongofasaltge"));
    assertFalse(result.startsWith("toolongofasaltgenerated"));
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
