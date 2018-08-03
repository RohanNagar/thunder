package com.sanctionco.thunder.email;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EmailExceptionTest {

  @Test
  void testEmailExceptionCreation() {
    EmailException exception = new EmailException();
    assertNull(exception.getMessage());

    exception = new EmailException("Test message");
    assertEquals("Test message", exception.getMessage());

    exception = new EmailException("Test message", new RuntimeException());
    assertEquals("Test message", exception.getMessage());
    assertEquals(RuntimeException.class, exception.getCause().getClass());

    exception = new EmailException(new NullPointerException());
    assertEquals(NullPointerException.class, exception.getCause().getClass());
  }
}
