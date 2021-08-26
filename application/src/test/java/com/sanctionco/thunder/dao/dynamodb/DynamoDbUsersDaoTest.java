package com.sanctionco.thunder.dao.dynamodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanctionco.thunder.TestResources;
import com.sanctionco.thunder.dao.DatabaseException;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.User;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import static com.sanctionco.thunder.dao.DatabaseTestUtil.assertDatabaseError;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DynamoDbUsersDaoTest {
  private static final ObjectMapper MAPPER = TestResources.MAPPER;
  private static final String TABLE_NAME = "testTable";
  private static final long CURR_TIME = Instant.now().toEpochMilli();

  private static final Email EMAIL = Email.unverified("test@test.com");
  private static final User USER = new User(EMAIL, "password",
      Collections.singletonMap("testProperty", "test"));
  private static final Map<String, AttributeValue> ITEM = new HashMap<>();

  private static final GetItemRequest GET_REQUEST = GetItemRequest.builder()
      .tableName(TABLE_NAME)
      .key(Collections.singletonMap("email",
          AttributeValue.builder().s(USER.getEmail().getAddress()).build()))
      .build();

  private static final GetItemResponse NULL_GET_RESPONSE = GetItemResponse.builder()
      .item(null).build();
  private static final GetItemResponse EMPTY_GET_RESPONSE = GetItemResponse.builder()
      .item(Collections.emptyMap()).build();

  @BeforeAll
  static void setup() {
    ITEM.put("email", AttributeValue.builder().s(USER.getEmail().getAddress()).build());
    ITEM.put("document", AttributeValue.builder().s(UsersDao.toJson(MAPPER, USER)).build());
    ITEM.put("creation_time", AttributeValue.builder().n(String.valueOf(CURR_TIME)).build());
    ITEM.put("update_time", AttributeValue.builder().n(String.valueOf(CURR_TIME)).build());
  }

  @Test
  void nullConstructorArgumentShouldThrow() {
    assertThrows(NullPointerException.class,
        () -> new DynamoDbUsersDao(null, null, null));
  }

  @Nested
  class Insert {

    @Test
    void shouldSucceed() {
      var dynamodb = mock(DynamoDbAsyncClient.class);
      var dao = new DynamoDbUsersDao(dynamodb, TABLE_NAME, MAPPER);

      when(dynamodb.putItem(any(PutItemRequest.class)))
          .thenReturn(completedFuture(PutItemResponse.builder().build()));

      var insertedUser = dao.insert(USER).join();

      verify(dynamodb).putItem(any(PutItemRequest.class));

      // The creation time and update time will have been created on insert
      long creationTime = (Long) insertedUser.getProperties().get("creationTime");
      long updateTime = (Long) insertedUser.getProperties().get("lastUpdateTime");

      assertAll("Ensure creation and update time were set",
          () -> assertTrue(creationTime > CURR_TIME),
          () -> assertTrue(updateTime > CURR_TIME),
          () -> assertEquals(creationTime, updateTime));

      assertEquals(USER.withTime(creationTime, updateTime), insertedUser);
    }

    @ParameterizedTest(name = "DAO returns {1} when DynamoDB throws {0}")
    @MethodSource("provideFailureTestArgs")
    void shouldFailCorrectly(Class<? extends Throwable> ex, DatabaseException.Error expected) {
      var dynamodb = mock(DynamoDbAsyncClient.class);
      var dao = new DynamoDbUsersDao(dynamodb, TABLE_NAME, MAPPER);

      when(dynamodb.putItem(any(PutItemRequest.class))).thenReturn(failedFuture(mock(ex)));

      assertDatabaseError(expected, () -> dao.insert(USER).join());
      verify(dynamodb).putItem(any(PutItemRequest.class));
    }

    static Stream<Arguments> provideFailureTestArgs() {
      return Stream.of(
          Arguments.of(ConditionalCheckFailedException.class, DatabaseException.Error.CONFLICT),
          Arguments.of(AwsServiceException.class, DatabaseException.Error.REQUEST_REJECTED),
          Arguments.of(SdkException.class, DatabaseException.Error.DATABASE_DOWN),
          Arguments.of(IllegalStateException.class, DatabaseException.Error.DATABASE_DOWN));
    }
  }

  @Nested
  class FindByEmail {

    @Test
    void shouldSucceed() {
      var dynamodb = mock(DynamoDbAsyncClient.class);
      var dao = new DynamoDbUsersDao(dynamodb, TABLE_NAME, MAPPER);

      when(dynamodb.getItem(eq(GET_REQUEST))).thenReturn(completedFuture(
          GetItemResponse.builder().item(ITEM).build()));

      var foundUser = dao.findByEmail("test@test.com").join();

      assertEquals(USER.withTime(CURR_TIME, CURR_TIME), foundUser);
      verify(dynamodb).getItem(eq(GET_REQUEST));
    }

    @ParameterizedTest(name = "DAO returns {1} when DynamoDB returns {0}")
    @MethodSource("provideFailureTestArgs")
    void shouldFailCorrectly(CompletableFuture<GetItemResponse> fut,
                             DatabaseException.Error expected) {
      var dynamodb = mock(DynamoDbAsyncClient.class);
      var dao = new DynamoDbUsersDao(dynamodb, TABLE_NAME, MAPPER);

      when(dynamodb.getItem(eq(GET_REQUEST))).thenReturn(fut);

      assertDatabaseError(expected, () -> dao.findByEmail("test@test.com").join());
      verify(dynamodb).getItem(eq(GET_REQUEST));
    }

    static Stream<Arguments> provideFailureTestArgs() {
      return Stream.of(
          Arguments.of(completedFuture(NULL_GET_RESPONSE), DatabaseException.Error.USER_NOT_FOUND),
          Arguments.of(completedFuture(EMPTY_GET_RESPONSE), DatabaseException.Error.USER_NOT_FOUND),
          Arguments.of(failedFuture(mock(SdkException.class)),
              DatabaseException.Error.DATABASE_DOWN),
          Arguments.of(failedFuture(mock(IllegalStateException.class)),
              DatabaseException.Error.DATABASE_DOWN));
    }
  }

  @Nested
  class Update {

    @ParameterizedTest(name = "when existing email is {0}")
    @NullSource
    @ValueSource(strings = {"test@test.com"})
    void shouldSucceed(String existingEmail) {
      var dynamodb = mock(DynamoDbAsyncClient.class);
      var dao = new DynamoDbUsersDao(dynamodb, TABLE_NAME, MAPPER);

      when(dynamodb.getItem(eq(GET_REQUEST)))
          .thenReturn(completedFuture(GetItemResponse.builder().item(ITEM).build()));
      when(dynamodb.putItem(any(PutItemRequest.class)))
          .thenReturn(completedFuture(PutItemResponse.builder()
              .attributes(Collections.singletonMap(
                  "creation_time",
                  AttributeValue.builder().n(String.valueOf(CURR_TIME)).build()))
              .build()));

      var updatedUser = dao.update(existingEmail, USER).join();

      // The creation time should stay the same, the update time should change.
      long creationTime = (Long) updatedUser.getProperties().get("creationTime");
      long updateTime = (Long) updatedUser.getProperties().get("lastUpdateTime");

      assertAll("Ensure creation and update time were set",
          () -> assertEquals(CURR_TIME, creationTime),
          () -> assertTrue(updateTime > CURR_TIME));

      assertEquals(USER.withTime(creationTime, updateTime), updatedUser);

      verify(dynamodb).getItem(eq(GET_REQUEST));
      verify(dynamodb).putItem(any(PutItemRequest.class));
      verify(dynamodb, never()).deleteItem(any(DeleteItemRequest.class));
    }

    @Test
    void shouldSucceedWithNewEmailAddress() {
      var dynamodb = mock(DynamoDbAsyncClient.class);
      var dao = new DynamoDbUsersDao(dynamodb, TABLE_NAME, MAPPER);

      GetItemRequest existingEmailRequest = GetItemRequest.builder()
          .tableName(TABLE_NAME)
          .key(Collections.singletonMap("email",
              AttributeValue.builder().s("existingEmail").build()))
          .build();

      when(dynamodb.getItem(eq(existingEmailRequest)))
          .thenReturn(completedFuture(GetItemResponse.builder().item(ITEM).build()));
      when(dynamodb.getItem(eq(GET_REQUEST)))
          .thenReturn(completedFuture(GetItemResponse.builder().item(null).build()));
      when(dynamodb.deleteItem(any(DeleteItemRequest.class)))
          .thenReturn(completedFuture(DeleteItemResponse.builder().attributes(ITEM).build()));
      when(dynamodb.putItem(any(PutItemRequest.class)))
          .thenReturn(completedFuture(PutItemResponse.builder().build()));

      var updatedUser = dao.update("existingEmail", USER).join();

      // The creation time and update time will have been reset since an email update
      // triggers a delete and insert
      long creationTime = (Long) updatedUser.getProperties().get("creationTime");
      long updateTime = (Long) updatedUser.getProperties().get("lastUpdateTime");

      assertAll("Ensure creation and update time were set",
          () -> assertTrue(creationTime > CURR_TIME),
          () -> assertTrue(updateTime > CURR_TIME),
          () -> assertEquals(creationTime, updateTime));

      assertEquals(USER.withTime(creationTime, updateTime), updatedUser);

      verify(dynamodb).getItem(eq(GET_REQUEST));
      verify(dynamodb).deleteItem(any(DeleteItemRequest.class));
      verify(dynamodb).putItem(any(PutItemRequest.class));
    }

    @ParameterizedTest(name = "DAO returns {1} when DynamoDB returns {0} with existing email {2}")
    @MethodSource("provideGetFailureTestArgs")
    void shouldFailWhenGetFails(CompletableFuture<GetItemResponse> fut,
                                DatabaseException.Error expected,
                                String existingEmail) {
      var dynamodb = mock(DynamoDbAsyncClient.class);
      var dao = new DynamoDbUsersDao(dynamodb, TABLE_NAME, MAPPER);

      when(dynamodb.getItem(eq(GET_REQUEST))).thenReturn(fut);

      assertDatabaseError(expected, () -> dao.update(existingEmail, USER).join());

      verify(dynamodb).getItem(eq(GET_REQUEST));
    }

    static Stream<Arguments> provideGetFailureTestArgs() {
      var existingItemResponse = GetItemResponse.builder().item(ITEM).build();

      return Stream.of(
          // Updating email should fail if the new address already exists as a user
          Arguments.of(completedFuture(existingItemResponse),
              DatabaseException.Error.CONFLICT, "originalemail@gmail.com"),
          // Updating email should fail if the GET request fails
          Arguments.of(failedFuture(mock(SdkException.class)),
              DatabaseException.Error.DATABASE_DOWN, "originalemail@gmail.com"),
          Arguments.of(failedFuture(mock(SdkException.class)),
              DatabaseException.Error.DATABASE_DOWN, null),
          // Updating the user should fail if there is no existing user
          Arguments.of(completedFuture(NULL_GET_RESPONSE),
              DatabaseException.Error.USER_NOT_FOUND, null),
          Arguments.of(completedFuture(EMPTY_GET_RESPONSE),
              DatabaseException.Error.USER_NOT_FOUND, null),
          // Updating the user should fail if there is an unknown error in the GET request
          Arguments.of(failedFuture(mock(IllegalStateException.class)),
              DatabaseException.Error.DATABASE_DOWN, null));
    }

    @ParameterizedTest(name = "DAO returns {1} when DynamoDB returns {0}")
    @MethodSource("provideUpdateFailureTestArgs")
    void shouldFailWhenUpdateFails(Class<? extends Throwable> ex,
                                   DatabaseException.Error expected) {
      var dynamodb = mock(DynamoDbAsyncClient.class);
      var dao = new DynamoDbUsersDao(dynamodb, TABLE_NAME, MAPPER);

      when(dynamodb.getItem(eq(GET_REQUEST)))
          .thenReturn(completedFuture(GetItemResponse.builder().item(ITEM).build()));
      when(dynamodb.putItem(any(PutItemRequest.class)))
          .thenReturn(failedFuture(mock(ex)));

      assertDatabaseError(expected, () -> dao.update(null, USER).join());

      verify(dynamodb).getItem(eq(GET_REQUEST));
      verify(dynamodb).putItem(any(PutItemRequest.class));
    }

    static Stream<Arguments> provideUpdateFailureTestArgs() {
      return Stream.of(
          Arguments.of(ConditionalCheckFailedException.class, DatabaseException.Error.CONFLICT),
          Arguments.of(AwsServiceException.class, DatabaseException.Error.REQUEST_REJECTED),
          Arguments.of(SdkException.class, DatabaseException.Error.DATABASE_DOWN),
          Arguments.of(IllegalStateException.class, DatabaseException.Error.DATABASE_DOWN));
    }
  }

  @Nested
  class Delete {

    @Test
    void shouldSucceed() {
      var dynamodb = mock(DynamoDbAsyncClient.class);
      var dao = new DynamoDbUsersDao(dynamodb, TABLE_NAME, MAPPER);

      when(dynamodb.deleteItem(any(DeleteItemRequest.class)))
          .thenReturn(completedFuture(DeleteItemResponse.builder().attributes(ITEM).build()));

      var deletedUser = dao.delete("test@test.com").join();

      assertEquals(USER.withTime(CURR_TIME, CURR_TIME), deletedUser);
      verify(dynamodb).deleteItem(any(DeleteItemRequest.class));
    }

    @ParameterizedTest(name = "DAO returns {1} when DynamoDB throws {0}")
    @MethodSource("provideFailureTestArgs")
    void shouldFailCorrectly(Class<? extends Throwable> ex, DatabaseException.Error expected) {
      var dynamodb = mock(DynamoDbAsyncClient.class);
      var dao = new DynamoDbUsersDao(dynamodb, TABLE_NAME, MAPPER);

      when(dynamodb.deleteItem(any(DeleteItemRequest.class))).thenReturn(failedFuture(mock(ex)));

      assertDatabaseError(expected, () -> dao.delete("test@test.com").join());
      verify(dynamodb).deleteItem(any(DeleteItemRequest.class));
    }

    static Stream<Arguments> provideFailureTestArgs() {
      return Stream.of(
          // Delete should fail if the user is not found
          Arguments.of(ConditionalCheckFailedException.class,
              DatabaseException.Error.USER_NOT_FOUND),
          Arguments.of(SdkException.class, DatabaseException.Error.DATABASE_DOWN),
          Arguments.of(IllegalStateException.class, DatabaseException.Error.DATABASE_DOWN));
    }
  }
}
