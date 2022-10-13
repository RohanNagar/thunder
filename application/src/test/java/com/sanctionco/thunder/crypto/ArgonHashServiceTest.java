package com.sanctionco.thunder.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArgonHashServiceTest {
  private final HashService hashService = new ArgonHashService(true, false);

  @Test
  void testHashMatch() {
    String plaintext = "password";
    String hashed = "$argon2id$v=19$m=15,t=2,p=1$and1aHgwcThpM2EwMDAwMA$+GgRQ1NSPghlIAUWlO1mVTktS"
        + "QVSj35tUNvLiVfWiB0";

    assertTrue(hashService.isMatch(plaintext, hashed));
  }

  @Test
  void testHashMismatch() {
    String plaintext = "password";
    String hashed = "$argon2id$v=19$m=15,t=2,p=1$MGlnaWcwZWsyZDFoMDAwMA$uv36sfuipb68EEn+an4iPf0B1"
        + "TNI6qw0YeW9PXKbPsM";

    assertFalse(hashService.isMatch(plaintext, hashed));
  }
}
