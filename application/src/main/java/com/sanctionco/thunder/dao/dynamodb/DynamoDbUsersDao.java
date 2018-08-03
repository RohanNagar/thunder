package com.sanctionco.thunder.dao.dynamodb;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.document.Expected;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.sanctionco.thunder.dao.DatabaseError;
import com.sanctionco.thunder.dao.DatabaseException;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.models.User;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A database access object (DAO) that is used to interact with Amazon's DynamoDB.
 * The DAO provides implementation details to insert, update, get, and delete a {@link User}
 * in a DynamoDB database.
 *
 * @see User
 * @see UsersDao
 */
public class DynamoDbUsersDao implements UsersDao {
  private static final Logger LOG = LoggerFactory.getLogger(DynamoDbUsersDao.class);

  private final Table table;
  private final ObjectMapper mapper;

  /**
   * Constructs a new DynamoDbUsersDao object.
   *
   * @param table The DynamoDB Table to interact with.
   * @param mapper An mapper used to serialize and deserialize JSON.
   */
  @Inject
  public DynamoDbUsersDao(Table table, ObjectMapper mapper) {
    this.table = Objects.requireNonNull(table);
    this.mapper = Objects.requireNonNull(mapper);
  }

  /**
   * Inserts a new user into the DynamoDB database.
   *
   * @param user The object to insert.
   * @return The User object that was created.
   * @throws DatabaseException If the user already exists or if the database is down.
   */
  public User insert(User user) {
    Objects.requireNonNull(user);

    long now = Instant.now().toEpochMilli();
    Item item = new Item()
        .withPrimaryKey("email", user.getEmail().getAddress())
        .withString("id", UUID.randomUUID().toString())
        .withString("version", UUID.randomUUID().toString())
        .withLong("creation_time", now)
        .withLong("update_time", now)
        .withJSON("document", UsersDao.toJson(mapper, user));

    try {
      table.putItem(item, new Expected("email").notExist());
    } catch (ConditionalCheckFailedException e) {
      LOG.error("The user {} already exists in the database.", user.getEmail(), e);
      throw new DatabaseException("The user already exists.",
          DatabaseError.CONFLICT);
    } catch (AmazonServiceException e) {
      LOG.error("The database rejected the create request.", e);
      throw new DatabaseException("The database rejected the create request.",
          DatabaseError.REQUEST_REJECTED);
    } catch (AmazonClientException e) {
      LOG.error("The database is currently unresponsive.", e);
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    return user;
  }

  /**
   * Finds a user from the DynamoDB database.
   *
   * @param email The email of the user to find.
   * @return The requested User or {@code null} if it does not exist.
   * @throws DatabaseException If the user does not exist or if the database is down.
   */
  public User findByEmail(String email) {
    Objects.requireNonNull(email);

    Item item;
    try {
      item = table.getItem("email", email);
    } catch (AmazonClientException e) {
      LOG.error("The database is currently unresponsive.", e);
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    if (item == null) {
      LOG.warn("The email {} was not found in the database.", email);
      throw new DatabaseException("The user was not found.", DatabaseError.USER_NOT_FOUND);
    }

    return UsersDao.fromJson(mapper, item.getJSON("document"));
  }

  /**
   * Updates a user in the DynamoDB database.
   *
   * @param existingEmail The existing email of the user.
   *                      This must not be {@code null} if the email is to be changed.
   * @param user The new User object to put in the database.
   * @return The User object that was updated or {@code null} if the updated failed.
   * @throws DatabaseException If the user is not found, the database is down, or the update fails.
   */
  public User update(@Nullable String existingEmail, User user) {
    Objects.requireNonNull(user);

    // Different emails means we need to delete and insert
    if (existingEmail != null && !existingEmail.equals(user.getEmail().getAddress())) {
      LOG.info("User to update has new email. The user will be deleted and then reinserted.");

      delete(existingEmail);

      return insert(user);
    }

    // Compute the new data
    long now = Instant.now().toEpochMilli();
    String newVersion = UUID.randomUUID().toString();
    String document = UsersDao.toJson(mapper, user);

    // Get the old version
    Item item;
    try {
      item = table.getItem("email", user.getEmail().getAddress());
    } catch (AmazonClientException e) {
      LOG.error("The database is currently unresponsive.", e);
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    if (item == null) {
      LOG.warn("The user {} was not found in the database.", user.getEmail().getAddress());
      throw new DatabaseException("The user was not found.", DatabaseError.USER_NOT_FOUND);
    }

    String oldVersion = item.getString("version");

    Item newItem = item
        .withString("version", newVersion)
        .withLong("update_time", now)
        .withJSON("document", document);

    try {
      table.putItem(newItem, new Expected("version").eq(oldVersion));
    } catch (ConditionalCheckFailedException e) {
      LOG.error("The user was updated while this update was in progress."
          + " Aborting to avoid race condition.", e);
      throw new DatabaseException("The user to update is at an unexpected stage.",
          DatabaseError.CONFLICT);
    } catch (AmazonServiceException e) {
      LOG.error("The database rejected the update request.", e);
      throw new DatabaseException("The database rejected the update request.",
          DatabaseError.REQUEST_REJECTED);
    } catch (AmazonClientException e) {
      LOG.error("The database is currently unresponsive.", e);
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    return user;
  }

  /**
   * Deletes a user in the DynamoDB database.
   *
   * @param email The email of the user to delete.
   * @return The User object that was deleted or {@code null} if the delete failed.
   * @throws DatabaseException If the user is not found or if the database is down.
   */
  public User delete(String email) {
    Objects.requireNonNull(email);

    // Get the item that will be deleted to return it
    Item item;
    try {
      item = table.getItem("email", email);
    } catch (AmazonClientException e) {
      LOG.error("The database is currently unresponsive.", e);
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
        .withPrimaryKey("email", email)
        .withExpected(new Expected("email").exists());

    try {
      table.deleteItem(deleteItemSpec);
    } catch (ConditionalCheckFailedException e) {
      LOG.warn("The email {} was not found in the database.", email, e);
      throw new DatabaseException("The user to delete was not found.",
          DatabaseError.USER_NOT_FOUND);
    } catch (AmazonClientException e) {
      LOG.error("The database is currently unresponsive.", e);
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    return UsersDao.fromJson(mapper, item.getJSON("document"));
  }
}
