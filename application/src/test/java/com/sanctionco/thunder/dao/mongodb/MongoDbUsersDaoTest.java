package com.sanctionco.thunder.dao.mongodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ErrorCategory;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.MongoWriteException;
import com.mongodb.WriteError;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.sanctionco.thunder.TestResources;
import com.sanctionco.thunder.dao.DatabaseException;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.User;

import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.CompletionException;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class MongoDbUsersDaoTest {
  private static final ObjectMapper MAPPER = TestResources.MAPPER;
  private static final Email EMAIL = new Email("test@test.com", true, "testToken");
  private static final User USER = new User(EMAIL, "password",
      Collections.singletonMap("testProperty", "test"));
  private static final Document DOCUMENT = new Document();
  private static final long CURR_TIME = Instant.now().toEpochMilli();

  @BeforeAll
  static void setup() {
    DOCUMENT.append("document", UsersDao.toJson(MAPPER, USER));
    DOCUMENT.append("creation_time", CURR_TIME);
    DOCUMENT.append("update_time", CURR_TIME);
  }

  @Test
  void testNullConstructorArgumentThrows() {
    assertThrows(NullPointerException.class,
        () -> new MongoDbUsersDao(null, null));
  }

  @Test
  void testSuccessfulInsert() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    User result = usersDao.insert(USER).join();

    // Insert will set the creation and update time
    long creationTime = (Long) result.getProperties().get("creationTime");
    long updateTime = (Long) result.getProperties().get("lastUpdateTime");

    assertAll("The creation and update time are correct",
        () -> assertTrue(creationTime > CURR_TIME),
        () -> assertTrue(updateTime > CURR_TIME),
        () -> assertEquals(creationTime, updateTime));

    assertEquals(USER.withTime(creationTime, updateTime), result);
    verify(collection, times(1)).insertOne(argThat(
        (Document doc) -> doc.containsKey("_id")
            && doc.containsKey("id")
            && doc.containsKey("version")
            && doc.containsKey("creation_time")
            && doc.containsKey("update_time")
            && doc.containsKey("document")));
  }

  @Test
  void testConflictingInsert() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    var exception = mock(MongoWriteException.class);
    var error = mock(WriteError.class);
    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    when(exception.getError()).thenReturn(error);
    when(error.getCategory()).thenReturn(ErrorCategory.DUPLICATE_KEY);

    doThrow(exception).when(collection).insertOne(any(Document.class));

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.insert(USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);

    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.CONFLICT, exp.getError());
    verify(collection, times(1)).insertOne(argThat(
        (Document doc) -> doc.containsKey("_id")
            && doc.containsKey("id")
            && doc.containsKey("version")
            && doc.containsKey("creation_time")
            && doc.containsKey("update_time")
            && doc.containsKey("document")));
  }

  @Test
  void testInsertTimeout() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    var exception = mock(MongoWriteException.class);
    var error = mock(WriteError.class);
    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    when(exception.getError()).thenReturn(error);
    when(error.getCategory()).thenReturn(ErrorCategory.EXECUTION_TIMEOUT);

    doThrow(exception).when(collection).insertOne(any(Document.class));

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.insert(USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);

    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.DATABASE_DOWN, exp.getError());
    verify(collection, times(1)).insertOne(argThat(
        (Document doc) -> doc.containsKey("_id")
            && doc.containsKey("id")
            && doc.containsKey("version")
            && doc.containsKey("creation_time")
            && doc.containsKey("update_time")
            && doc.containsKey("document")));
  }

  @Test
  void testInsertRejected() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    var exception = mock(MongoWriteException.class);
    var error = mock(WriteError.class);
    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    when(exception.getError()).thenReturn(error);
    when(error.getCategory()).thenReturn(ErrorCategory.UNCATEGORIZED);

    doThrow(exception).when(collection).insertOne(any(Document.class));

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.insert(USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);

    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.REQUEST_REJECTED, exp.getError());
    verify(collection, times(1)).insertOne(argThat(
        (Document doc) -> doc.containsKey("_id")
            && doc.containsKey("id")
            && doc.containsKey("version")
            && doc.containsKey("creation_time")
            && doc.containsKey("update_time")
            && doc.containsKey("document")));
  }

  @Test
  void testInsertWithDatabaseDown() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    doThrow(MongoTimeoutException.class)
        .when(collection).insertOne(any(Document.class));

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.insert(USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);

    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.DATABASE_DOWN, exp.getError());
    verify(collection, times(1)).insertOne(argThat(
        (Document doc) -> doc.containsKey("_id")
            && doc.containsKey("id")
            && doc.containsKey("version")
            && doc.containsKey("creation_time")
            && doc.containsKey("update_time")
            && doc.containsKey("document")));
  }

  @Test
  void testInsertWithRequestRejected() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    MongoCommandException exception = mock(MongoCommandException.class);
    when(exception.getErrorMessage()).thenReturn("Test error");

    doThrow(exception).when(collection).insertOne(any(Document.class));

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.insert(USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);

    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.REQUEST_REJECTED, exp.getError());
    verify(collection, times(1)).insertOne(argThat(
        (Document doc) -> doc.containsKey("_id")
            && doc.containsKey("id")
            && doc.containsKey("version")
            && doc.containsKey("creation_time")
            && doc.containsKey("update_time")
            && doc.containsKey("document")));
  }

  @Test
  void testInsertWithUnknownException() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    doThrow(new IllegalStateException()).when(collection).insertOne(any(Document.class));

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.insert(USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);

    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.DATABASE_DOWN, exp.getError());
    verify(collection, times(1)).insertOne(argThat(
        (Document doc) -> doc.containsKey("_id")
            && doc.containsKey("id")
            && doc.containsKey("version")
            && doc.containsKey("creation_time")
            && doc.containsKey("update_time")
            && doc.containsKey("document")));
  }

  @Test
  void testSuccessfulFindByEmail() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    FindIterable<Document> findIterable = mock(FindIterable.class);

    when(findIterable.first()).thenReturn(DOCUMENT);
    doReturn(findIterable).when(collection).find(any(Bson.class));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    User result = usersDao.findByEmail("test@test.com").join();

    assertEquals(USER.withTime(CURR_TIME, CURR_TIME), result);
    verify(collection, times(1))
        .find(eq(Filters.eq("_id", "test@test.com")));
  }

  @Test
  void testUnsuccessfulFindByEmailEmptyItem() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    FindIterable<Document> findIterable = mock(FindIterable.class);

    when(findIterable.first()).thenReturn(null);
    doReturn(findIterable).when(collection).find(any(Bson.class));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.findByEmail("test@test.com").join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.USER_NOT_FOUND, exp.getError());
    verify(collection, times(1))
        .find(eq(Filters.eq("_id", "test@test.com")));
  }

  @Test
  void testFindByEmailDatabaseDown() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    doThrow(MongoTimeoutException.class).when(collection).find(any(Bson.class));

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.findByEmail("test@test.com").join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.DATABASE_DOWN, exp.getError());
    verify(collection, times(1))
        .find(eq(Filters.eq("_id", "test@test.com")));
  }

  @Test
  void testFindByEmailRequestRejected() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    MongoCommandException exception = mock(MongoCommandException.class);
    when(exception.getErrorMessage()).thenReturn("Test error");

    doThrow(exception).when(collection).find(any(Bson.class));

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.findByEmail("test@test.com").join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.REQUEST_REJECTED, exp.getError());
    verify(collection, times(1))
        .find(eq(Filters.eq("_id", "test@test.com")));
  }

  @Test
  void testFindByEmailUnknownException() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    doThrow(new IllegalStateException()).when(collection).find(any(Bson.class));

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.findByEmail("test@test.com").join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.DATABASE_DOWN, exp.getError());
    verify(collection, times(1))
        .find(eq(Filters.eq("_id", "test@test.com")));
  }

  @Test
  void testSuccessfulUpdate() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    FindIterable<Document> findIterable = mock(FindIterable.class);

    when(findIterable.first()).thenReturn(DOCUMENT);
    doReturn(findIterable).when(collection).find(any(Bson.class));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    User result = usersDao.update(null, USER).join();

    // Update will set the update time
    long creationTime = (Long) result.getProperties().get("creationTime");
    long updateTime = (Long) result.getProperties().get("lastUpdateTime");

    assertAll("The creation and update time are correct",
        () -> assertEquals(CURR_TIME, creationTime),
        () -> assertTrue(updateTime > CURR_TIME));

    assertEquals(USER.withTime(creationTime, updateTime), result);
    verify(collection, times(1)).find(any(Bson.class));
    verify(collection, times(1)).updateOne(argThat((Bson bson) -> {
      BsonDocument doc = bson.toBsonDocument(
          BsonDocument.class,
          MongoClientSettings.getDefaultCodecRegistry());
      return doc.containsKey("version");
    }), any(Bson.class));
  }

  @Test
  void testSuccessfulEmailUpdate() {
    MongoCollection<Document> collection = mock(MongoCollection.class);

    FindIterable<Document> findIterableNew = mock(FindIterable.class);
    when(findIterableNew.first()).thenReturn(null);
    doReturn(findIterableNew).when(collection).find(eq(
        Filters.eq("_id", USER.getEmail().getAddress())));

    FindIterable<Document> findIterableExisting = mock(FindIterable.class);
    when(findIterableExisting.first()).thenReturn(DOCUMENT);
    doReturn(findIterableExisting).when(collection).find(eq(
        Filters.eq("_id", "existingEmail")));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    User result = usersDao.update("existingEmail", USER).join();

    // The creation time and update time will have been reset since an email update
    // triggers a delete and insert
    long creationTime = (Long) result.getProperties().get("creationTime");
    long updateTime = (Long) result.getProperties().get("lastUpdateTime");

    assertAll("The creation and update time are correct",
        () -> assertTrue(creationTime > CURR_TIME),
        () -> assertTrue(updateTime > CURR_TIME),
        () -> assertEquals(creationTime, updateTime));

    assertEquals(USER.withTime(creationTime, updateTime), result);

    verify(collection, times(1))
        .find(eq(Filters.eq("_id", "existingEmail")));

    verify(collection, times(1))
        .find(eq(Filters.eq("_id", "test@test.com")));

    verify(collection, times(1)).insertOne(argThat(
        (Document doc) -> doc.containsKey("_id")
            && doc.containsKey("id")
            && doc.containsKey("version")
            && doc.containsKey("creation_time")
            && doc.containsKey("update_time")
            && doc.containsKey("document")));

    verify(collection, times(1))
        .deleteOne(eq(Filters.eq("_id", "existingEmail")));
  }

  @Test
  void testSameExistingEmail() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    FindIterable<Document> findIterable = mock(FindIterable.class);

    when(findIterable.first()).thenReturn(DOCUMENT);
    doReturn(findIterable).when(collection).find(any(Bson.class));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    User result = usersDao.update("test@test.com", USER).join();

    // Update will set the update time
    long creationTime = (Long) result.getProperties().get("creationTime");
    long updateTime = (Long) result.getProperties().get("lastUpdateTime");

    assertAll("The creation and update time are correct",
        () -> assertEquals(CURR_TIME, creationTime),
        () -> assertTrue(updateTime > CURR_TIME));

    assertEquals(USER.withTime(creationTime, updateTime), result);

    verify(collection, times(1))
        .find(eq(Filters.eq("_id", "test@test.com")));

    verify(collection, times(1)).updateOne(argThat((Bson bson) -> {
      BsonDocument doc = bson.toBsonDocument(
          BsonDocument.class,
          MongoClientSettings.getDefaultCodecRegistry());
      return doc.containsKey("version");
    }), any(Bson.class));

    verify(collection, never()).deleteOne(any());
  }

  @Test
  void testExistingUserWithNewEmail() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    FindIterable<Document> findIterable = mock(FindIterable.class);

    when(findIterable.first()).thenReturn(DOCUMENT);
    doReturn(findIterable).when(collection).find(any(Bson.class));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.update("originalemail@test.com", USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.CONFLICT, exp.getError());
    verify(collection, times(1))
        .find(eq(Filters.eq("_id", "test@test.com")));
  }

  @Test
  void testExistingUserWithNewEmailDatabaseDown() {
    MongoCollection<Document> collection = mock(MongoCollection.class);

    doThrow(MongoTimeoutException.class).when(collection).find(any(Bson.class));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.update("originalemail@test.com", USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.DATABASE_DOWN, exp.getError());
    verify(collection, times(1))
        .find(eq(Filters.eq("_id", "test@test.com")));
  }

  @Test
  void testUpdateGetNotFound() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    FindIterable<Document> findIterable = mock(FindIterable.class);

    when(findIterable.first()).thenReturn(null);
    doReturn(findIterable).when(collection).find(any(Bson.class));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.update(null, USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.USER_NOT_FOUND, exp.getError());
    verify(collection, times(1))
        .find(eq(Filters.eq("_id", "test@test.com")));
  }

  @Test
  void testUpdateGetDatabaseDown() {
    MongoCollection<Document> collection = mock(MongoCollection.class);

    doThrow(MongoTimeoutException.class).when(collection).find(any(Bson.class));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.update(null, USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.DATABASE_DOWN, exp.getError());
    verify(collection, times(1))
        .find(eq(Filters.eq("_id", "test@test.com")));
  }

  @Test
  void testUpdateGetRequestRejected() {
    MongoCollection<Document> collection = mock(MongoCollection.class);

    MongoCommandException exception = mock(MongoCommandException.class);
    when(exception.getErrorMessage()).thenReturn("Test error");

    doThrow(exception).when(collection).find(any(Bson.class));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.update(null, USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.REQUEST_REJECTED, exp.getError());
    verify(collection, times(1))
        .find(eq(Filters.eq("_id", "test@test.com")));
  }

  @Test
  void testUpdatePutTimeout() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    FindIterable<Document> findIterable = mock(FindIterable.class);
    var exception = mock(MongoWriteException.class);
    var error = mock(WriteError.class);

    when(findIterable.first()).thenReturn(DOCUMENT);
    when(exception.getError()).thenReturn(error);
    when(error.getCategory()).thenReturn(ErrorCategory.EXECUTION_TIMEOUT);

    doReturn(findIterable).when(collection).find(any(Bson.class));
    doThrow(exception).when(collection).updateOne(any(Bson.class), any(Bson.class));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.update(null, USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.DATABASE_DOWN, exp.getError());
    verify(collection, times(1)).find(any(Bson.class));
    verify(collection, times(1)).updateOne(argThat((Bson bson) -> {
      BsonDocument doc = bson.toBsonDocument(
          BsonDocument.class,
          MongoClientSettings.getDefaultCodecRegistry());
      return doc.containsKey("version");
    }), any(Bson.class));
  }

  @Test
  void testUpdatePutRejected() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    FindIterable<Document> findIterable = mock(FindIterable.class);
    var exception = mock(MongoWriteException.class);
    var error = mock(WriteError.class);

    when(findIterable.first()).thenReturn(DOCUMENT);
    when(exception.getError()).thenReturn(error);
    when(error.getCategory()).thenReturn(ErrorCategory.UNCATEGORIZED);

    doReturn(findIterable).when(collection).find(any(Bson.class));
    doThrow(exception).when(collection).updateOne(any(Bson.class), any(Bson.class));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.update(null, USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.REQUEST_REJECTED, exp.getError());
    verify(collection, times(1)).find(any(Bson.class));
    verify(collection, times(1)).updateOne(argThat((Bson bson) -> {
      BsonDocument doc = bson.toBsonDocument(
          BsonDocument.class,
          MongoClientSettings.getDefaultCodecRegistry());
      return doc.containsKey("version");
    }), any(Bson.class));
  }

  @Test
  void testUpdatePutDatabaseDown() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    FindIterable<Document> findIterable = mock(FindIterable.class);

    when(findIterable.first()).thenReturn(DOCUMENT);
    doReturn(findIterable).when(collection).find(any(Bson.class));
    doThrow(MongoTimeoutException.class).when(collection)
        .updateOne(any(Bson.class), any(Bson.class));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.update(null, USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.DATABASE_DOWN, exp.getError());
    verify(collection, times(1)).find(any(Bson.class));
    verify(collection, times(1)).updateOne(argThat((Bson bson) -> {
      BsonDocument doc = bson.toBsonDocument(
          BsonDocument.class,
          MongoClientSettings.getDefaultCodecRegistry());
      return doc.containsKey("version");
    }), any(Bson.class));
  }

  @Test
  void testUpdatePutRequestRejected() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    FindIterable<Document> findIterable = mock(FindIterable.class);

    MongoCommandException exception = mock(MongoCommandException.class);
    when(exception.getErrorMessage()).thenReturn("Test error");

    when(findIterable.first()).thenReturn(DOCUMENT);
    doReturn(findIterable).when(collection).find(any(Bson.class));
    doThrow(exception).when(collection)
        .updateOne(any(Bson.class), any(Bson.class));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.update(null, USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.REQUEST_REJECTED, exp.getError());
    verify(collection, times(1)).find(any(Bson.class));
    verify(collection, times(1)).updateOne(argThat((Bson bson) -> {
      BsonDocument doc = bson.toBsonDocument(
          BsonDocument.class,
          MongoClientSettings.getDefaultCodecRegistry());
      return doc.containsKey("version");
    }), any(Bson.class));
  }

  @Test
  void testUpdatePutRequestUnknownException() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    FindIterable<Document> findIterable = mock(FindIterable.class);

    when(findIterable.first()).thenReturn(DOCUMENT);
    doReturn(findIterable).when(collection).find(any(Bson.class));
    doThrow(new IllegalStateException()).when(collection)
        .updateOne(any(Bson.class), any(Bson.class));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.update(null, USER).join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.DATABASE_DOWN, exp.getError());
    verify(collection, times(1)).find(any(Bson.class));
    verify(collection, times(1)).updateOne(argThat((Bson bson) -> {
      BsonDocument doc = bson.toBsonDocument(
          BsonDocument.class,
          MongoClientSettings.getDefaultCodecRegistry());
      return doc.containsKey("version");
    }), any(Bson.class));
  }

  @Test
  void testSuccessfulDelete() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    FindIterable<Document> findIterable = mock(FindIterable.class);

    when(findIterable.first()).thenReturn(DOCUMENT);
    doReturn(findIterable).when(collection).find(any(Bson.class));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    User result = usersDao.delete("test@test.com").join();

    assertEquals(USER.withTime(CURR_TIME, CURR_TIME), result);
    verify(collection, times(1)).find(eq(Filters.eq("_id", "test@test.com")));
    verify(collection, times(1)).deleteOne(eq(Filters.eq("_id", "test@test.com")));
  }

  @Test
  void testUnsuccessfulDeleteGetFailure() {
    MongoCollection<Document> collection = mock(MongoCollection.class);

    doThrow(MongoTimeoutException.class).when(collection).find(any(Bson.class));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.delete("test@test.com").join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.DATABASE_DOWN, exp.getError());
    verify(collection, times(1)).find(any(Bson.class));
  }

  @Test
  void testUnsuccessfulDeleteDatabaseDown() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    FindIterable<Document> findIterable = mock(FindIterable.class);

    when(findIterable.first()).thenReturn(DOCUMENT);
    doReturn(findIterable).when(collection).find(any(Bson.class));
    doThrow(MongoTimeoutException.class).when(collection).deleteOne(any(Bson.class));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.delete("test@test.com").join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.DATABASE_DOWN, exp.getError());
    verify(collection, times(1)).find(eq(Filters.eq("_id", "test@test.com")));
    verify(collection, times(1)).deleteOne(eq(Filters.eq("_id", "test@test.com")));
  }

  @Test
  void testUnsuccessfulDeleteRequestRejected() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    FindIterable<Document> findIterable = mock(FindIterable.class);

    MongoCommandException exception = mock(MongoCommandException.class);
    when(exception.getErrorMessage()).thenReturn("Test error");

    when(findIterable.first()).thenReturn(DOCUMENT);
    doReturn(findIterable).when(collection).find(any(Bson.class));
    doThrow(exception).when(collection).deleteOne(any(Bson.class));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.delete("test@test.com").join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.REQUEST_REJECTED, exp.getError());
    verify(collection, times(1)).find(eq(Filters.eq("_id", "test@test.com")));
    verify(collection, times(1)).deleteOne(eq(Filters.eq("_id", "test@test.com")));
  }

  @Test
  void testUnsuccessfulDeleteUnknownException() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    FindIterable<Document> findIterable = mock(FindIterable.class);

    when(findIterable.first()).thenReturn(DOCUMENT);
    doReturn(findIterable).when(collection).find(any(Bson.class));
    doThrow(new IllegalStateException()).when(collection).deleteOne(any(Bson.class));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    CompletionException e = assertThrows(CompletionException.class,
        () -> usersDao.delete("test@test.com").join());

    assertTrue(e.getCause() instanceof DatabaseException);
    var exp = (DatabaseException) e.getCause();

    assertEquals(DatabaseException.Error.DATABASE_DOWN, exp.getError());
    verify(collection, times(1)).find(eq(Filters.eq("_id", "test@test.com")));
    verify(collection, times(1)).deleteOne(eq(Filters.eq("_id", "test@test.com")));
  }
}
