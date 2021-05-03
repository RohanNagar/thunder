package com.sanctionco.thunder;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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
    var response = new ThunderException("A failed exception").response("test@test.com");

    assertEquals(500, response.getStatus());
    assertEquals("A failed exception (User: test@test.com)", response.getEntity());
  }

  @Test
  void testUnknownThrowableToResponse() {
    var response = ThunderException
        .responseFromThrowable(new IllegalStateException("failed"), "test");

    assertEquals(500, response.getStatus());
    assertEquals("An internal server error occurred (User: test)", response.getEntity());
  }

  @ParameterizedTest
  @MethodSource("throwableProvider")
  void testThrowableToResponse(Throwable throwable) {
    var response = ThunderException.responseFromThrowable(throwable, "test");

    assertEquals(500, response.getStatus());
    assertEquals("Failure (User: test)", response.getEntity());
  }

  @SuppressWarnings("unused")
  static Stream<Throwable> throwableProvider() {
    return Stream.of(
        new ThunderException("Failure"),
        new IllegalStateException(new ThunderException("Failure")),
        new RuntimeException(new IllegalStateException(new ThunderException("Failure"))));
  }
}
