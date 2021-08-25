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
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DynamoDbUsersDaoTest {
  private static final ObjectMapper MAPPER = TestResources.MAPPER;
  private static final String TABLE_NAME = "testTable";
  private static final Email EMAIL = new Email("test@test.com", true, "testToken");
  private static final User USER = new User(EMAIL, "password",
      Collections.singletonMap("testProperty", "test"));
  private static final Map<String, AttributeValue> ITEM = new HashMap<>();
  private static final long CURR_TIME = Instant.now().toEpochMilli();

  private static final GetItemRequest GET_REQUEST = GetItemRequest.builder()
      .tableName("testTable")
      .key(Collections.singletonMap("email",
          AttributeValue.builder().s(USER.getEmail().getAddress()).build()))
      .build();

  @BeforeAll
  static void setup() {
    ITEM.put("email", AttributeValue.builder().s(USER.getEmail().getAddress()).build());
    ITEM.put("document", AttributeValue.builder().s(UsersDao.toJson(MAPPER, USER)).build());
    ITEM.put("creation_time", AttributeValue.builder().n(String.valueOf(CURR_TIME)).build());
    ITEM.put("update_time", AttributeValue.builder().n(String.valueOf(CURR_TIME)).build());
  }

  @Test
  void testNullConstructorArgumentThrows() {
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

      var result = dao.insert(USER).join();

      verify(dynamodb).putItem(any(PutItemRequest.class));

      // The creation time and update time will have been created on insert
      long creationTime = (Long) result.getProperties().get("creationTime");
      long updateTime = (Long) result.getProperties().get("lastUpdateTime");

      assertAll("Ensure creation and update time were set",
          () -> assertTrue(creationTime > CURR_TIME),
          () -> assertTrue(updateTime > CURR_TIME),
          () -> assertEquals(creationTime, updateTime));

      assertEquals(USER.withTime(creationTime, updateTime), result);
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

      var result = dao.findByEmail("test@test.com").join();

      assertEquals(USER.withTime(CURR_TIME, CURR_TIME), result);
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
      var nullItemResponse = GetItemResponse.builder().item(null).build();
      var emptyItemResponse = GetItemResponse.builder().item(Collections.emptyMap()).build();

      return Stream.of(
          Arguments.of(completedFuture(nullItemResponse), DatabaseException.Error.USER_NOT_FOUND),
          Arguments.of(completedFuture(emptyItemResponse), DatabaseException.Error.USER_NOT_FOUND),
          Arguments.of(failedFuture(mock(SdkException.class)),
              DatabaseException.Error.DATABASE_DOWN),
          Arguments.of(failedFuture(mock(IllegalStateException.class)),
              DatabaseException.Error.DATABASE_DOWN));
    }
  }

  @Test
  void update_ShouldSucceed() {
    DynamoDbAsyncClient client = mock(DynamoDbAsyncClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(completedFuture(
            GetItemResponse.builder().item(ITEM).build()));
    when(client.putItem(any(PutItemRequest.class)))
        .thenReturn(completedFuture(PutItemResponse.builder()
            .attributes(Collections.singletonMap(
                "creation_time",
                AttributeValue.builder().n(String.valueOf(CURR_TIME)).build()))
            .build()));

    User result = usersDao.update(null, USER).join();

    // The creation time should stay the same, the update time should change.
    long creationTime = (Long) result.getProperties().get("creationTime");
    long updateTime = (Long) result.getProperties().get("lastUpdateTime");

    assertEquals(CURR_TIME, creationTime);
    assertTrue(updateTime > CURR_TIME);

    assertEquals(USER.withTime(creationTime, updateTime), result);
    verify(client, times(1)).getItem(eq(GET_REQUEST));
    verify(client, times(1)).putItem(any(PutItemRequest.class));
  }

  @Test
  void update_WithExistingEmailShouldSucceed() {
    DynamoDbAsyncClient client = mock(DynamoDbAsyncClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    GetItemRequest existingEmailRequest = GetItemRequest.builder()
        .tableName("testTable")
        .key(Collections.singletonMap("email",
            AttributeValue.builder().s("existingEmail").build()))
        .build();

    when(client.getItem(eq(existingEmailRequest)))
        .thenReturn(completedFuture(
            GetItemResponse.builder().item(ITEM).build()));
    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(completedFuture(
            GetItemResponse.builder().item(null).build()));
    when(client.deleteItem(any(DeleteItemRequest.class)))
        .thenReturn(completedFuture(
            DeleteItemResponse.builder().attributes(ITEM).build()));
    when(client.putItem(any(PutItemRequest.class)))
        .thenReturn(completedFuture(
            PutItemResponse.builder().build()));

    User result = usersDao.update("existingEmail", USER).join();

    // The creation time and update time will have been reset since an email update
    // triggers a delete and insert
    long creationTime = (Long) result.getProperties().get("creationTime");
    long updateTime = (Long) result.getProperties().get("lastUpdateTime");

    assertTrue(creationTime > CURR_TIME);
    assertTrue(updateTime > CURR_TIME);

    assertEquals(creationTime, updateTime);

    assertEquals(USER.withTime(creationTime, updateTime), result);

    verify(client, times(1)).getItem(eq(GET_REQUEST));
    verify(client, times(1)).deleteItem(any(DeleteItemRequest.class));
    verify(client, times(1)).putItem(any(PutItemRequest.class));
  }

  @Test
  void update_WithSameExistingEmailShouldSucceed() {
    DynamoDbAsyncClient client = mock(DynamoDbAsyncClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(completedFuture(
            GetItemResponse.builder().item(ITEM).build()));
    when(client.putItem(any(PutItemRequest.class)))
        .thenReturn(completedFuture(PutItemResponse.builder()
            .attributes(Collections.singletonMap(
                "creation_time",
                AttributeValue.builder().n(String.valueOf(CURR_TIME)).build()))
            .build()));

    User result = usersDao.update("test@test.com", USER).join();

    // The creation time should stay the same, the update time should change.
    long creationTime = (Long) result.getProperties().get("creationTime");
    long updateTime = (Long) result.getProperties().get("lastUpdateTime");

    assertEquals(CURR_TIME, creationTime);
    assertTrue(updateTime > CURR_TIME);

    assertEquals(USER.withTime(creationTime, updateTime), result);

    verify(client, times(1)).getItem(eq(GET_REQUEST));
    verify(client, times(1)).putItem(any(PutItemRequest.class));
    verify(client, never()).deleteItem(any(DeleteItemRequest.class));
  }

  @Test
  void update_WithExistingUserOfNewEmailShouldFail() {
    DynamoDbAsyncClient client = mock(DynamoDbAsyncClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(completedFuture(
            GetItemResponse.builder().item(ITEM).build()));

    assertDatabaseError(DatabaseException.Error.CONFLICT,
        () -> usersDao.update("originalemail@gmail.com", USER).join());

    verify(client, times(1)).getItem(eq(GET_REQUEST));
  }

  @Test
  void update_GetWithNewEmailDatabaseDownShouldFail() {
    DynamoDbAsyncClient client = mock(DynamoDbAsyncClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(failedFuture(mock(SdkException.class)));

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.update("originalemail@gmail.com", USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.DATABASE_DOWN, exp.getError());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
  }

  @Test
  void update_NoExistingUserShouldFail() {
    DynamoDbAsyncClient client = mock(DynamoDbAsyncClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(completedFuture(
            GetItemResponse.builder().item(null).build()));

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.update(null, USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.USER_NOT_FOUND, exp.getError());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
  }

  @Test
  void update_EmptyExistingUserShouldFail() {
    DynamoDbAsyncClient client = mock(DynamoDbAsyncClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(completedFuture(
            GetItemResponse.builder().item(Collections.emptyMap()).build()));

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.update(null, USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.USER_NOT_FOUND, exp.getError());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
  }

  @Test
  void update_GetDatabaseDownShouldFail() {
    DynamoDbAsyncClient client = mock(DynamoDbAsyncClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(failedFuture(mock(SdkException.class)));

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.update(null, USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.DATABASE_DOWN, exp.getError());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
  }

  @Test
  void update_GetUnknownExceptionShouldFail() {
    DynamoDbAsyncClient client = mock(DynamoDbAsyncClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(failedFuture(mock(IllegalStateException.class)));

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.update(null, USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.DATABASE_DOWN, exp.getError());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
  }

  @Test
  void update_VersionConflictShouldFail() {
    DynamoDbAsyncClient client = mock(DynamoDbAsyncClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(completedFuture(
            GetItemResponse.builder().item(ITEM).build()));
    when(client.putItem(any(PutItemRequest.class)))
        .thenReturn(failedFuture(mock(ConditionalCheckFailedException.class)));

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.update(null, USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.CONFLICT, exp.getError());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
    verify(client, times(1)).putItem(any(PutItemRequest.class));
  }

  @Test
  void update_DatabaseRejectionShouldFail() {
    DynamoDbAsyncClient client = mock(DynamoDbAsyncClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(completedFuture(
            GetItemResponse.builder().item(ITEM).build()));
    when(client.putItem(any(PutItemRequest.class)))
        .thenReturn(failedFuture(mock(AwsServiceException.class)));

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.update(null, USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.REQUEST_REJECTED, exp.getError());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
    verify(client, times(1)).putItem(any(PutItemRequest.class));
  }

  @Test
  void update_DatabaseDownShouldFail() {
    DynamoDbAsyncClient client = mock(DynamoDbAsyncClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(completedFuture(GetItemResponse.builder().item(ITEM).build()));
    when(client.putItem(any(PutItemRequest.class)))
        .thenReturn(failedFuture(mock(SdkException.class)));

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.update(null, USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.DATABASE_DOWN, exp.getError());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
    verify(client, times(1)).putItem(any(PutItemRequest.class));
  }

  @Test
  void update_UnknownExceptionShouldFail() {
    DynamoDbAsyncClient client = mock(DynamoDbAsyncClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(completedFuture(
            GetItemResponse.builder().item(ITEM).build()));
    when(client.putItem(any(PutItemRequest.class)))
        .thenReturn(failedFuture(mock(IllegalStateException.class)));

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.update(null, USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.DATABASE_DOWN, exp.getError());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
    verify(client, times(1)).putItem(any(PutItemRequest.class));
  }

  /* delete() */

  @Test
  void delete_ShouldSucceed() {
    DynamoDbAsyncClient client = mock(DynamoDbAsyncClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.deleteItem(any(DeleteItemRequest.class)))
        .thenReturn(completedFuture(DeleteItemResponse.builder()
            .attributes(ITEM)
            .build()));

    User result = usersDao.delete("test@test.com").join();

    assertEquals(USER.withTime(CURR_TIME, CURR_TIME), result);
    verify(client, times(1)).deleteItem(any(DeleteItemRequest.class));
  }

  @Test
  void delete_ItemNotFoundShouldFail() {
    DynamoDbAsyncClient client = mock(DynamoDbAsyncClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.deleteItem(any(DeleteItemRequest.class)))
        .thenReturn(failedFuture(mock(ConditionalCheckFailedException.class)));

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.delete("test@test.com").join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.USER_NOT_FOUND, exp.getError());
    verify(client, times(1)).deleteItem(any(DeleteItemRequest.class));
  }

  @Test
  void delete_DatabaseDownShouldFail() {
    DynamoDbAsyncClient client = mock(DynamoDbAsyncClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.deleteItem(any(DeleteItemRequest.class)))
        .thenReturn(failedFuture(mock(SdkException.class)));

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.delete("test@test.com").join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.DATABASE_DOWN, exp.getError());
    verify(client, times(1)).deleteItem(any(DeleteItemRequest.class));
  }

  @Test
  void delete_UnknownExceptionShouldFail() {
    DynamoDbAsyncClient client = mock(DynamoDbAsyncClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.deleteItem(any(DeleteItemRequest.class)))
        .thenReturn(failedFuture(mock(IllegalStateException.class)));

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.delete("test@test.com").join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.DATABASE_DOWN, exp.getError());
    verify(client, times(1)).deleteItem(any(DeleteItemRequest.class));
  }
}
