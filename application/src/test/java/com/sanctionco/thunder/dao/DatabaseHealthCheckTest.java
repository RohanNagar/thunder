package com.sanctionco.thunder.dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DatabaseHealthCheckTest {

  @Test
  void testCheckThrows() {
    DatabaseHealthCheck healthCheck = new DatabaseHealthCheck();

    IllegalStateException e = assertThrows(IllegalStateException.class, healthCheck::check);

    assertEquals("Cannot check the health of a generic Database! "
            + "Something went wrong during Thunder configuration initialization.", e.getMessage());
  }
}
