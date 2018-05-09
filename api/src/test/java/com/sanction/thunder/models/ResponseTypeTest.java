package com.sanction.thunder.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ResponseTypeTest {

  @Test
  public void testJsonResponseType() {
    assertAll("Assert equal JSON response type.",
        () -> assertEquals(ResponseType.JSON, ResponseType.fromString("json")),
        () -> assertEquals("json", ResponseType.JSON.toString()));
  }

  @Test
  public void testHtmlResponseType() {
    assertAll("Assert equal HTML response type.",
        () -> assertEquals(ResponseType.HTML, ResponseType.fromString("html")),
        () -> assertEquals("html", ResponseType.HTML.toString()));
  }

  @Test
  public void testNullResponseTypeFromString() {
    assertNull(ResponseType.fromString("unknown"));
  }
}
