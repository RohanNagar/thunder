package com.sanctionco.thunder.crypto;

import org.junit.jupiter.api.Test;

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
}
