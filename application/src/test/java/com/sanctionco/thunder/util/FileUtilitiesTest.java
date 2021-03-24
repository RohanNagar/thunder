package com.sanctionco.thunder.util;

import com.google.common.io.Resources;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class FileUtilitiesTest {

  @Test
  void testConstructInstance() {
    new FileUtilities();
  }

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
}
