package com.sanctionco.thunder.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanctionco.thunder.models.User;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

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
  CompletableFuture<User> insert(User user);

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
  CompletableFuture<User> update(@Nullable String existingEmail, User user);

  /**
   * Deletes the user with the given email in the DynamoDB database.
   *
   * @param email the email of the user to delete from the table
   * @return The user that was deleted
   * @throws DatabaseException if the user was not found or if the database was down
   */
  CompletableFuture<User> delete(String email);

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
    // TODO figure out how to chain this to the insert.
    // TODO Right now we have to join() on the find result to let any exceptions
    // TODO propagate.
    findByEmail(user.getEmail().getAddress(), true)
        .thenApply(result -> {
          // If code execution reaches here, we found the user without an error.
          // Since a user with the new email address was found, throw an exception.
          throw new DatabaseException("A user with the new email address already exists.",
              DatabaseError.CONFLICT);
        })
        .exceptionally(throwable -> {
          var cause = (DatabaseException) throwable.getCause();

          // We got an exception when finding the user. If it is USER_NOT_FOUND, we are okay.
          // If it is not USER_NOT_FOUND, we need to throw the exception we got
          if (!cause.getErrorKind().equals(DatabaseError.USER_NOT_FOUND)) {
            throw cause;
          }

          // Otherwise we're good
          return null;
        }).join();


    return insert(user)
        .thenCombine(delete(existingEmail), (inserted, deleted) -> inserted);
  }
}
