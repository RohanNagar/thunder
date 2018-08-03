package com.sanctionco.thunder.dao;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseErrorTest {

  @Test
  void testConflict() {
    String expected = "User test@test.com already exists in the database.";

    Response response = DatabaseError.CONFLICT.buildResponse("test@test.com");

    assertAll("The response information is correct",
        () -> assertEquals(Response.Status.CONFLICT, response.getStatusInfo()),
        () -> assertEquals(expected, response.getEntity()));
  }

  @Test
  void testUserNotFound() {
    String expected = "User test@test.com not found in the database.";

    Response response = DatabaseError.USER_NOT_FOUND.buildResponse("test@test.com");

    assertAll("The response information is correct",
        () -> assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo()),
        () -> assertEquals(expected, response.getEntity()));
  }

  @Test
  void testRequestRejected() {
    String expected = "The database rejected the request. Check your data and try again.";

    Response response = DatabaseError.REQUEST_REJECTED.buildResponse("test@test.com");

    assertAll("The response information is correct",
        () -> assertEquals(Response.Status.INTERNAL_SERVER_ERROR, response.getStatusInfo()),
        () -> assertEquals(expected, response.getEntity()));
  }

  @Test
  void testDatabaseDown() {
    String expected = "Database is currently unavailable. Please try again later.";

    Response response = DatabaseError.DATABASE_DOWN.buildResponse("test@test.com");

    assertAll("The response information is correct",
        () -> assertEquals(Response.Status.SERVICE_UNAVAILABLE, response.getStatusInfo()),
        () -> assertEquals(expected, response.getEntity()));
  }
}
