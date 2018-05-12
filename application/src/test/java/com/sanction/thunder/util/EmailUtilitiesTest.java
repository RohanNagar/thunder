package com.sanction.thunder.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailUtilitiesTest {
  private static final String URL_PLACEHOLDER = "CODEGEN-URL";

  @Test
  void testReplacePlaceholderNoUrl() {
    String contents = "test contents";
    String url = "http://www.test.com";

    assertEquals(contents, EmailUtilities.replaceUrlPlaceholder(contents, URL_PLACEHOLDER, url));
  }

  @Test
  void testReplacePlaceholderWithUrl() {
    String contents = "test contents " + URL_PLACEHOLDER;
    String url = "http://www.test.com";

    String expected = "test contents " + url;

    assertEquals(expected, EmailUtilities.replaceUrlPlaceholder(contents, URL_PLACEHOLDER, url));
  }
}
