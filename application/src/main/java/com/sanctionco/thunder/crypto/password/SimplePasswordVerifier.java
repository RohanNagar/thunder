package com.sanctionco.thunder.crypto.password;

/**
 * Provides password-checking methods to ensure that
 * passwords hashed with simple algorithms match.
 */
public class SimplePasswordVerifier implements PasswordVerifier {

  /**
   * Determines if the given password matches the given hashed storedPassword.
   *
   * @param password The password to check for a match.
   * @param storedPassword The hashed password to check against.
   * @return True if they match, false otherwise
   */
  @Override
  public boolean isCorrectPassword(String password, String storedPassword) {
    return password.equals(storedPassword);
  }
}
