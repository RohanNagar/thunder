package com.sanction.thunder.dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseExceptionTest {

  @Test
  void testDatabaseExceptionCreation() {
    DatabaseException exception = new DatabaseException(DatabaseError.USER_NOT_FOUND);
    assertEquals(DatabaseError.USER_NOT_FOUND, exception.getErrorKind());

    exception = new DatabaseException("Error", DatabaseError.CONFLICT);
    assertEquals(DatabaseError.CONFLICT, exception.getErrorKind());
    assertEquals("Error", exception.getMessage());

    exception = new DatabaseException("Error", new RuntimeException(), DatabaseError.DATABASE_DOWN);
    assertEquals(DatabaseError.DATABASE_DOWN, exception.getErrorKind());
    assertEquals("Error", exception.getMessage());
    assertEquals(RuntimeException.class, exception.getCause().getClass());

    exception = new DatabaseException(new RuntimeException(), DatabaseError.REQUEST_REJECTED);
    assertEquals(DatabaseError.REQUEST_REJECTED, exception.getErrorKind());
    assertEquals(RuntimeException.class, exception.getCause().getClass());
  }
}
