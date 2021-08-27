package com.sanctionco.thunder.util;

import com.google.common.io.Resources;

import java.io.IOException;
import java.nio.file.Files;
import java.security.interfaces.RSAPublicKey;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class FileUtilitiesTest {

  @Test
  void shouldReadTestFile() {
    assertEquals("test", FileUtilities.readFileAsResources("fixtures/test.txt"));
  }

  @Test
  void shouldThrowOnNonexistentFile() {
    IllegalStateException nonexistent = assertThrows(IllegalStateException.class,
        () -> FileUtilities.readFileAsResources("not-exist"));

    assertTrue(nonexistent.getCause() instanceof IllegalArgumentException);
  }

  @Test
  void shouldThrowOnError() {
    try (MockedStatic<Resources> resourcesMock = mockStatic(Resources.class)) {
      resourcesMock.when(() -> Resources.toString(any(), any())).thenThrow(IOException.class);

      IllegalStateException exception = assertThrows(IllegalStateException.class,
          () -> FileUtilities.readFileAsResources("not-exist"));

      assertTrue(exception.getCause() instanceof IOException);
    }
  }

  @Test
  void shouldReadRsaPublicKey() {
    RSAPublicKey key = FileUtilities.readPublicKeyFromPath(
        Resources.getResource("fixtures/test-rsa-public-key.der").getPath());

    assertNotNull(key);
    assertEquals("RSA", key.getAlgorithm());
  }

  @Test
  void rsaShouldThrowOnFileNotFound() {
    RuntimeException e = assertThrows(RuntimeException.class,
        () -> FileUtilities.readPublicKeyFromPath("not-exist"));

    assertTrue(e.getMessage().contains("Unable to read RSA public key"));
  }

  @Test
  void rsaShouldThrowOnError() {
    try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
      filesMock.when(() -> Files.readAllBytes(any())).thenThrow(IOException.class);

      RuntimeException e = assertThrows(RuntimeException.class,
          () -> FileUtilities.readPublicKeyFromPath(
              Resources.getResource("fixtures/test-rsa-public-key.der").getPath()));

      assertTrue(e.getMessage().contains("Unable to read RSA public key"));
    }
  }
}
