package com.sanctionco.thunder.dao.dynamodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanctionco.thunder.dao.DatabaseException;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.models.User;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ExpectedAttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;

/**
 * Provides the Amazon DynamoDB implementation for the {@link UsersDao}. Provides methods to
 * insert, update, get, and delete a {@code User} (in the {@code api} module) in the database.
 *
 * @see UsersDao
 */
public class DynamoDbUsersDao implements UsersDao {
  private static final Logger LOG = LoggerFactory.getLogger(DynamoDbUsersDao.class);

  private final DynamoDbAsyncClient dynamoDbClient;
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
  public DynamoDbUsersDao(DynamoDbAsyncClient dynamoDbClient,
                          String tableName,
                          ObjectMapper mapper) {
    this.dynamoDbClient = Objects.requireNonNull(dynamoDbClient);
    this.tableName = Objects.requireNonNull(tableName);
    this.mapper = Objects.requireNonNull(mapper);
  }

  @Override
  public CompletableFuture<User> insert(User user) {
    Objects.requireNonNull(user);

    long now = Instant.now().toEpochMilli();

    Map<String, AttributeValue> item = Map.of(
        "email", AttributeValue.builder().s(user.getEmail().getAddress()).build(),
        "id", AttributeValue.builder().s(UUID.randomUUID().toString()).build(),
        "version", AttributeValue.builder().s(UUID.randomUUID().toString()).build(),
        "creation_time", AttributeValue.builder().n(String.valueOf(now)).build(),
        "update_time", AttributeValue.builder().n(String.valueOf(now)).build(),
        "document", AttributeValue.builder().s(UsersDao.toJson(mapper, user)).build());

    PutItemRequest putItemRequest = PutItemRequest.builder()
        .tableName(tableName)
        .item(item)
        .expected(Collections.singletonMap("email",
            ExpectedAttributeValue.builder().exists(false).build()))
        .build();

    return dynamoDbClient.putItem(putItemRequest)
        .thenApply(response -> user.withTime(now, now))
        .exceptionally(throwable -> {
          throw convertToDatabaseException(throwable.getCause(), user.getEmail().getAddress());
        });
  }

  @Override
  public CompletableFuture<User> findByEmail(String email) {
    Objects.requireNonNull(email);

    GetItemRequest request = GetItemRequest.builder()
        .tableName(tableName)
        .key(Collections.singletonMap("email", AttributeValue.builder().s(email).build()))
        .build();

    return dynamoDbClient.getItem(request)
        .thenApply(response -> {
          if (response.item().size() <= 0) {
            LOG.warn("The email {} was not found in the database.", email);
            throw new DatabaseException("User not found in the database.",
                DatabaseException.Error.USER_NOT_FOUND);
          }

          return UsersDao.fromJson(mapper, response.item().get("document").s())
              .withTime(
                  Long.parseLong(response.item().get("creation_time").n()),
                  Long.parseLong(response.item().get("update_time").n()));
        }).exceptionally(throwable -> {
          throw convertToDatabaseException(throwable.getCause(), email);
        });
  }

  @Override
  public CompletableFuture<User> update(@Nullable String existingEmail, User user) {
    Objects.requireNonNull(user);

    // Different email (primary key) means we need to delete and insert
    if (existingEmail != null && !existingEmail.equals(user.getEmail().getAddress())) {
      LOG.info("User to update has new email. The user will be deleted and then reinserted.");
      return updateEmail(existingEmail, user);
    }

    long now = Instant.now().toEpochMilli();

    // Get the old version
    GetItemRequest request = GetItemRequest.builder()
        .tableName(tableName)
        .key(Collections.singletonMap("email",
            AttributeValue.builder().s(user.getEmail().getAddress()).build()))
        .build();

    return dynamoDbClient.getItem(request)
        .thenApply(response -> {
          if (response.item().size() <= 0) {
            LOG.warn("The email {} was not found in the database.", user.getEmail().getAddress());
            throw new DatabaseException("User not found in the database.",
                DatabaseException.Error.USER_NOT_FOUND);
          }

          // Compute the new data
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
          newItem.put("update_time", AttributeValue.builder().n(String.valueOf(now)).build());
          newItem.put("document", AttributeValue.builder().s(document).build());

          return PutItemRequest.builder()
              .tableName(tableName)
              .item(newItem)
              .expected(Collections.singletonMap("version",
                  ExpectedAttributeValue.builder()
                      .comparisonOperator(ComparisonOperator.EQ)
                      .value(response.item().get("version"))
                      .build()))
              .returnValues(ReturnValue.ALL_OLD)
              .build();
        })
        .thenCompose(dynamoDbClient::putItem)
        .thenApply(response ->
            user.withTime(Long.parseLong(response.attributes().get("creation_time").n()), now))
        .exceptionally(throwable -> {
          throw convertToDatabaseException(throwable.getCause(), user.getEmail().getAddress());
        });
  }

  @Override
  public CompletableFuture<User> delete(String email) {
    Objects.requireNonNull(email);

    DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
        .tableName(tableName)
        .key(Collections.singletonMap("email", AttributeValue.builder().s(email).build()))
        .expected(Collections.singletonMap("email",
            ExpectedAttributeValue.builder()
                .value(AttributeValue.builder().s(email).build())
                .exists(true)
                .build()))
        .returnValues(ReturnValue.ALL_OLD)
        .build();

    return dynamoDbClient.deleteItem(deleteItemRequest)
        .thenApply(response -> UsersDao.fromJson(mapper, response.attributes().get("document").s())
            .withTime(
                Long.parseLong(response.attributes().get("creation_time").n()),
                Long.parseLong(response.attributes().get("update_time").n())))
        .exceptionally(throwable -> {
          // First check ConditionalCheckFailedException, since we want to return a different
          // result than convertToDatabaseException() supplies
          if (throwable.getCause() instanceof ConditionalCheckFailedException) {
            LOG.warn("The email {} was not found in the database.", email, throwable);
            throw new DatabaseException("User not found in the database.",
                DatabaseException.Error.USER_NOT_FOUND);
          }

          throw convertToDatabaseException(throwable.getCause(), email);
        });
  }

  /**
   * Converts a throwable received from DynamoDB into a {@link DatabaseException}.
   *
   * @param throwable the throwable to convert
   * @param email the email address that was operated on
   * @return a new {@link DatabaseException}
   */
  private DatabaseException convertToDatabaseException(Throwable throwable, String email) {
    if (throwable instanceof DatabaseException) {
      return (DatabaseException) throwable;
    }

    if (throwable instanceof ConditionalCheckFailedException) {
      LOG.error("ConditionalCheck failed for insert/update of user {}.", email, throwable);
      return new DatabaseException("ConditionalCheck failed for insert/update. If this is an"
          + " update, try again. If this is a new user, a user with the same email address already"
          + " exists.",
          DatabaseException.Error.CONFLICT);
    }

    if (throwable instanceof AwsServiceException) {
      LOG.error("The database rejected the request (User {}).", email, throwable);
      return new DatabaseException("The database rejected the request."
          + " Check your data and try again.", DatabaseException.Error.REQUEST_REJECTED);
    }

    if (throwable instanceof SdkException) {
      LOG.error("The database is currently unresponsive (User {}).", email, throwable);
      return new DatabaseException("Database is currently unavailable. Please try again later.",
          DatabaseException.Error.DATABASE_DOWN);
    }

    LOG.error("Unknown database error (User {}).", email, throwable);
    return new DatabaseException("Unknown database error. Please try again later.",
        DatabaseException.Error.DATABASE_DOWN);
  }
}
