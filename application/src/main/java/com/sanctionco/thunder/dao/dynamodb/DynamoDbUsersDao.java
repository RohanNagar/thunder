package com.sanctionco.thunder.dao.dynamodb;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.sanctionco.thunder.dao.DatabaseError;
import com.sanctionco.thunder.dao.DatabaseException;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.models.User;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ExpectedAttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

/**
 * Provides the Amazon DynamoDB implementation for the {@link UsersDao}. Provides methods to
 * insert, update, get, and delete a {@code User} (in the {@code api} module) in the database.
 *
 * @see UsersDao
 */
public class DynamoDbUsersDao implements UsersDao {
  private static final Logger LOG = LoggerFactory.getLogger(DynamoDbUsersDao.class);

  private final DynamoDbClient dynamoDbClient;
  private final String tableName;
  private final ObjectMapper mapper;

  /**
   * Constructs a new {@code DynamoDbUsersDao} object with the given dynamoDbClient, table,
   * and mapper.
   *
   * @param dynamoDbClient the dynamoDbClient to perform operations on
   * @param tableName the name of the DynamoDB table to operate on
   * @param mapper the mapper used to serialize and deserialize JSON
   */
  @Inject
  public DynamoDbUsersDao(DynamoDbClient dynamoDbClient, String tableName, ObjectMapper mapper) {
    this.dynamoDbClient = Objects.requireNonNull(dynamoDbClient);
    this.tableName = Objects.requireNonNull(tableName);
    this.mapper = Objects.requireNonNull(mapper);
  }

