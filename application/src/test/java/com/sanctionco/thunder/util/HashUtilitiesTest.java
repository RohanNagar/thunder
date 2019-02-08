package com.sanctionco.thunder.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HashUtilitiesTest {

  @Test
  void testConstructInstance() {
    new HashUtilities();
  }

  @Test
  void testMd5Hash() {
    String plaintext = "password";
    String hashed = "5f4dcc3b5aa765d61d8327deb882cf99";

    String computed = HashUtilities.performHash("MD5", plaintext);

    assertTrue(hashed.equalsIgnoreCase(computed));
  }

  @Test
  void testMd5HashMismatch() {
    String plaintext = "password";
    String hashed = "5e9d11a14ad1c8dd77e98ef9b53fd1ba";

    String computed = HashUtilities.performHash("MD5", plaintext);

    assertFalse(hashed.equalsIgnoreCase(computed));
  }

  @Test
  void testUnsupportedAlgorithmThrows() {
    Exception e = assertThrows(RuntimeException.class,
        () -> HashUtilities.performHash("badHashType", "plaintext"));

    assertEquals("java.security.NoSuchAlgorithmException: "
        + "badHashType MessageDigest not available", e.getMessage());
  }
}
