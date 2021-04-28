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
import java.util.concurrent.CompletionException;

import javax.annotation.Nullable;
import javax.inject.Inject;

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
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
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
  @Inject
  public DynamoDbUsersDao(DynamoDbAsyncClient dynamoDbClient,
                          String tableName,
                          ObjectMapper mapper) {
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
      return dynamoDbClient.putItem(putItemRequest)
          .thenApply(response -> user.withTime(now, now))
          .exceptionally(throwable -> {
            throw convertToDatabaseException(throwable.getCause(), user.getEmail().getAddress());
          }).join();
    } catch (CompletionException e) {
      throw (DatabaseException) e.getCause();
    }
  }

  @Override
  public User findByEmail(String email) {
    Objects.requireNonNull(email);

    GetItemRequest request = GetItemRequest.builder()
        .tableName(tableName)
        .key(Collections.singletonMap("email", AttributeValue.builder().s(email).build()))
        .build();

    try {
      return dynamoDbClient.getItem(request)
          .thenApply(response -> {
            if (response.item().size() <= 0) {
              LOG.warn("The email {} was not found in the database.", email);
              throw new DatabaseException("The user was not found.", DatabaseError.USER_NOT_FOUND);
            }

            return UsersDao.fromJson(mapper, response.item().get("document").s())
                .withTime(
                    Long.parseLong(response.item().get("creation_time").n()),
                    Long.parseLong(response.item().get("update_time").n()));
          }).exceptionally(throwable -> {
            throw convertToDatabaseException(throwable.getCause(), email);
          }).join();
    } catch (CompletionException e) {
      throw (DatabaseException) e.getCause();
    }
  }

  @Override
  public User update(@Nullable String existingEmail, User user) {
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

    try {
      return dynamoDbClient.getItem(request)
          .thenApply(response -> {
            if (response.item().size() <= 0) {
              LOG.warn("The email {} was not found in the database.", user.getEmail().getAddress());
              throw new DatabaseException("The user was not found.", DatabaseError.USER_NOT_FOUND);
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
          }).join();
    } catch (CompletionException e) {
      throw (DatabaseException) e.getCause();
    }
  }

  @Override
  public User delete(String email) {
    Objects.requireNonNull(email);

    // TODO: chain these requests once we return a CompletableFuture<User>
    // Get the item that will be deleted to return it
    User user = findByEmail(email);

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

    try {
      return dynamoDbClient.deleteItem(deleteItemRequest)
          .thenApply(response -> user)
          .exceptionally(throwable -> {
            // First check ConditionalCheckFailedException, since we want to return a different
            // result than convertToDatabaseException() supplies
            if (throwable.getCause() instanceof ConditionalCheckFailedException) {
              LOG.warn("The email {} was not found in the database.", email, throwable);
              throw new DatabaseException("The user to delete was not found.",
                  DatabaseError.USER_NOT_FOUND);
            }

            throw convertToDatabaseException(throwable.getCause(), email);
          }).join();
    } catch (CompletionException e) {
      throw (DatabaseException) e.getCause();
    }
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
      throw new DatabaseException("ConditionalCheck failed for insert/update.",
          DatabaseError.CONFLICT);
    }

    if (throwable instanceof AwsServiceException) {
      LOG.error("The database rejected the request.", throwable);
      throw new DatabaseException("The database rejected the request.",
          DatabaseError.REQUEST_REJECTED);
    }

    if (throwable instanceof SdkException) {
      LOG.error("The database is currently unresponsive.", throwable);
      return new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    LOG.error("Unknown database error.", throwable);
    return new DatabaseException("Unknown database error.", DatabaseError.DATABASE_DOWN);
  }
}
