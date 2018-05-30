package com.sanction.thunder.validation;

import com.sanction.thunder.models.User;

import javax.inject.Inject;
import javax.validation.ValidationException;

import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Provides validation logic for request validation for
 * {@link com.sanction.thunder.models.User User} properties.
 */
public class RequestValidator {

  private static final Logger LOG = LoggerFactory.getLogger(RequestValidator.class);

  private PropertyValidator propertyValidator;

  @Inject
  public RequestValidator(PropertyValidator propertyValidator) {
    this.propertyValidator = propertyValidator;
  }

  /**
   * Determines if user is valid by a given string.
   * @param user The user to test for validity
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
      throw new ValidationException("Cannot post a user with invalid properties");
    }
    
  }
  

  /**
   * Determines if user password and email are valid by given strings.
   * @param password The password to test for validity
   * @param email The email to test for validity
   */
  public void validate(String password, String email) {

    if (email == null || email.isEmpty()) {
      LOG.warn("Attempted to get a null user.");
      throw new ValidationException("Incorrect or missing email query parameter.");
    }

    if (password == null || password.isEmpty()) {
      LOG.warn("Attempted to get user {} without a password", email);
      throw new ValidationException("Incorrect or missing header credentials.");
    }
    
  }
  
  /**
   * Determines if user password, email and user are valid by given params.
   * @param password The password to test for validity
   * @param email The email to test for validity
   * @param user The user to test for validity
   */
  public void validate(String password, String email, User user) {
    
    if (user == null) {
      LOG.warn("Attempted to update a null user.");
      throw new ValidationException("Cannot put a null user.");
    }
    
    if (user.getEmail() == null) {
      LOG.warn("Attempted to update user without an email object.");
      throw new ValidationException("Cannot post a user without an email address.");
    }
    
    if (!isValidEmail(user.getEmail().getAddress())) {
      LOG.error("The new email address is invalid: {}", user.getEmail());
      throw new ValidationException("Invalid email address format. Please try again.");
    }
    
    if (password == null || password.isEmpty()) {
      LOG.warn("Attempted to update user {} without a password.", user.getEmail().getAddress());
      throw new ValidationException("Incorrect or missing header credentials.");
    }
    
    if (!propertyValidator.isValidPropertiesMap(user.getProperties())) {
      LOG.warn("Attempted to update a user with new invalid properties.");
      throw new ValidationException("Cannot post a user with invalid properties");
    }
  }

  /**
   * Determines if user password and email are valid by given strings.
   * The difference to validate(String password, String email) 
   * is that log messages are different here
   * @param password The password to test for validity
   * @param email The email to test for validity
   */
  public void validateDelete(String password, String email) {

    if (email == null || email.isEmpty()) {
      LOG.warn("Attempted to delete a null user.");
      throw new ValidationException("Incorrect or missing email query parameter.");
    }

    if (password == null || password.isEmpty()) {
      LOG.warn("Attempted to delete user {} without a password.", email);
      throw new ValidationException("Incorrect or missing header credentials.");
    }
  }


  /**
   * Determines if the given email string is valid or not.
   *
   * @param email
   *          The email address to validate.
   * @return True if the email is valid, false otherwise.
   */
  private boolean isValidEmail(String email) {
    return email != null && !email.isEmpty() && EmailValidator.getInstance().isValid(email);
  }
}
