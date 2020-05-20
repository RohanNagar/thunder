package com.sanctionco.thunder.validation;

import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.User;

import java.util.Collections;

import javax.validation.ValidationException;

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
  private static final PropertyValidator PROPERTY_VALIDATOR = mock(PropertyValidator.class);

  private final RequestValidator validator = new RequestValidator(PROPERTY_VALIDATOR, true);

  @Test
  void testValidateUserNullUser() {
    ValidationException e = assertThrows(ValidationException.class, () -> validator.validate(null));

    assertEquals("Cannot post a null user.", e.getMessage());
  }

  @Test
  void testValidateUserNullEmail() {
    User user = new User(null, "password", Collections.emptyMap());

    ValidationException e = assertThrows(ValidationException.class, () -> validator.validate(user));

    assertEquals("Cannot post a user without an email address.", e.getMessage());
  }

  @Test
  void testValidateUserNullEmailAddress() {
    Email email = new Email(null, false, "token");
    User user = new User(email, "password", Collections.emptyMap());

    ValidationException e = assertThrows(ValidationException.class, () -> validator.validate(user));

    assertEquals("Invalid email address format. Please try again.", e.getMessage());
  }

  @Test
  void testValidateUserEmptyEmailAddress() {
    Email email = new Email("", false, "token");
    User user = new User(email, "password", Collections.emptyMap());

    ValidationException e = assertThrows(ValidationException.class, () -> validator.validate(user));

    assertEquals("Invalid email address format. Please try again.", e.getMessage());
  }

  @Test
  void testValidateUserInvalidEmailAddress() {
    Email email = new Email("notARealEmail", false, "token");
    User user = new User(email, "password", Collections.emptyMap());

    ValidationException e = assertThrows(ValidationException.class, () -> validator.validate(user));

    assertEquals("Invalid email address format. Please try again.", e.getMessage());
  }

  @Test
  void testValidateUserMismatchPropertyMap() {
    var propertyValidator = mock(PropertyValidator.class);
    var validator = new RequestValidator(propertyValidator, true);

    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(false);

    Email email = new Email("test@test.com", false, "token");
    User user = new User(email, "password", Collections.emptyMap());

    ValidationException e = assertThrows(ValidationException.class, () -> validator.validate(user));

    assertEquals("Cannot post a user with invalid properties.", e.getMessage());
  }

  @Test
  void testValidateUserSuccess() {
    var propertyValidator = mock(PropertyValidator.class);
    var validator = new RequestValidator(propertyValidator, true);

    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(true);

    Email email = new Email("test@test.com", false, "token");
    User user = new User(email, "password", Collections.emptyMap());

    assertDoesNotThrow(() -> validator.validate(user));
  }

  @Test
  void testValidatePasswordAndEmailNullEmail() {
    ValidationException e = assertThrows(ValidationException.class,
        () -> validator.validate("passsword", null, false));

    assertEquals("Incorrect or missing email query parameter.", e.getMessage());
  }

  @Test
  void testValidatePasswordAndEmailEmptyEmail() {
    ValidationException e = assertThrows(ValidationException.class,
        () -> validator.validate("passsword", "", false));

    assertEquals("Incorrect or missing email query parameter.", e.getMessage());
  }

  @Test
  void testValidatePasswordAndEmailNullPassword() {
    ValidationException e = assertThrows(ValidationException.class,
        () -> validator.validate(null, "test@test.com", false));

    assertEquals("Credentials are required to access this resource.", e.getMessage());
  }

  @Test
  void testValidatePasswordAndEmailEmptyPassword() {
    ValidationException e = assertThrows(ValidationException.class,
        () -> validator.validate("", "test@test.com", false));

    assertEquals("Credentials are required to access this resource.", e.getMessage());
  }

  @Test
  void testValidatePasswordAndEmailEmptyToken() {
    ValidationException e = assertThrows(ValidationException.class,
        () -> validator.validate("", "test@test.com", true));

    assertEquals("Incorrect or missing verification token query parameter.", e.getMessage());
  }

  @Test
  void testValidatePasswordAndEmailSuccess() {
    assertDoesNotThrow(() -> validator.validate("password", "test@test.com", false));
  }

  @Test
  void testValidateNullPassword() {
    var propertyValidator = mock(PropertyValidator.class);
    var validator = new RequestValidator(propertyValidator, true);

    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(true);

    Email email = new Email("test@test.com", false, "token");
    User user = new User(email, "password", Collections.emptyMap());

    ValidationException e = assertThrows(ValidationException.class,
        () -> validator.validate(null, "test@test.com", user));

    assertEquals("Credentials are required to access this resource.", e.getMessage());
  }

  @Test
  void testValidateEmptyPassword() {
    var propertyValidator = mock(PropertyValidator.class);
    var validator = new RequestValidator(propertyValidator, true);

    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(true);

    Email email = new Email("test@test.com", false, "token");
    User user = new User(email, "password", Collections.emptyMap());

    ValidationException e = assertThrows(ValidationException.class,
        () -> validator.validate("", "test@test.com", user));

    assertEquals("Credentials are required to access this resource.", e.getMessage());
  }

  @Test
  void testValidateSuccess() {
    var propertyValidator = mock(PropertyValidator.class);
    var validator = new RequestValidator(propertyValidator, true);

    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(true);

    Email email = new Email("test@test.com", false, "token");
    User user = new User(email, "password", Collections.emptyMap());

    assertDoesNotThrow(() -> validator.validate("password", "test@test.com", user));
  }

  /* Disable header check */
  @Test
  void testValidatePasswordAndEmailDisabledHeaderCheck() {
    var propertyValidator = mock(PropertyValidator.class);
    var validator = new RequestValidator(propertyValidator, false);

    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(true);

    Email email = new Email("test@test.com", false, "token");
    User user = new User(email, "password", Collections.emptyMap());

    assertDoesNotThrow(() -> validator.validate(null, "test@test.com", false));
    assertDoesNotThrow(() -> validator.validate("", "test@test.com", false));

    assertDoesNotThrow(() -> validator.validate(null, "test@test.com", user));
    assertDoesNotThrow(() -> validator.validate("", "test@test.com", user));
  }

  @Test
  void testIsPasswordHeaderCheckEnabled() {
    RequestValidator validator = new RequestValidator(PROPERTY_VALIDATOR, true);

    assertTrue(validator.isPasswordHeaderCheckEnabled());

    validator = new RequestValidator(PROPERTY_VALIDATOR, false);

    assertFalse(validator.isPasswordHeaderCheckEnabled());
  }
}
