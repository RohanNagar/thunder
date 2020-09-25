package com.sanctionco.thunder.email;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

class EmailHealthCheckTest {

  @Test
  void testCheckThrows() {
    EmailHealthCheck healthCheck = new EmailHealthCheck();

    IllegalStateException e = assertThrows(IllegalStateException.class, healthCheck::check);

    assertEquals("Cannot check the health of a generic Email provider! "
        + "Something went wrong during Thunder configuration initialization.", e.getMessage());
  }
}
