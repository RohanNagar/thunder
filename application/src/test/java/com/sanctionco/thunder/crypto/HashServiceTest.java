package com.sanctionco.thunder.crypto;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HashServiceTest {

  @ParameterizedTest
  @EnumSource(HashAlgorithm.class)
  void testHashSame(HashAlgorithm algorithm) {
    var hashService = algorithm.newHashService(true, false);

    String plaintext = "password";
    String secondPlaintext = "password";

    String result = hashService.hash(plaintext);
    String secondResult = hashService.hash(secondPlaintext);

    assertTrue(hashService.isMatch(plaintext, result));
    assertTrue(hashService.isMatch(secondPlaintext, secondResult));

    assertTrue(hashService.isMatch(plaintext, secondResult));
    assertTrue(hashService.isMatch(secondPlaintext, result));
  }

  @ParameterizedTest
  @EnumSource(HashAlgorithm.class)
  void testHashDifferent(HashAlgorithm algorithm) {
    var hashService = algorithm.newHashService(true, false);

    String plaintext = "password";
    String secondPlaintext = "secondPassword";

    String result = hashService.hash(plaintext);
    String secondResult = hashService.hash(secondPlaintext);

    assertTrue(hashService.isMatch(plaintext, result));
    assertTrue(hashService.isMatch(secondPlaintext, secondResult));

    assertFalse(hashService.isMatch(plaintext, secondResult));
    assertFalse(hashService.isMatch(secondPlaintext, result));
  }

  @ParameterizedTest
  @EnumSource(HashAlgorithm.class)
  void testHashWithMistakesDisabled(HashAlgorithm algorithm) {
    var hashService = algorithm.newHashService(true, false);

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

  @ParameterizedTest
  @EnumSource(HashAlgorithm.class)
  void testHashWithMistakesEnabled(HashAlgorithm algorithm) {
    HashService hashService = algorithm.newHashService(true, true);

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

  @ParameterizedTest
  @EnumSource(HashAlgorithm.class)
  void testHashWithMistakesEnabledIncorrect(HashAlgorithm algorithm) {
    HashService hashService = algorithm.newHashService(true, true);

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

  @ParameterizedTest
  @EnumSource(HashAlgorithm.class)
  void testServerSideHashDisabled(HashAlgorithm algorithm) {
    HashService hashService = algorithm.newHashService(false, false);

    String plaintext = "password";
    String result = hashService.hash(plaintext);

    // No hashing should be performed
    assertEquals(plaintext, result);
  }
}
