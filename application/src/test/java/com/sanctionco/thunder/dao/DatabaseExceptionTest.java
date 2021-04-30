package com.sanctionco.thunder.dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseExceptionTest {

  @Test
  void testDatabaseExceptionCreation() {
    DatabaseException exception = new DatabaseException("Error", DatabaseException.Error.CONFLICT);
    assertEquals(DatabaseException.Error.CONFLICT, exception.getError());
    assertEquals("Error", exception.getMessage());

    exception = new DatabaseException(
        "Error", new RuntimeException(), DatabaseException.Error.DATABASE_DOWN);
    assertEquals(DatabaseException.Error.DATABASE_DOWN, exception.getError());
    assertEquals("Error", exception.getMessage());
    assertEquals(RuntimeException.class, exception.getCause().getClass());
  }

  @Test
  void testResponse() {
    var response = new DatabaseException("Error", DatabaseException.Error.USER_NOT_FOUND)
        .response("test");
    assertEquals(404, response.getStatus());
    assertEquals("Error (User: test)", response.getEntity());

    response = new DatabaseException("Error", DatabaseException.Error.CONFLICT)
        .response("test");
    assertEquals(409, response.getStatus());
    assertEquals("Error (User: test)", response.getEntity());

    response = new DatabaseException("Error", DatabaseException.Error.DATABASE_DOWN)
        .response("test");
    assertEquals(503, response.getStatus());
    assertEquals("Error (User: test)", response.getEntity());

    response = new DatabaseException("Error", DatabaseException.Error.REQUEST_REJECTED)
        .response("test");
    assertEquals(500, response.getStatus());
    assertEquals("Error (User: test)", response.getEntity());
  }
}
