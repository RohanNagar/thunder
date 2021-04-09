package com.sanctionco.thunder.dao.dynamodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanctionco.thunder.dao.DatabaseError;
import com.sanctionco.thunder.dao.DatabaseException;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.User;

import io.dropwizard.jackson.Jackson;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

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

class DynamoDbUsersDaoTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
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

  /* insert() */

  @Test
  void testSuccessfulInsert() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    User result = usersDao.insert(USER);

    verify(client, times(1)).putItem(any(PutItemRequest.class));

    // The creation time and update time will have been created on insert
    long creationTime = (Long) result.getProperties().get("creationTime");
    long updateTime = (Long) result.getProperties().get("lastUpdateTime");

    assertTrue(creationTime > CURR_TIME);
    assertTrue(updateTime > CURR_TIME);

    assertEquals(creationTime, updateTime);

    assertEquals(USER.withTime(creationTime, updateTime), result);
  }

  @Test
  void testConflictingInsert() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.putItem(any(PutItemRequest.class)))
        .thenThrow(ConditionalCheckFailedException.class);

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.insert(USER));

    assertEquals(DatabaseError.CONFLICT, e.getErrorKind());
    verify(client, times(1)).putItem(any(PutItemRequest.class));
  }

  @Test
  void testInsertWithUnsupportedData() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.putItem(any(PutItemRequest.class))).thenThrow(AwsServiceException.class);

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.insert(USER));

    assertEquals(DatabaseError.REQUEST_REJECTED, e.getErrorKind());
    verify(client, times(1)).putItem(any(PutItemRequest.class));
  }

  @Test
  void testInsertWithDatabaseDown() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.putItem(any(PutItemRequest.class))).thenThrow(SdkException.class);

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.insert(USER));

    assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
    verify(client, times(1)).putItem(any(PutItemRequest.class));
  }

  /* findByEmail() */

  @Test
  void testSuccessfulFindByEmail() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(GetItemResponse.builder().item(ITEM).build());

    User result = usersDao.findByEmail("test@test.com");

    assertEquals(USER.withTime(CURR_TIME, CURR_TIME), result);
    verify(client, times(1)).getItem(eq(GET_REQUEST));
  }

  @Test
  void testUnsuccessfulFindByEmail() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(GetItemResponse.builder().item(null).build());

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.findByEmail("test@test.com"));

    assertEquals(DatabaseError.USER_NOT_FOUND, e.getErrorKind());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
  }

  @Test
  void testUnsuccessfulFindByEmailEmptyItem() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(GetItemResponse.builder().item(Collections.emptyMap())
            .build());

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.findByEmail("test@test.com"));

    assertEquals(DatabaseError.USER_NOT_FOUND, e.getErrorKind());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
  }

  @Test
  void testFindByEmailDatabaseDown() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST))).thenThrow(SdkException.class);

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.findByEmail("test@test.com"));

    assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
  }

  /* update() */

  @Test
  void testSuccessfulUpdate() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(GetItemResponse.builder().item(ITEM).build());

    User result = usersDao.update(null, USER);

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
  void testSuccessfulEmailUpdate() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    GetItemRequest existingEmailRequest = GetItemRequest.builder()
        .tableName("testTable")
        .key(Collections.singletonMap("email",
            AttributeValue.builder().s("existingEmail").build()))
        .build();

    when(client.getItem(eq(existingEmailRequest)))
        .thenReturn(GetItemResponse.builder().item(ITEM).build());
    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(GetItemResponse.builder().item(null).build());

    User result = usersDao.update("existingEmail", USER);

    // The creation time and update time will have been reset since an email update
    // triggers a delete and insert
    long creationTime = (Long) result.getProperties().get("creationTime");
    long updateTime = (Long) result.getProperties().get("lastUpdateTime");

    assertTrue(creationTime > CURR_TIME);
    assertTrue(updateTime > CURR_TIME);

    assertEquals(creationTime, updateTime);

    assertEquals(USER.withTime(creationTime, updateTime), result);

    verify(client, times(1)).getItem(eq(existingEmailRequest));
    verify(client, times(1)).getItem(eq(GET_REQUEST));
    verify(client, times(1)).deleteItem(any(DeleteItemRequest.class));
    verify(client, times(1)).putItem(any(PutItemRequest.class));
  }

  @Test
  void testSameExistingEmail() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(GetItemResponse.builder().item(ITEM).build());

    User result = usersDao.update("test@test.com", USER);

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
  void testExistingUserWithNewEmail() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(GetItemResponse.builder().item(ITEM).build());

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.update("originalemail@gmail.com", USER));

    assertEquals(DatabaseError.CONFLICT, e.getErrorKind());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
  }

  @Test
  void testExistingUserWithNewEmailDatabaseDown() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST))).thenThrow(SdkException.class);

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.update("originalemail@gmail.com", USER));

    assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
  }

  @Test
  void testUpdateGetNotFound() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(GetItemResponse.builder().item(null).build());

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.update(null, USER));

    assertEquals(DatabaseError.USER_NOT_FOUND, e.getErrorKind());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
  }

  @Test
  void testUpdateGetNotFoundEmptyItem() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(GetItemResponse.builder().item(Collections.emptyMap()).build());

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.update(null, USER));

    assertEquals(DatabaseError.USER_NOT_FOUND, e.getErrorKind());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
  }

  @Test
  void testUpdateGetDatabaseDown() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST))).thenThrow(SdkException.class);

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.update(null, USER));

    assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
  }

  @Test
  void testUpdatePutConflict() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(GetItemResponse.builder().item(ITEM).build());
    when(client.putItem(any(PutItemRequest.class)))
        .thenThrow(ConditionalCheckFailedException.class);

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.update(null, USER));

    assertEquals(DatabaseError.CONFLICT, e.getErrorKind());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
    verify(client, times(1)).putItem(any(PutItemRequest.class));
  }

  @Test
  void testUpdatePutUnsupportedData() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(GetItemResponse.builder().item(ITEM).build());
    when(client.putItem(any(PutItemRequest.class)))
        .thenThrow(AwsServiceException.class);

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.update(null, USER));

    assertEquals(DatabaseError.REQUEST_REJECTED, e.getErrorKind());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
    verify(client, times(1)).putItem(any(PutItemRequest.class));
  }

  @Test
  void testUpdatePutDatabaseDown() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(GetItemResponse.builder().item(ITEM).build());
    when(client.putItem(any(PutItemRequest.class)))
        .thenThrow(SdkException.class);

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.update(null, USER));

    assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
    verify(client, times(1)).putItem(any(PutItemRequest.class));
  }

  /* delete() */

  @Test
  void testSuccessfulDelete() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(GetItemResponse.builder().item(ITEM).build());

    User result = usersDao.delete("test@test.com");

    assertEquals(USER.withTime(CURR_TIME, CURR_TIME), result);
    verify(client, times(1)).getItem(eq(GET_REQUEST));
    verify(client, times(1)).deleteItem(any(DeleteItemRequest.class));
  }

  @Test
  void testUnsuccessfulDeleteGetFailure() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST))).thenThrow(SdkException.class);

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.delete("test@test.com"));

    assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
  }

  @Test
  void testUnsuccessfulDeleteNotFound() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(GetItemResponse.builder().item(ITEM).build());
    when(client.deleteItem(any(DeleteItemRequest.class)))
        .thenThrow(ConditionalCheckFailedException.class);

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.delete("test@test.com"));

    assertEquals(DatabaseError.USER_NOT_FOUND, e.getErrorKind());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
    verify(client, times(1)).deleteItem(any(DeleteItemRequest.class));
  }

  @Test
  void testUnsuccessfulDeleteDatabaseDown() {
    DynamoDbClient client = mock(DynamoDbClient.class);

    UsersDao usersDao = new DynamoDbUsersDao(client, "testTable", MAPPER);

    when(client.getItem(eq(GET_REQUEST)))
        .thenReturn(GetItemResponse.builder().item(ITEM).build());
    when(client.deleteItem(any(DeleteItemRequest.class)))
        .thenThrow(SdkException.class);

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.delete("test@test.com"));

    assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
    verify(client, times(1)).getItem(eq(GET_REQUEST));
    verify(client, times(1)).deleteItem(any(DeleteItemRequest.class));
  }
}
