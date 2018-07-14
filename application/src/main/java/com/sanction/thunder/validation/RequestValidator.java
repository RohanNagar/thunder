package com.sanction.thunder.validation;

import com.sanction.thunder.models.User;

import javax.inject.Inject;
import javax.validation.ValidationException;

import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides validation logic for request validation for
 * {@link User} properties.
 */
public class RequestValidator {
  private static final Logger LOG = LoggerFactory.getLogger(RequestValidator.class);

  private final PropertyValidator propertyValidator;

  @Inject
  public RequestValidator(PropertyValidator propertyValidator) {
    this.propertyValidator = propertyValidator;
  }

  /**
   * Determines if the User object is valid.
   *
   * @param user The user to test for validity
   * @throws ValidationException if validation fails
   */
  public void validate(User user) {
    if (user == null) {
      LOG.warn("Attempted to post a null user.");
      throw new ValidationException("Cannot post a null user.");
    }

    if (user.getEmail() == null) {
      LOG.warn("Attempted to post a user with a null Email object.");
      throw new ValidationException("Cannot post a user without an email address.");
    }

    if (!isValidEmail(user.getEmail().getAddress())) {
      LOG.error("The new user has an invalid email address: {}", user.getEmail());
      throw new ValidationException("Invalid email address format. Please try again.");
    }

    if (!propertyValidator.isValidPropertiesMap(user.getProperties())) {
      LOG.warn("Attempted to post a user with invalid properties.");
      throw new ValidationException("Cannot post a user with invalid properties.");
    }
  }

  /**
   * Determines if the given password and email are valid.
   *
   * @param password The password to test for validity
   * @param email The email to test for validity
   * @throws ValidationException if validation fails
   */
  public void validate(String password, String email) {
    if (email == null || email.isEmpty()) {
      LOG.warn("Attempted to operate on a null user.");
      throw new ValidationException("Incorrect or missing email query parameter.");
    }

    if (password == null || password.isEmpty()) {
      LOG.warn("Attempted to operate on user {} without a password.", email);
      throw new ValidationException("Incorrect or missing header credentials.");
    }
  }

  /**
   * Determines if the given password, email, and User object are valid.
   *
   * @param password The password to test for validity
   * @param email The email to test for validity
   * @param user The user to test for validity
   * @throws ValidationException if validation fails
   */
  public void validate(String password, String email, User user) {
    validate(user);
    validate(password, email);
  }

  /**
   * Determines if the given email string is valid or not.
   *
   * @param email The email address to validate.
   * @return True if the email is valid, false otherwise.
   */
  private boolean isValidEmail(String email) {
    return email != null && !email.isEmpty() && EmailValidator.getInstance().isValid(email);
  }
}
