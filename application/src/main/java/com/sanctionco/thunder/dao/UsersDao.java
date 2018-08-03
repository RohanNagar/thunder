package com.sanctionco.thunder.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.sanctionco.thunder.models.User;

import java.io.IOException;
import javax.annotation.Nullable;

/**
 * Defines methods to interact with a database containing user information.
 * Allows for creating, updating, finding, and deleting users from a database.
 *
 * @see User
 */
public interface UsersDao {

  /**
   * Inserts a new user into the database.
   *
   * @param user The User object to insert.
   * @return The User object that was created.
   * @throws DatabaseException If the user already exists or if the database is down.
   */
  User insert(User user);

  /**
   * Finds a user from the database.
   *
   * @param email The email of the user to find.
   * @return The requested User or {@code null} if it does not exist.
   * @throws DatabaseException If the user does not exist or if the database is down.
   */
  User findByEmail(String email);

  /**
   * Updates a user in the database.
   *
   * @param existingEmail The existing email of the user.
   *                      This must not be {@code null} if the email is to be changed.
   * @param user The new User object to put in the database.
   * @return The User object that was updated or {@code null} if the updated failed.
   * @throws DatabaseException If the user is not found, the database is down, or the update fails.
   */
  User update(@Nullable String existingEmail, User user);

  /**
   * Deletes a user in the database.
   *
   * @param email The email of the user to delete.
   * @return The User object that was deleted or {@code null} if the delete failed.
   * @throws DatabaseException If the user is not found or if the database is down.
   */
  User delete(String email);

  /**
   * Serializes a User object to a JSON String.
   *
   * @param mapper The object used to perform the JSON serialization.
   * @param object The object to serialize to JSON.
   * @return A String representing the JSON of the user object.
   */
  static String toJson(ObjectMapper mapper, User object) {
    try {
      return mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Deserializes a User object from a JSON String.
   *
   * @param mapper The object to perform the deserialization.
   * @param json The JSON String to deserialize.
   * @return A User object representing the JSON.
   */
  static User fromJson(ObjectMapper mapper, String json) {
    try {
      return mapper.readValue(json, User.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
