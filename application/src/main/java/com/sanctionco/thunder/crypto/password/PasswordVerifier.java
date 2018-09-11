package com.sanctionco.thunder.crypto.password;

/**
 * Defines a class that provides password-checking methods to ensure that
 * hashed passwords match.
 */
public interface PasswordVerifier {

  /**
   * Determines if the given password matches the given hashed storedPassword.
   *
   * @param password The password to check for a match.
   * @param storedPassword The hashed password to check against.
   * @return True if they match, false otherwise
   */
  boolean isCorrectPassword(String password, String storedPassword);
}
