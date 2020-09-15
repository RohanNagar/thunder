package com.sanctionco.thunder.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HashAlgorithmTest {

  @Test
  void testBcryptResponseTypeMapping() {
    assertAll("Assert equal BCRYPT enum and string value",
        () -> assertEquals(HashAlgorithm.BCRYPT, HashAlgorithm.fromString("bcrypt")),
        () -> assertEquals("bcrypt", HashAlgorithm.BCRYPT.toString()),
        () -> assertEquals(BCryptHashService.class,
            HashAlgorithm.BCRYPT.newHashService(false, false).getClass()));
  }

  @Test
  void testMd5ResponseTypeMapping() {
    assertAll("Assert equal SHA256 enum and string value",
        () -> assertEquals(HashAlgorithm.SHA256, HashAlgorithm.fromString("sha256")),
        () -> assertEquals("sha256", HashAlgorithm.SHA256.toString()),
        () -> assertEquals(Sha256HashService.class,
            HashAlgorithm.SHA256.newHashService(false, false).getClass()));
  }

  @Test
  void testSimpleResponseTypeMapping() {
    assertAll("Assert equal SIMPLE enum and string value",
        () -> assertEquals(HashAlgorithm.SIMPLE, HashAlgorithm.fromString("simple")),
        () -> assertEquals("simple", HashAlgorithm.SIMPLE.toString()),
        () -> assertEquals(SimpleHashService.class,
            HashAlgorithm.SIMPLE.newHashService(false, false).getClass()));
  }

  @Test
  void testNullResponseTypeFromString() {
    assertNull(HashAlgorithm.fromString("unknown"));
  }
}
