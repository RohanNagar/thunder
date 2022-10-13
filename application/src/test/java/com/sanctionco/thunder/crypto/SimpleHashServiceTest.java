package com.sanctionco.thunder.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleHashServiceTest {
  private final HashService hashService = new SimpleHashService(true, false);

  @Test
  void testHashMatch() {
    String plaintext = "5f4dcc3b5aa765d61d8327deb882cf99";
    String hashed = "5f4dcc3b5aa765d61d8327deb882cf99";

    assertTrue(hashService.isMatch(plaintext, hashed));
  }

  @Test
  void testHashMismatch() {
    String plaintext = "5f4dcc3b5aa765d61d8327deb882cf99";
    String hashed = "5e9d11a14ad1c8dd77e98ef9b53fd1ba";

    assertFalse(hashService.isMatch(plaintext, hashed));
  }
}
