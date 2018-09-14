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
            HashAlgorithm.BCRYPT.newHashService().getClass()));
  }

  @Test
  void testMd5ResponseTypeMapping() {
    assertAll("Assert equal MD5 enum and string value",
        () -> assertEquals(HashAlgorithm.MD5, HashAlgorithm.fromString("md5")),
        () -> assertEquals("md5", HashAlgorithm.MD5.toString()),
        () -> assertEquals(MD5HashService.class,
            HashAlgorithm.MD5.newHashService().getClass()));
  }

  @Test
  void testSimpleResponseTypeMapping() {
    assertAll("Assert equal SIMPLE enum and string value",
        () -> assertEquals(HashAlgorithm.SIMPLE, HashAlgorithm.fromString("simple")),
        () -> assertEquals("simple", HashAlgorithm.SIMPLE.toString()),
        () -> assertEquals(SimpleHashService.class,
            HashAlgorithm.SIMPLE.newHashService().getClass()));
  }

  @Test
  void testNoneResponseTypeMapping() {
    assertAll("Assert equal NONE enum and string value",
        () -> assertEquals(HashAlgorithm.NONE, HashAlgorithm.fromString("none")),
        () -> assertEquals("none", HashAlgorithm.NONE.toString()),
        () -> assertEquals(SimpleHashService.class,
            HashAlgorithm.NONE.newHashService().getClass()));
  }

  @Test
  void testNullResponseTypeFromString() {
    assertNull(HashAlgorithm.fromString("unknown"));
  }
}
