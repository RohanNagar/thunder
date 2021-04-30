package com.sanctionco.thunder.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestValidationExceptionTest {

  @Test
  void testConstruction() {
    RequestValidationException exception = new RequestValidationException(
        "Failed", RequestValidationException.Error.INCORRECT_PASSWORD);
    assertEquals("Failed", exception.getMessage());
    assertEquals(RequestValidationException.Error.INCORRECT_PASSWORD, exception.getError());

    exception = new RequestValidationException(
        "Fail", new IllegalStateException(), RequestValidationException.Error.INVALID_PARAMETERS);
    assertEquals("Fail", exception.getMessage());
    assertEquals(IllegalStateException.class, exception.getCause().getClass());
    assertEquals(RequestValidationException.Error.INVALID_PARAMETERS, exception.getError());
  }

  @Test
  void testStaticConstruction() {
    var exception = RequestValidationException.incorrectPassword();
    assertEquals(
        "Unable to validate the request with the provided credentials", exception.getMessage());
    assertEquals(RequestValidationException.Error.INCORRECT_PASSWORD, exception.getError());

    exception = RequestValidationException.incorrectPassword("My message");
    assertEquals("My message", exception.getMessage());
    assertEquals(RequestValidationException.Error.INCORRECT_PASSWORD, exception.getError());

    exception = RequestValidationException.invalidParameters();
    assertEquals("The request was malformed.", exception.getMessage());
    assertEquals(RequestValidationException.Error.INVALID_PARAMETERS, exception.getError());

    exception = RequestValidationException.invalidParameters("My message");
    assertEquals("My message", exception.getMessage());
    assertEquals(RequestValidationException.Error.INVALID_PARAMETERS, exception.getError());

    exception = RequestValidationException.incorrectToken();
    assertEquals("Incorrect verification token.", exception.getMessage());
    assertEquals(RequestValidationException.Error.INCORRECT_TOKEN, exception.getError());

    exception = RequestValidationException.incorrectToken("My message");
    assertEquals("My message", exception.getMessage());
    assertEquals(RequestValidationException.Error.INCORRECT_TOKEN, exception.getError());

    exception = RequestValidationException.tokenNotSet();
    assertEquals("Empty value found for user verification token.", exception.getMessage());
    assertEquals(RequestValidationException.Error.TOKEN_NOT_SET, exception.getError());

    exception = RequestValidationException.tokenNotSet("My message");
    assertEquals("My message", exception.getMessage());
    assertEquals(RequestValidationException.Error.TOKEN_NOT_SET, exception.getError());
  }

  @Test
  void testResponse() {
    var response = RequestValidationException.incorrectPassword("My message")
        .response("test.com");
    assertEquals(401, response.getStatus());
    assertEquals("My message (User: test.com)", response.getEntity());

    response = RequestValidationException.invalidParameters("My message 2").response("test2.com");
    assertEquals(400, response.getStatus());
    assertEquals("My message 2 (User: test2.com)", response.getEntity());

    response = RequestValidationException.incorrectToken("My message 2").response("test4.com");
    assertEquals(400, response.getStatus());
    assertEquals("My message 2 (User: test4.com)", response.getEntity());

    response = RequestValidationException.tokenNotSet("Unknown").response("test3.com");
    assertEquals(500, response.getStatus());
    assertEquals("Unknown (User: test3.com)", response.getEntity());
  }
}