  @Override
  public User insert(User user) {
    Objects.requireNonNull(user);

    long now = Instant.now().toEpochMilli();

    Map<String, AttributeValue> item = new HashMap<>();

    item.put("email", AttributeValue.builder().s(user.getEmail().getAddress()).build());
    item.put("id", AttributeValue.builder().s(UUID.randomUUID().toString()).build());
    item.put("version", AttributeValue.builder().s(UUID.randomUUID().toString()).build());
    item.put("creation_time", AttributeValue.builder().n(String.valueOf(now)).build());
    item.put("update_time", AttributeValue.builder().n(String.valueOf(now)).build());
    item.put("document", AttributeValue.builder().s(UsersDao.toJson(mapper, user)).build());

    PutItemRequest putItemRequest = PutItemRequest.builder()
        .tableName(tableName)
        .item(item)
        .expected(Collections.singletonMap("email",
            ExpectedAttributeValue.builder().exists(false).build()))
        .build();

    try {
      dynamoDbClient.putItem(putItemRequest);
    } catch (ConditionalCheckFailedException e) {
      LOG.error("The user {} already exists in the database.", user.getEmail(), e);
      throw new DatabaseException("The user already exists.",
          DatabaseError.CONFLICT);
    } catch (AwsServiceException e) {
      LOG.error("The database rejected the create request.", e);
      throw new DatabaseException("The database rejected the create request.",
          DatabaseError.REQUEST_REJECTED);
    } catch (SdkException e) {
      LOG.error("The database is currently unresponsive.", e);
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    return user;
  }

  @Override
  public User findByEmail(String email) {
    Objects.requireNonNull(email);

    GetItemRequest request = GetItemRequest.builder()
        .tableName(tableName)
        .key(Collections.singletonMap("email", AttributeValue.builder().s(email).build()))
        .build();

    GetItemResponse response;
    try {
      response = dynamoDbClient.getItem(request);
    } catch (SdkException e) {
      LOG.error("The database is currently unresponsive.", e);
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    if (response.item() == null || response.item().size() <= 0) {
      LOG.warn("The email {} was not found in the database.", email);
      throw new DatabaseException("The user was not found.", DatabaseError.USER_NOT_FOUND);
    }

    return UsersDao.fromJson(mapper, response.item().get("document").s());
  }

  @Override
  public User update(@Nullable String existingEmail, User user) {
    Objects.requireNonNull(user);

    // Different email (primary key) means we need to delete and insert
    if (existingEmail != null && !existingEmail.equals(user.getEmail().getAddress())) {
      LOG.info("User to update has new email. The user will be deleted and then reinserted.");
      return updateEmail(existingEmail, user);
    }

    // Get the old version
    GetItemRequest request = GetItemRequest.builder()
        .tableName(tableName)
        .key(Collections.singletonMap("email",
            AttributeValue.builder().s(user.getEmail().getAddress()).build()))
        .build();

    GetItemResponse response;
    try {
      response = dynamoDbClient.getItem(request);
    } catch (SdkException e) {
      LOG.error("The database is currently unresponsive.", e);
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    if (response.item() == null || response.item().size() <= 0) {
      LOG.warn("The user {} was not found in the database.", user.getEmail().getAddress());
      throw new DatabaseException("The user was not found.", DatabaseError.USER_NOT_FOUND);
    }

    // Compute the new data
    long now = Instant.now().toEpochMilli();
    String newVersion = UUID.randomUUID().toString();
    String document = UsersDao.toJson(mapper, user);

    // Build the new item
    Map<String, AttributeValue> newItem = new HashMap<>();

    // Fields that don't change
    newItem.put("email", response.item().get("email"));
    newItem.put("id", response.item().get("id"));
    newItem.put("creation_time", response.item().get("creation_time"));

    // Fields that do change
    newItem.put("version", AttributeValue.builder().s(newVersion).build());
    newItem.put("update_time", AttributeValue.builder().s(String.valueOf(now)).build());
    newItem.put("document", AttributeValue.builder().s(document).build());

    PutItemRequest putItemRequest = PutItemRequest.builder()
        .tableName(tableName)
        .item(newItem)
        .expected(Collections.singletonMap("version",
            ExpectedAttributeValue.builder()
                .comparisonOperator(ComparisonOperator.EQ)
                .value(response.item().get("version"))
                .build()))
        .build();

    try {
      dynamoDbClient.putItem(putItemRequest);
    } catch (ConditionalCheckFailedException e) {
      LOG.error("The user was updated while this update was in progress."
          + " Aborting to avoid race condition.", e);
      throw new DatabaseException("The user to update is at an unexpected stage.",
          DatabaseError.CONFLICT);
    } catch (AwsServiceException e) {
      LOG.error("The database rejected the update request.", e);
      throw new DatabaseException("The database rejected the update request.",
          DatabaseError.REQUEST_REJECTED);
    } catch (SdkException e) {
      LOG.error("The database is currently unresponsive.", e);
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    return user;
  }

  @Override
  public User delete(String email) {
    Objects.requireNonNull(email);

    // Get the item that will be deleted to return it
    Map<String, AttributeValue> primaryKey =
        Collections.singletonMap("email", AttributeValue.builder().s(email).build());

    GetItemRequest request = GetItemRequest.builder()
        .tableName(tableName)
        .key(primaryKey)
        .build();

    GetItemResponse response;
    try {
      response = dynamoDbClient.getItem(request);
    } catch (SdkException e) {
      LOG.error("The database is currently unresponsive.", e);
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
        .tableName(tableName)
        .key(primaryKey)
        .expected(Collections.singletonMap("email",
            ExpectedAttributeValue.builder()
                .value(AttributeValue.builder().s(email).build())
                .exists(true)
                .build()))
        .build();

    try {
      dynamoDbClient.deleteItem(deleteItemRequest);
    } catch (ConditionalCheckFailedException e) {
      LOG.warn("The email {} was not found in the database.", email, e);
      throw new DatabaseException("The user to delete was not found.",
          DatabaseError.USER_NOT_FOUND);
    } catch (SdkException e) {
      LOG.error("The database is currently unresponsive.", e);
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    return UsersDao.fromJson(mapper, response.item().get("document").s());
  }

  /**
   * Updates a user's email by first deleting the user in the database, then inserting
   * the user with the updated email.
   *
   * @param existingEmail the email of the user before the update
   * @param user the updated user object to put in the database
   * @return the user that was updated
   * @throws DatabaseException if the existing user was not found, the database was down,
   *     the database rejected the request, or a user with the new email address already exists
   */
  private User updateEmail(String existingEmail, User user) {
    try {
      // We have to make sure the new email address doesn't already exist
      findByEmail(user.getEmail().getAddress());

      // If code execution reaches here, we found the user without an error.
      // Since a user with the new email address was found, throw an exception.
      throw new DatabaseException("A user with the new email address already exists.",
          DatabaseError.CONFLICT);
    } catch (DatabaseException e) {
      // We got an exception when finding the user. If it is USER_NOT_FOUND, we are okay.
      // If it is not USER_NOT_FOUND, we need to throw the exception we got
      if (!e.getErrorKind().equals(DatabaseError.USER_NOT_FOUND)) {
        throw e;
      }
    }

    // If it doesn't exist, we can go ahead and delete & update
    delete(existingEmail);

    return insert(user);
  }
}
