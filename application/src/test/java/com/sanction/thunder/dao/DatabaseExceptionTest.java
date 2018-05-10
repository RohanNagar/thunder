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

    exception = new DatabaseException("Error", new Exception(), DatabaseError.DATABASE_DOWN);
    assertEquals(DatabaseError.DATABASE_DOWN, exception.getErrorKind());
    assertEquals("Error", exception.getMessage());

    exception = new DatabaseException(new Exception(), DatabaseError.REQUEST_REJECTED);
    assertEquals(DatabaseError.REQUEST_REJECTED, exception.getErrorKind());
  }
}
