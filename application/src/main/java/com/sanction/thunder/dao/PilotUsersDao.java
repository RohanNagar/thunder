package com.sanction.thunder.dao;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
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

import static com.google.common.base.Preconditions.checkNotNull;

public class PilotUsersDao {
  /** The DynamoDB table object to interact with. */
  private final Table table;

  /** A mapper for converting to and from JSON. */
  private final ObjectMapper mapper;

  @Inject
  public PilotUsersDao(DynamoDB dynamo, ObjectMapper mapper) {
    this.table = dynamo.getTable("storm-users-test");
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
        .withPrimaryKey("username", user.getUsername())
        .withString("id", UUID.randomUUID().toString())
        .withString("version", UUID.randomUUID().toString())
        .withLong("creation_time", now)
        .withLong("update_time", now)
        .withJSON("document", toJson(mapper, user));

    try {
      table.putItem(item, new Expected("username").notExist());
    } catch (ConditionalCheckFailedException e) {
      throw new DatabaseException("The user already exists.",
          DatabaseError.CONFLICT);
    } catch (AmazonClientException e) {
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    return user;
  }

  /**
   * Find a PilotUser from the data store.
   *
   * @param username The username of the user to find.
   * @return The requested PilotUser or {@code null} if it does not exist.
   * @throws DatabaseException If the user does not exist or if the database is down.
   */
  public PilotUser findByUsername(String username) {
    checkNotNull(username);

    Item item;
    try {
      item = table.getItem("username", username);
    } catch (AmazonClientException e) {
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    if (item == null) {
      throw new DatabaseException("The user was not found.", DatabaseError.USER_NOT_FOUND);
    }

    return fromJson(mapper, item.getJSON("document"));
  }

  /**
   * Update a PilotUser in the data store.
   *
   * @param user The user object to update. Must have the same username as the one to update.
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
      item = table.getItem("username", user.getUsername());
    } catch (AmazonClientException e) {
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    if (item == null) {
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
      throw new DatabaseException("The user to update is at an unexpected stage.",
          DatabaseError.CONFLICT);
    } catch (AmazonClientException e) {
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    return user;
  }

  /**
   * Delete a PilotUser in the data store.
   *
   * @param username The username of the user to delete.
   * @return The PilotUser object that was deleted or {@code null} if the delete failed.
   * @throws DatabaseException If the user is not found or if the database is down.
   */
  public PilotUser delete(String username) {
    checkNotNull(username);

    // Get the item that will be deleted to return it
    Item item;
    try {
      item = table.getItem("username", username);
    } catch (AmazonClientException e) {
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
        .withPrimaryKey("username", username)
        .withExpected(new Expected("username").exists());

    try {
      table.deleteItem(deleteItemSpec);
    } catch (ConditionalCheckFailedException e) {
      throw new DatabaseException("The user to delete was not found.",
          DatabaseError.USER_NOT_FOUND);
    } catch (AmazonClientException e) {
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
