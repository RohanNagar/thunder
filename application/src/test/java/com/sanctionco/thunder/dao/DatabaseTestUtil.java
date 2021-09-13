package com.sanctionco.thunder.dao;

import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

/**
 * Provides utility methods for DAO unit tests.
 */
public class DatabaseTestUtil {
  private DatabaseTestUtil() {
  }

  /**
   * Assert that a {@link CompletionException} is thrown when executing the given operation,
   * and that the cause is a {@link DatabaseException} with the expected error.
   *
   * @param expectedError the expected database error that occurs within the operation
   * @param op the operation to run
   */
  public static void assertDatabaseError(DatabaseException.Error expectedError, Runnable op) {
    CompletionException e = assertThrowsExactly(CompletionException.class, op::run);

    assertInstanceOf(DatabaseException.class, e.getCause());
    var exp = (DatabaseException) e.getCause();

    assertEquals(expectedError, exp.getError());
  }
}
