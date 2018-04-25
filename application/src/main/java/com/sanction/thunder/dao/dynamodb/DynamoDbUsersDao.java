package com.sanction.thunder.dao.dynamodb;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.document.Expected;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.sanction.thunder.dao.DatabaseError;
import com.sanction.thunder.dao.DatabaseException;
import com.sanction.thunder.dao.UsersDao;
import com.sanction.thunder.models.User;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

public class DynamoDbUsersDao implements UsersDao {
  private static final Logger LOG = LoggerFactory.getLogger(DynamoDbUsersDao.class);

  /** The DynamoDB table object to interact with. */
  private final Table table;

  /** A mapper for converting to and from JSON. */
  private final ObjectMapper mapper;

  @Inject
  public DynamoDbUsersDao(Table table, ObjectMapper mapper) {
    this.table = table;
    this.mapper = mapper;
  }

  /**
   * Inserts a new user into the DynamoDB database.
   *
   * @param user The object to insert.
   * @return The User object that was created.
   * @throws DatabaseException If the user already exists or if the database is down.
   */
  public User insert(User user) {
    checkNotNull(user);

    long now = Instant.now().toEpochMilli();
    Item item = new Item()
        .withPrimaryKey("email", user.getEmail().getAddress())
        .withString("id", UUID.randomUUID().toString())
        .withString("version", UUID.randomUUID().toString())
        .withLong("creation_time", now)
        .withLong("update_time", now)
        .withJSON("document", toJson(mapper, user));

    try {
      table.putItem(item, new Expected("email").notExist());
    } catch (ConditionalCheckFailedException e) {
      LOG.error("The user {} already exists in the database. Throwing DatabaseException.",
          user.getEmail());
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
    checkNotNull(email);

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

    return fromJson(mapper, item.getJSON("document"));
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
    checkNotNull(user);

    // Different emails means we need to delete and insert
    if (existingEmail != null && !existingEmail.equals(user.getEmail().getAddress())) {
      LOG.info("User to update has new email. The user will be deleted and then reinserted.");

      delete(existingEmail);

      return insert(user);
    }

    // Compute the new data
    long now = Instant.now().toEpochMilli();
    String newVersion = UUID.randomUUID().toString();
    String document = toJson(mapper, user);

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
    checkNotNull(email);

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

    return fromJson(mapper, item.getJSON("document"));
  }

  /**
   * Serializes a User object to a JSON String.
   *
   * @param mapper The object used to perform the JSON serialization.
   * @param object The object to serialize to JSON.
   * @return A String representing the JSON of the user object.
   */
  private static String toJson(ObjectMapper mapper, User object) {
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
  private static User fromJson(ObjectMapper mapper, String json) {
    try {
      return mapper.readValue(json, User.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
