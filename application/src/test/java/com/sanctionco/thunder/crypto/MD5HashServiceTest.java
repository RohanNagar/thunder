package com.sanctionco.thunder.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MD5HashServiceTest {
  private final HashService hashService = new MD5HashService(false);

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
  }

  @Test
  void testHashDifferent() {
    String plaintext = "password";
    String secondPlaintext = "secondPassword";

    String result = hashService.hash(plaintext);
    String secondResult = hashService.hash(secondPlaintext);

    assertNotEquals(result, secondResult);
  }

  @Test
  void testServerSideHashDisabled() {
    HashService hashService = new MD5HashService(false);

    String plaintext = "password";
    String result = hashService.hash(plaintext);

    assertEquals(plaintext, result);
  }
}
