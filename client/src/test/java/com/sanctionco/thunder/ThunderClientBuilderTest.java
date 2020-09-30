package com.sanctionco.thunder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThunderClientBuilderTest {

  @Test
  void buildShouldThrowWithoutSettingEndpoint() {
    NullPointerException e = assertThrows(NullPointerException.class,
        () -> new ThunderClientBuilder().build());

    assertTrue(e.getMessage().startsWith("You must provide an endpoint"));
  }

  @Test
  void buildShouldThrowWithoutSettingAuthentication() {
    NullPointerException e = assertThrows(NullPointerException.class,
        () -> new ThunderClientBuilder().endpoint("http://www.test.com").build());

    assertTrue(e.getMessage().startsWith("You must provide an authentication"));
  }

  @Test
  void shouldBuild() {
    new ThunderClientBuilder()
        .endpoint("http://www.test.com")
        .authentication("Test", "test")
        .build();
  }

  @Test
  void testEnsureTrailingSlashExistsNoChange() {
    String url = "https://www.thunder.com/";
    String result = ThunderClientBuilder.ensureTrailingSlashExists(url);

    assertEquals(url, result);
  }

  @Test
  void testEnsureTrailingSlashExistsNoSlash() {
    String url = "https://www.thunder.com";
    String result = ThunderClientBuilder.ensureTrailingSlashExists(url);

    assertNotEquals(url, result);
    assertEquals("https://www.thunder.com/", result);
  }
}
