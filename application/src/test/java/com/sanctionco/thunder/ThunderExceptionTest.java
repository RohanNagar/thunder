package com.sanctionco.thunder;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThunderExceptionTest {

  @Test
  void testConstruction() {
    ThunderException exception = new ThunderException("A failed exception");
    assertEquals("A failed exception", exception.getMessage());

    exception = new ThunderException("A failed exception", new IllegalStateException());
    assertEquals("A failed exception", exception.getMessage());
    assertEquals(IllegalStateException.class, exception.getCause().getClass());
  }

  @Test
  void testToResponse() {
    Response response = new ThunderException("A failed exception").response();

    assertEquals(500, response.getStatus());
    assertEquals("A failed exception", response.getEntity());
  }
}
