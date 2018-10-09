package com.sanctionco.thunder.validation;

import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.User;

import java.util.Collections;
import javax.validation.ValidationException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestValidatorTest {
  private static final PropertyValidator propertyValidator = mock(PropertyValidator.class);

  private final RequestValidator validator = new RequestValidator(propertyValidator);

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
    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(false);

    Email email = new Email("test@test.com", false, "token");
    User user = new User(email, "password", Collections.emptyMap());

    ValidationException e = assertThrows(ValidationException.class, () -> validator.validate(user));

    assertEquals("Cannot post a user with invalid properties.", e.getMessage());
  }

  @Test
  void testValidateUserSuccess() {
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

    assertEquals("Incorrect or missing header credentials.", e.getMessage());
  }

  @Test
  void testValidatePasswordAndEmailEmptyPassword() {
    ValidationException e = assertThrows(ValidationException.class,
        () -> validator.validate("", "test@test.com", false));

    assertEquals("Incorrect or missing header credentials.", e.getMessage());
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
    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(true);

    Email email = new Email("test@test.com", false, "token");
    User user = new User(email, "password", Collections.emptyMap());

    ValidationException e = assertThrows(ValidationException.class,
        () -> validator.validate(null, "test@test.com", user));

    assertEquals("Incorrect or missing header credentials.", e.getMessage());
  }

  @Test
  void testValidateEmptyPassword() {
    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(true);

    Email email = new Email("test@test.com", false, "token");
    User user = new User(email, "password", Collections.emptyMap());

    ValidationException e = assertThrows(ValidationException.class,
        () -> validator.validate("", "test@test.com", user));

    assertEquals("Incorrect or missing header credentials.", e.getMessage());
  }

  @Test
  void testValidateSuccess() {
    when(propertyValidator.isValidPropertiesMap(anyMap())).thenReturn(true);

    Email email = new Email("test@test.com", false, "token");
    User user = new User(email, "password", Collections.emptyMap());

    assertDoesNotThrow(() -> validator.validate("password", "test@test.com", user));
  }
}
