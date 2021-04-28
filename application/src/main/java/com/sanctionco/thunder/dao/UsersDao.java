package com.sanctionco.thunder.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanctionco.thunder.models.User;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.annotation.Nullable;

/**
 * Provides the base interface for the {@code UsersDao}. Provides methods to
 * insert, update, get, and delete a {@code User} (in the {@code api} module) in the database.
 */
public interface UsersDao {

  /**
   * Inserts the user into the DynamoDB table.
   *
   * @param user the user to insert
   * @return the user that was created in the table
   * @throws DatabaseException if the user already exists, the database rejected the request,
   *     or the database was down
   */
  default User insert(User user) {
    try {
      return insert(user, true).join();
    } catch (CompletionException exp) {
      throw (DatabaseException) exp.getCause();
    }
  }

  CompletableFuture<User> insert(User user, boolean unused);

  /**
   * Retrieves the user with the given email from the DynamoDB table.
   *
   * @param email the email of the user to retrieve
   * @return the requested user
   * @throws DatabaseException if the user does not exist in the table or if the database was down
   */
  default User findByEmail(String email) {
    try {
      return findByEmail(email, true).join();
    } catch (CompletionException exp) {
      throw (DatabaseException) exp.getCause();
    }
  }

  CompletableFuture<User> findByEmail(String email, boolean unused);

  /**
   * Updates the user in the DynamoDB database.
   *
   * @param existingEmail the email of the user before the update. If the user's email is
   *                      being updated, then this must not be {@code null}.
   * @param user the updated user object to put in the database
   * @return the user that was updated
   * @throws DatabaseException if the user was not found, the database was down, the database
   *     rejected the request, or the update failed
   */
  default User update(@Nullable String existingEmail, User user) {
    try {
      return update(existingEmail, user, true).join();
    } catch (CompletionException exp) {
      throw (DatabaseException) exp.getCause();
    }
  }

  CompletableFuture<User> update(@Nullable String existingEmail, User user, boolean unused);

  /**
   * Deletes the user with the given email in the DynamoDB database.
   *
   * @param email the email of the user to delete from the table
   * @return The user that was deleted
   * @throws DatabaseException if the user was not found or if the database was down
   */
  default User delete(String email) {
    try {
      return delete(email, true).join();
    } catch (CompletionException exp) {
      throw (DatabaseException) exp.getCause();
    }
  }

  CompletableFuture<User> delete(String email, boolean unused);

  /**
   * Serializes a user to a JSON String.
   *
   * @param mapper the mapper used to perform JSON serialization
   * @param user the user to serialize to JSON
   * @return the JSON string representation of the user
   */
  static String toJson(ObjectMapper mapper, User user) {
    try {
      return mapper.writeValueAsString(user);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * De-serializes a user from a JSON String.
   *
   * @param mapper the mapper used to perform JSON deserialization
   * @param json the JSON string to deserialize
   * @return the user object representation of the JSON
   */
  static User fromJson(ObjectMapper mapper, String json) {
    try {
      return mapper.readValue(json, User.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Updates a user's email by first inserting a new user with the updated email in the database,
   * then deleting the old user.
   *
   * @param existingEmail the email of the user before the update
   * @param user the updated user object to put in the database
   * @return the user that was updated
   * @throws DatabaseException if the existing user was not found, the database was down,
   *     the database rejected the request, or a user with the new email address already exists
   */
  default CompletableFuture<User> updateEmail(String existingEmail, User user) {
    try {
      // We have to make sure the new email address doesn't already exist
      findByEmail(user.getEmail().getAddress(), true).join();

      // If code execution reaches here, we found the user without an error.
      // Since a user with the new email address was found, throw an exception.
      throw new DatabaseException("A user with the new email address already exists.",
          DatabaseError.CONFLICT);
    } catch (CompletionException e) {
      if (!(e.getCause() instanceof DatabaseException exp)) {
        throw e;
      }

      // We got an exception when finding the user. If it is USER_NOT_FOUND, we are okay.
      // If it is not USER_NOT_FOUND, we need to throw the exception we got
      if (!exp.getErrorKind().equals(DatabaseError.USER_NOT_FOUND)) {
        throw e;
      }
    }

    CompletableFuture<User> result = insert(user, true);

    delete(existingEmail);

    return result;
  }
}
