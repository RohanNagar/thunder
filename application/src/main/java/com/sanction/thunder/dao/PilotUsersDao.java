package com.sanction.thunder.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Expected;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
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
   * @return The PilotUser object that was created or {@code null} if the create failed.
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
      return null;
    }

    return user;
  }

  /**
   * Find a PilotUser from the data store.
   *
   * @param username The username of the user to find.
   * @return The requested PilotUser or {@code null} if it does not exist.
   */
  public PilotUser findByUsername(String username) {
    checkNotNull(username);

    Item item = table.getItem("username", username);
    if (item == null) {
      return null;
    }

    return fromJson(mapper, item.getJSON("document"));
  }

  /**
   * Update a PilotUser in the data store.
   *
   * @param user The user object to update. Must have the same username as the one to update.
   * @return The PilotUser object that was updated or {@code null} if the updated failed.
   */
  public PilotUser update(PilotUser user) {
    checkNotNull(user);

    // Compute the new data
    long now = Instant.now().toEpochMilli();
    String newVersion = UUID.randomUUID().toString();
    String document = toJson(mapper, user);

    // Get the old version
    Item item = table.getItem("username", user.getUsername());
    if (item == null) {
      return null;
    }

    String oldVersion = item.getString("version");

    Item newItem = item
        .withString("version", newVersion)
        .withLong("update_time", now)
        .withJSON("document", document);

    try {
      table.putItem(newItem, new Expected("version").eq(oldVersion));
    } catch (ConditionalCheckFailedException e) {
      return null;
    }

    return user;
  }

  /**
   * Delete a PilotUser in the data store.
   *
   * @param username The username of the user to delete.
   * @return The PilotUser object that was deleted or {@code null} if the delete failed.
   */
  public PilotUser delete(String username) {
    checkNotNull(username);

    // Get the item that will be deleted to return it
    Item item = table.getItem("username", username);

    DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
        .withPrimaryKey("username", username)
        .withExpected(new Expected("username").exists());

    try {
      table.deleteItem(deleteItemSpec);
    } catch (ConditionalCheckFailedException e) {
      return null;
    }

    return fromJson(mapper, item.getJSON("document"));
  }

  private static String toJson(ObjectMapper mapper, PilotUser object) {
    try {
      return mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw Throwables.propagate(e);
    }
  }

  private static PilotUser fromJson(ObjectMapper mapper, String json) {
    try {
      return mapper.readValue(json, PilotUser.class);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

}
