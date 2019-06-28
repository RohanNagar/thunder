package com.sanctionco.thunder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ThunderBuilderTest {

  @Test
  void testEnsureTrailingSlashExistsNoChange() {
    String url = "https://www.thunder.com/";
    String result = ThunderBuilder.ensureTrailingSlashExists(url);

    assertEquals(url, result);
  }

  @Test
  void testEnsureTrailingSlashExistsNoSlash() {
    String url = "https://www.thunder.com";
    String result = ThunderBuilder.ensureTrailingSlashExists(url);

    assertNotEquals(url, result);
    assertEquals("https://www.thunder.com/", result);
  }
}
