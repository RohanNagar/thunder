package com.sanction.thunder.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResponseTypeTest {

  @Test
  void testJsonResponseTypeMapping() {
    assertAll("Assert equal JSON enum and string value",
        () -> assertEquals(ResponseType.JSON, ResponseType.fromString("json")),
        () -> assertEquals("json", ResponseType.JSON.toString()));
  }

  @Test
  void testHtmlResponseTypeMapping() {
    assertAll("Assert equal HTML enum and string value",
        () -> assertEquals(ResponseType.HTML, ResponseType.fromString("html")),
        () -> assertEquals("html", ResponseType.HTML.toString()));
  }

  @Test
  void testNullResponseTypeFromString() {
    assertNull(ResponseType.fromString("unknown"));
  }
}
