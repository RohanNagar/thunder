package com.sanctionco.thunder.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailUtilitiesTest {

  @Test
  void testConstructInstance() {
    new EmailUtilities();
  }

  @Test
  void testReplacePlaceholderNoUrl() {
    String contents = "test contents";
    String url = "http://www.test.com";

    assertEquals(contents, EmailUtilities.replaceUrlPlaceholder(contents, "CODEGEN-URL", url));
  }

  @Test
  void testReplacePlaceholderWithUrl() {
    String contents = "test contents CODEGEN-URL";
    String url = "http://www.test.com";

    String expected = "test contents " + url;

    assertEquals(expected, EmailUtilities.replaceUrlPlaceholder(contents, "CODEGEN-URL", url));
  }

  @Test
  void testReplaceWithCustomPlaceholder() {
    String contents = "test contents PLACEHOLDER";
    String url = "http://www.test.com";

    String expected = "test contents " + url;

    assertEquals(expected, EmailUtilities.replaceUrlPlaceholder(contents, "PLACEHOLDER", url));
  }
}
