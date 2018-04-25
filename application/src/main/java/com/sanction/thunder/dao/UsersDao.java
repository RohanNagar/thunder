package com.sanction.thunder.dao;

import com.sanction.thunder.models.User;
import javax.annotation.Nullable;

public interface UsersDao {

  /**
   * Insert a new User into the data store.
   *
   * @param user The object to insert.
   * @return The User object that was created.
   * @throws DatabaseException If the user already exists or if the database is down.
   */
  User insert(User user);

  /**
   * Find a User from the data store.
   *
   * @param email The email of the user to find.
   * @return The requested User or {@code null} if it does not exist.
   * @throws DatabaseException If the user does not exist or if the database is down.
   */
  User findByEmail(String email);

  /**
   * Update a User in the data store.
   *
   * @param existingEmail The existing email of the user.
   *                     This must not be {@code null} if the email is to be changed.
   * @param user The user object to update. Must have the same email as the one to update.
   * @return The User object that was updated or {@code null} if the updated failed.
   * @throws DatabaseException If the user is not found, the database is down, or the update fails.
   */
  User update(@Nullable String existingEmail, User user);

  /**
   * Delete a User in the data store.
   *
   * @param email The email of the user to delete.
   * @return The User object that was deleted or {@code null} if the delete failed.
   * @throws DatabaseException If the user is not found or if the database is down.
   */
  User delete(String email);
}
