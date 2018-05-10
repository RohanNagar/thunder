package com.sanction.thunder.email;

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

    exception = new EmailException("Test message", new Exception());
    assertEquals("Test message", exception.getMessage());

    exception = new EmailException(new Exception());
    assertEquals(Exception.class, exception.getCause().getClass());
  }
}
