package com.sanction.thunder.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Expected;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.sanction.thunder.models.StormUser;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

public class StormUsersDao {
  /** The DynamoDB table object to interact with. */
  private final Table table;

  /** A mapper for converting to and from JSON. */
  private final ObjectMapper mapper;

  @Inject
  public StormUsersDao(DynamoDB dynamo, ObjectMapper mapper) {
    this.table = dynamo.getTable("storm-users-prod");
    this.mapper = mapper;
  }

  /**
   * Insert a new StormUser into the data store.
   *
   * @param object The object to insert.
   * @return True if the object was inserted successfully, false otherwise.
   */
  public boolean insert(StormUser object) {
    checkNotNull(object);

    long now = Instant.now().toEpochMilli();
    Item item = new Item()
        .withPrimaryKey("username", object.getUsername())
        .withString("version", UUID.randomUUID().toString())
        .withLong("creation_time", now)
        .withLong("update_time", now)
        .withJSON("document", toJson(mapper, object));

    try {
      table.putItem(item, new Expected("username").notExist());
    } catch (ConditionalCheckFailedException e) {
      return false;
    }

    return true;
  }

  /**
   * Find a StormUser from the data store.
   *
   * @param username The username of the user to find.
   * @return The requested user or {@code null} if it does not exist.
   */
  public StormUser findByUsername(String username) {
    checkNotNull(username);

    Item item = table.getItem("username", username);
    if (item == null) {
      return null;
    }

    return fromJson(mapper, item.getJSON("document"));
  }

  /**
   * Update a StormUser in the data store.
   *
   * @param user The user object to update. Must have the same username as the one to update.
   * @return True if the user was updated successfully, false otherwise.
   */
  public boolean update(StormUser user) {
    checkNotNull(user);

    // Compute the new data
    long now = Instant.now().toEpochMilli();
    String newVersion = UUID.randomUUID().toString();
    String document = toJson(mapper, user);

    // Get the old version
    Item item = table.getItem("username", user.getUsername());
    String oldVersion = item.getString("version");

    Item newItem = item
        .withString("version", newVersion)
        .withLong("update_time", now)
        .withString("document", document);

    try {
      table.putItem(newItem, new Expected("version").eq(oldVersion));
    } catch (ConditionalCheckFailedException e) {
      return false;
    }

    return true;
  }

  private static String toJson(ObjectMapper mapper, StormUser object) {
    try {
      return mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw Throwables.propagate(e);
    }
  }

  private static StormUser fromJson(ObjectMapper mapper, String json) {
    try {
      return mapper.readValue(json, StormUser.class);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

}
