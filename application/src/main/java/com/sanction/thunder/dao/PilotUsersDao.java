package com.sanction.thunder.dao;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.document.Expected;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanction.thunder.models.PilotUser;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

public class PilotUsersDao {
  private static final Logger LOG = LoggerFactory.getLogger(PilotUsersDao.class);

  /** The DynamoDB table object to interact with. */
  private final Table table;

  /** A mapper for converting to and from JSON. */
  private final ObjectMapper mapper;

  @Inject
  public PilotUsersDao(Table table, ObjectMapper mapper) {
    this.table = table;
    this.mapper = mapper;
  }

  /**
   * Insert a new PilotUser into the data store.
   *
   * @param user The object to insert.
   * @return The PilotUser object that was created.
   * @throws DatabaseException If the user already exists or if the database is down.
   */
  public PilotUser insert(PilotUser user) {
    checkNotNull(user);

    long now = Instant.now().toEpochMilli();
    Item item = new Item()
        .withPrimaryKey("email", user.getEmail())
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
      LOG.error("The database received unsupported data.", e);
      throw new DatabaseException("Unsupported data sent to database.",
          DatabaseError.UNSUPPORTED_DATA);
    } catch (AmazonClientException e) {
      LOG.error("The database is currently unresponsive.", e);
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    return user;
  }

  /**
   * Find a PilotUser from the data store.
   *
   * @param email The email of the user to find.
   * @return The requested PilotUser or {@code null} if it does not exist.
   * @throws DatabaseException If the user does not exist or if the database is down.
   */
  public PilotUser findByEmail(String email) {
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
   * Update a PilotUser in the data store.
   *
   * @param user The user object to update. Must have the same email as the one to update.
   * @return The PilotUser object that was updated or {@code null} if the updated failed.
   * @throws DatabaseException If the user is not found, the database is down, or the update fails.
   */
  public PilotUser update(PilotUser user) {
    checkNotNull(user);

    // Compute the new data
    long now = Instant.now().toEpochMilli();
    String newVersion = UUID.randomUUID().toString();
    String document = toJson(mapper, user);

    // Get the old version
    Item item;
    try {
      item = table.getItem("email", user.getEmail());
    } catch (AmazonClientException e) {
      LOG.error("The database is currently unresponsive.", e);
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    if (item == null) {
      LOG.warn("The user {} was not found in the database.", user.getEmail());
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
      LOG.error("The database received unsupported data.", e);
      throw new DatabaseException("Unsupported data sent to database.",
          DatabaseError.UNSUPPORTED_DATA);
    } catch (AmazonClientException e) {
      LOG.error("The database is currently unresponsive.", e);
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    return user;
  }

  /**
   * Delete a PilotUser in the data store.
   *
   * @param email The email of the user to delete.
   * @return The PilotUser object that was deleted or {@code null} if the delete failed.
   * @throws DatabaseException If the user is not found or if the database is down.
   */
  public PilotUser delete(String email) {
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

  private static String toJson(ObjectMapper mapper, PilotUser object) {
    try {
      return mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private static PilotUser fromJson(ObjectMapper mapper, String json) {
    try {
      return mapper.readValue(json, PilotUser.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
