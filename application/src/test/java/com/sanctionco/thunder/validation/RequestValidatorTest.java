package com.sanctionco.thunder.validation;

import com.sanctionco.jmail.EmailValidator;
import com.sanctionco.jmail.JMail;
import com.sanctionco.thunder.crypto.HashAlgorithm;
import com.sanctionco.thunder.crypto.HashService;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.User;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestValidatorTest {
  private static final EmailValidator EMAIL_VALIDATOR = JMail.strictValidator();
  private static final PropertyValidator PROPERTY_VALIDATOR = mock(PropertyValidator.class);
  private static final HashService HASH_SERVICE = HashAlgorithm.SIMPLE
      .newHashService(true, true);

  private final RequestValidator validator = new RequestValidator(
      EMAIL_VALIDATOR, PROPERTY_VALIDATOR, HASH_SERVICE, true);

  @Test
  void testValidateUserNullUser() {
    RequestValidationException e = assertThrows(RequestValidationException.class,
        () -> validator.validate(null));

    assertEquals("Cannot post a null user.", e.getMessage());
    assertEquals(RequestValidationException.Error.INVALID_PARAMETERS, e.getError());
  }

  @Test
  void testValidateUserNullEmail() {
    User user = new User(null, "password");

    RequestValidationException e = assertThrows(RequestValidationException.class,
        () -> validator.validate(user));

    assertEquals("Cannot post a user without an email address.", e.getMessage());
    assertEquals(RequestValidationException.Error.INVALID_PARAMETERS, e.getError());
  }

  @Test
  void testValidateUserNullEmailAddress() {
    Email email = new Email(null, false, "token");
    User user = new User(email, "password");

    RequestValidationException e = assertThrows(RequestValidationException.class,
        () -> validator.validate(user));

    assertEquals("Invalid email address format. Please try again.", e.getMessage());
    assertEquals(RequestValidationException.Error.INVALID_PARAMETERS, e.getError());
  }

  @Test
  void testValidateUserEmptyEmailAddress() {
    Email email = new Email("", false, "token");
    User user = new User(email, "password");

    RequestValidationException e = assertThrows(RequestValidationException.class,
        () -> validator.validate(user));

    assertEquals("Invalid email address format. Please try again.", e.getMessage());
    assertEquals(RequestValidationException.Error.INVALID_PARAMETERS, e.getError());
  }

  @Test
  void testValidateUserInvalidEmailAddress() {
    Email email = new Email("notARealEmail", false, "token");
    User user = new User(email, "password");

    RequestValidationException e = assertThrows(RequestValidationException.class,
        () -> validator.validate(user));

    assertEquals("Invalid email address format. Please try again.", e.getMessage());
    assertEquals(RequestValidationException.Error.INVALID_PARAMETERS, e.getError());
  }

  @Test
  void testValidateUserInvalidEmailAddressCustomRule() {
    var emailValidaor = JMail.strictValidator().withRule(e -> e.localPart().startsWith("hello"));
    var propertyValidator = mock(PropertyValidator.class);
    var validator = new RequestValidator(emailValidaor, propertyValidator, HASH_SERVICE, true);

    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(true);

    var user = new User(Email.unverified("test@test.com"), "password");

    RequestValidationException e = assertThrows(RequestValidationException.class,
        () -> validator.validate(user));

    assertEquals("Invalid email address format. Please try again.", e.getMessage());
    assertEquals(RequestValidationException.Error.INVALID_PARAMETERS, e.getError());

    var validUser = new User(Email.unverified("hello@test.com"), "password");

    assertDoesNotThrow(() -> validator.validate(validUser));
  }

  @Test
  void testValidateUserMismatchPropertyMap() {
    var propertyValidator = mock(PropertyValidator.class);
    var validator = new RequestValidator(EMAIL_VALIDATOR, propertyValidator, HASH_SERVICE, true);

    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(false);

    Email email = new Email("test@test.com", false, "token");
    User user = new User(email, "password");

    RequestValidationException e = assertThrows(RequestValidationException.class,
        () -> validator.validate(user));

    assertEquals("Cannot post a user with invalid properties.", e.getMessage());
    assertEquals(RequestValidationException.Error.INVALID_PARAMETERS, e.getError());
  }

  @Test
  void testValidateUserSuccess() {
    var propertyValidator = mock(PropertyValidator.class);
    var validator = new RequestValidator(EMAIL_VALIDATOR, propertyValidator, HASH_SERVICE, true);

    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(true);

    Email email = new Email("test@test.com", false, "token");
    User user = new User(email, "password");

    assertDoesNotThrow(() -> validator.validate(user));
  }

  @Test
  void testValidatePasswordAndEmailNullEmail() {
    RequestValidationException e = assertThrows(RequestValidationException.class,
        () -> validator.validate("passsword", null, false));

    assertEquals("Incorrect or missing email query parameter.", e.getMessage());
    assertEquals(RequestValidationException.Error.INVALID_PARAMETERS, e.getError());
  }

  @Test
  void testValidatePasswordAndEmailEmptyEmail() {
    RequestValidationException e = assertThrows(RequestValidationException.class,
        () -> validator.validate("passsword", "", false));

    assertEquals("Incorrect or missing email query parameter.", e.getMessage());
    assertEquals(RequestValidationException.Error.INVALID_PARAMETERS, e.getError());
  }

  @Test
  void testValidatePasswordAndEmailNullPassword() {
    RequestValidationException e = assertThrows(RequestValidationException.class,
        () -> validator.validate(null, "test@test.com", false));

    assertEquals("Credentials are required to access this resource.", e.getMessage());
    assertEquals(RequestValidationException.Error.INVALID_PARAMETERS, e.getError());
  }

  @Test
  void testValidatePasswordAndEmailEmptyPassword() {
    RequestValidationException e = assertThrows(RequestValidationException.class,
        () -> validator.validate("", "test@test.com", false));

    assertEquals("Credentials are required to access this resource.", e.getMessage());
    assertEquals(RequestValidationException.Error.INVALID_PARAMETERS, e.getError());
  }

  @Test
  void testValidatePasswordAndEmailEmptyToken() {
    RequestValidationException e = assertThrows(RequestValidationException.class,
        () -> validator.validate("", "test@test.com", true));

    assertEquals("Incorrect or missing verification token query parameter.", e.getMessage());
    assertEquals(RequestValidationException.Error.INVALID_PARAMETERS, e.getError());
  }

  @Test
  void testValidatePasswordAndEmailSuccess() {
    assertDoesNotThrow(() -> validator.validate("password", "test@test.com", false));
  }

  @Test
  void testValidateNullPassword() {
    var propertyValidator = mock(PropertyValidator.class);
    var validator = new RequestValidator(EMAIL_VALIDATOR, propertyValidator, HASH_SERVICE, true);

    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(true);

    Email email = new Email("test@test.com", false, "token");
    User user = new User(email, "password");

    RequestValidationException e = assertThrows(RequestValidationException.class,
        () -> validator.validate(null, "test@test.com", user));

    assertEquals("Credentials are required to access this resource.", e.getMessage());
    assertEquals(RequestValidationException.Error.INVALID_PARAMETERS, e.getError());
  }

  @Test
  void testValidateEmptyPassword() {
    var propertyValidator = mock(PropertyValidator.class);
    var validator = new RequestValidator(EMAIL_VALIDATOR, propertyValidator, HASH_SERVICE, true);

    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(true);

    Email email = new Email("test@test.com", false, "token");
    User user = new User(email, "password");

    RequestValidationException e = assertThrows(RequestValidationException.class,
        () -> validator.validate("", "test@test.com", user));

    assertEquals("Credentials are required to access this resource.", e.getMessage());
    assertEquals(RequestValidationException.Error.INVALID_PARAMETERS, e.getError());
  }

  @Test
  void testValidateSuccess() {
    var propertyValidator = mock(PropertyValidator.class);
    var validator = new RequestValidator(EMAIL_VALIDATOR, propertyValidator, HASH_SERVICE, true);

    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(true);

    Email email = new Email("test@test.com", false, "token");
    User user = new User(email, "password");

    assertDoesNotThrow(() -> validator.validate("password", "test@test.com", user));
  }

  /* Disable header check */
  @Test
  void testValidatePasswordAndEmailDisabledHeaderCheck() {
    var propertyValidator = mock(PropertyValidator.class);
    var validator = new RequestValidator(EMAIL_VALIDATOR, propertyValidator, HASH_SERVICE, false);

    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(true);

    Email email = new Email("test@test.com", false, "token");
    User user = new User(email, "password");

    assertDoesNotThrow(() -> validator.validate(null, "test@test.com", false));
    assertDoesNotThrow(() -> validator.validate("", "test@test.com", false));

    assertDoesNotThrow(() -> validator.validate(null, "test@test.com", user));
    assertDoesNotThrow(() -> validator.validate("", "test@test.com", user));
  }

  @Test
  void testIsPasswordHeaderCheckEnabled() {
    var validator = new RequestValidator(EMAIL_VALIDATOR, PROPERTY_VALIDATOR, HASH_SERVICE, true);

    assertTrue(validator.isPasswordHeaderCheckEnabled());

    validator = new RequestValidator(EMAIL_VALIDATOR, PROPERTY_VALIDATOR, HASH_SERVICE, false);

    assertFalse(validator.isPasswordHeaderCheckEnabled());
  }

  @Test
  void verifyPasswordCompletesWhenCheckIsDisabled() {
    var validator = new RequestValidator(EMAIL_VALIDATOR, PROPERTY_VALIDATOR, HASH_SERVICE, false);

    assertDoesNotThrow(() -> validator.verifyPasswordHeader("supply", "different"));
    assertDoesNotThrow(() -> validator.verifyPasswordHeader("supply", "supply"));
  }

  @Test
  void verifyPasswordCompletesWhenCheckIsEnabled() {
    var validator = new RequestValidator(EMAIL_VALIDATOR, PROPERTY_VALIDATOR, HASH_SERVICE, true);

    assertDoesNotThrow(() -> validator.verifyPasswordHeader("supply", "supply"));
    RequestValidationException e = assertThrows(RequestValidationException.class,
        () -> validator.verifyPasswordHeader("supply", "different"));

    assertEquals(e.getError(), RequestValidationException.Error.INCORRECT_PASSWORD);
  }
}
