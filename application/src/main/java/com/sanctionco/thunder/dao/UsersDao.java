package com.sanctionco.thunder.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.sanctionco.thunder.models.User;

import java.io.IOException;
import javax.annotation.Nullable;

/**
 * Provides the base interface for the UsersDao. Provides methods to
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
  User insert(User user);

  /**
   * Retrieves the user with the given email from the DynamoDB table.
   *
   * @param email the email of the user to retrieve
   * @return the requested user
   * @throws DatabaseException if the user does not exist in the table or if the database was down
   */
  User findByEmail(String email);

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
  User update(@Nullable String existingEmail, User user);

  /**
   * Deletes the user with the given email in the DynamoDB database.
   *
   * @param email the email of the user to delete from the table
   * @return The user that was deleted
   * @throws DatabaseException if the user was not found or if the database was down
   */
  User delete(String email);

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
   * Deserializes a user from a JSON String.
   *
   * @param mapper the mapper used to perform JSON deserialization
   * @param json the JSON string to deserialize
   * @return the user object representation of the JSON
   */
  static User fromJson(ObjectMapper mapper, String json) {
    try {
      return mapper.readValue(json, User.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
