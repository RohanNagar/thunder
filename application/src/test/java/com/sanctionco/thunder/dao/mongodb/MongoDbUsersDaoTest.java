package com.sanctionco.thunder.dao.mongodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoTimeoutException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.sanctionco.thunder.dao.DatabaseError;
import com.sanctionco.thunder.dao.DatabaseException;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.User;

import io.dropwizard.jackson.Jackson;

import java.util.Collections;

import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
public class MongoDbUsersDaoTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
  private static final Email EMAIL = new Email("test@test.com", true, "testToken");
  private static final User USER = new User(EMAIL, "password",
      Collections.singletonMap("testProperty", "test"));
  private static final Document DOCUMENT = new Document();

  @BeforeAll
  static void setup() {
    DOCUMENT.append("document", UsersDao.toJson(MAPPER, USER));
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

    User result = usersDao.insert(USER);

    assertEquals(USER, result);
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
    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    doThrow(MongoWriteException.class)
        .when(collection).insertOne(any(Document.class));

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.insert(USER));

    assertEquals(DatabaseError.CONFLICT, e.getErrorKind());
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

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.insert(USER));

    assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
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

    User result = usersDao.findByEmail("test@test.com");

    assertEquals(USER, result);
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

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.findByEmail("test@test.com"));

    assertEquals(DatabaseError.USER_NOT_FOUND, e.getErrorKind());
    verify(collection, times(1))
        .find(eq(Filters.eq("_id", "test@test.com")));
  }

  @Test
  void testFindByEmailDatabaseDown() {
    MongoCollection<Document> collection = mock(MongoCollection.class);
    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    doThrow(MongoTimeoutException.class).when(collection).find(any(Bson.class));

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.findByEmail("test@test.com"));

    assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
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

    User result = usersDao.update(null, USER);

    assertEquals(USER, result);
    verify(collection, times(1)).find(any(Bson.class));
    verify(collection, times(1)).updateOne(argThat((Bson bson) -> {
      BsonDocument doc = bson.toBsonDocument(
          BsonDocument.class,
          MongoClient.getDefaultCodecRegistry());
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

    User result = usersDao.update("existingEmail", USER);

    assertEquals(USER, result);

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

    User result = usersDao.update("test@test.com", USER);

    assertEquals(USER, result);

    verify(collection, times(1))
        .find(eq(Filters.eq("_id", "test@test.com")));

    verify(collection, times(1)).updateOne(argThat((Bson bson) -> {
      BsonDocument doc = bson.toBsonDocument(
          BsonDocument.class,
          MongoClient.getDefaultCodecRegistry());
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

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.update("originalemail@test.com", USER));

    assertEquals(DatabaseError.CONFLICT, e.getErrorKind());
    verify(collection, times(1))
        .find(eq(Filters.eq("_id", "test@test.com")));
  }

  @Test
  void testExistingUserWithNewEmailDatabaseDown() {
    MongoCollection<Document> collection = mock(MongoCollection.class);

    doThrow(MongoTimeoutException.class).when(collection).find(any(Bson.class));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.update("originalemail@test.com", USER));

    assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
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

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.update(null, USER));

    assertEquals(DatabaseError.USER_NOT_FOUND, e.getErrorKind());
    verify(collection, times(1))
        .find(eq(Filters.eq("_id", "test@test.com")));
  }

  @Test
  void testUpdateGetDatabaseDown() {
    MongoCollection<Document> collection = mock(MongoCollection.class);

    doThrow(MongoTimeoutException.class).when(collection).find(any(Bson.class));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.update(null, USER));

    assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
    verify(collection, times(1))
        .find(eq(Filters.eq("_id", "test@test.com")));
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

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.update(null, USER));

    assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
    verify(collection, times(1)).find(any(Bson.class));
    verify(collection, times(1)).updateOne(argThat((Bson bson) -> {
      BsonDocument doc = bson.toBsonDocument(
          BsonDocument.class,
          MongoClient.getDefaultCodecRegistry());
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

    User result = usersDao.delete("test@test.com");

    assertEquals(USER, result);
    verify(collection, times(1)).find(eq(Filters.eq("_id", "test@test.com")));
    verify(collection, times(1)).deleteOne(eq(Filters.eq("_id", "test@test.com")));
  }

  @Test
  void testUnsuccessfulDeleteGetFailure() {
    MongoCollection<Document> collection = mock(MongoCollection.class);

    doThrow(MongoTimeoutException.class).when(collection).find(any(Bson.class));

    MongoDbUsersDao usersDao = new MongoDbUsersDao(collection, MAPPER);

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.delete("test@test.com"));

    assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
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

    DatabaseException e = assertThrows(DatabaseException.class,
        () -> usersDao.delete("test@test.com"));

    assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
    verify(collection, times(1)).find(eq(Filters.eq("_id", "test@test.com")));
    verify(collection, times(1)).deleteOne(eq(Filters.eq("_id", "test@test.com")));
  }
}
