package com.sanctionco.thunder.crypto.password;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Provides password-checking methods to ensure that
 * passwords hashed with BCrypt match.
 */
public class BCryptPasswordVerifier implements PasswordVerifier {

  /**
   * Determines if the given password matches the given hashed storedPassword.
   *
   * @param password The password to check for a match.
   * @param storedPassword The hashed password to check against.
   * @return True if they match, false otherwise
   */
  @Override
  public boolean isCorrectPassword(String password, String storedPassword) {
    return BCrypt.checkpw(password, storedPassword);
  }
}
