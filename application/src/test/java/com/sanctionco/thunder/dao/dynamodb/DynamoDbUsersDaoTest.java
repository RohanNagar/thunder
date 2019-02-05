//package com.sanctionco.thunder.dao.dynamodb;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import com.sanctionco.thunder.dao.DatabaseError;
//import com.sanctionco.thunder.dao.DatabaseException;
//import com.sanctionco.thunder.dao.UsersDao;
//import com.sanctionco.thunder.models.Email;
//import com.sanctionco.thunder.models.User;
//
//import io.dropwizard.jackson.Jackson;
//
//import java.util.Collections;
//
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//class DynamoDbUsersDaoTest {
//  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
//  private static final Item ITEM = mock(Item.class);
//  private static final Email EMAIL = new Email("test@test.com", true, "testToken");
//  private static final User USER = new User(EMAIL, "password",
//      Collections.singletonMap("testProperty", "test"));
//
//  private final Table table = mock(Table.class);
//
//  private final UsersDao usersDao = new DynamoDbUsersDao(table, MAPPER);
//
//  @BeforeAll
//  static void setup() {
//    when(ITEM.getJSON(anyString())).thenReturn(UsersDao.toJson(MAPPER, USER));
//    when(ITEM.getString(anyString())).thenReturn("example");
//
//    when(ITEM.withString(anyString(), anyString())).thenReturn(ITEM);
//    when(ITEM.withLong(anyString(), anyLong())).thenReturn(ITEM);
//    when(ITEM.withJSON(anyString(), anyString())).thenReturn(ITEM);
//  }
//
//  @Test
//  void testNullConstructorArgumentThrows() {
//    assertThrows(NullPointerException.class,
//        () -> new DynamoDbUsersDao(null, null));
//  }
//
//  /* insert() */
//
//  @Test
//  void testSuccessfulInsert() {
//    User result = usersDao.insert(USER);
//
//    verify(table, times(1)).putItem(any(Item.class), any(Expected.class));
//    assertEquals(USER, result);
//  }
//
//  @Test
//  @SuppressWarnings("unchecked")
//  void testConflictingInsert() {
//    when(table.putItem(any(), any())).thenThrow(ConditionalCheckFailedException.class);
//
//    DatabaseException e = assertThrows(DatabaseException.class,
//        () -> usersDao.insert(USER));
//
//    assertEquals(DatabaseError.CONFLICT, e.getErrorKind());
//    verify(table, times(1)).putItem(any(Item.class), any(Expected.class));
//  }
//
//  @Test
//  @SuppressWarnings("unchecked")
//  void testInsertWithUnsupportedData() {
//    when(table.putItem(any(), any())).thenThrow(AmazonServiceException.class);
//
//    DatabaseException e = assertThrows(DatabaseException.class,
//        () -> usersDao.insert(USER));
//
//    assertEquals(DatabaseError.REQUEST_REJECTED, e.getErrorKind());
//    verify(table, times(1)).putItem(any(Item.class), any(Expected.class));
//  }
//
//  @Test
//  @SuppressWarnings("unchecked")
//  void testInsertWithDatabaseDown() {
//    when(table.putItem(any(), any())).thenThrow(AmazonClientException.class);
//
//    DatabaseException e = assertThrows(DatabaseException.class,
//        () -> usersDao.insert(USER));
//
//    assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
//    verify(table, times(1)).putItem(any(Item.class), any(Expected.class));
//  }
//
//  /* findByEmail() */
//
//  @Test
//  void testSuccessfulFindByEmail() {
//    when(table.getItem(eq("email"), eq("test@test.com"))).thenReturn(ITEM);
//
//    User result = usersDao.findByEmail("test@test.com");
//
//    verify(table, times(1)).getItem(eq("email"), eq("test@test.com"));
//    assertEquals(USER, result);
//  }
//
//  @Test
//  void testUnsuccessfulFindByEmail() {
//    when(table.getItem(eq("email"), eq("test@test.com"))).thenReturn(null);
//
//    DatabaseException e = assertThrows(DatabaseException.class,
//        () -> usersDao.findByEmail("test@test.com"));
//
//    assertEquals(DatabaseError.USER_NOT_FOUND, e.getErrorKind());
//    verify(table, times(1)).getItem(eq("email"), eq("test@test.com"));
//  }
//
//  @Test
//  @SuppressWarnings("unchecked")
//  void testFindByEmailDatabaseDown() {
//    when(table.getItem(eq("email"), eq("test@test.com"))).thenThrow(AmazonClientException.class);
//
//    DatabaseException e = assertThrows(DatabaseException.class,
//        () -> usersDao.findByEmail("test@test.com"));
//
//    assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
//    verify(table, times(1)).getItem(eq("email"), eq("test@test.com"));
//  }
//
//  /* update() */
//
//  @Test
//  void testSuccessfulUpdate() {
//    when(table.getItem(eq("email"), eq("test@test.com"))).thenReturn(ITEM);
//
//    User result = usersDao.update(null, USER);
//
//    verify(table, times(1)).getItem(eq("email"), eq("test@test.com"));
//    verify(table, times(1)).putItem(any(Item.class), any(Expected.class));
//    assertEquals(USER, result);
//  }
//
//  @Test
//  void testSuccessfulEmailUpdate() {
//    when(table.getItem(eq("email"), eq("existingEmail"))).thenReturn(ITEM);
//    when(table.getItem(eq("email"), eq("test@test.com"))).thenReturn(null);
//
//    User result = usersDao.update("existingEmail", USER);
//
//    assertEquals(USER, result);
//
//    verify(table, times(1)).getItem(eq("email"), eq("existingEmail"));
//    verify(table, times(1)).deleteItem(any(DeleteItemSpec.class));
//    verify(table, times(1)).putItem(any(Item.class), any(Expected.class));
//  }
//
//  @Test
//  void testSameExistingEmail() {
//    when(table.getItem(eq("email"), eq("test@test.com"))).thenReturn(ITEM);
//
//    User result = usersDao.update("test@test.com", USER);
//
//    assertEquals(USER, result);
//
//    verify(table, times(1)).getItem(eq("email"), eq("test@test.com"));
//    verify(table, times(1)).putItem(any(Item.class), any(Expected.class));
//    verify(table, never()).deleteItem(any(DeleteItemSpec.class));
//  }
//
//  @Test
//  void testExistingUserWithNewEmail() {
//    when(table.getItem(eq("email"), eq("test@test.com"))).thenReturn(ITEM);
//
//    DatabaseException e = assertThrows(DatabaseException.class,
//        () -> usersDao.update("originalemail@gmail.com", USER));
//
//    assertEquals(DatabaseError.CONFLICT, e.getErrorKind());
//    verify(table, times(1)).getItem(eq("email"), eq("test@test.com"));
//  }
//
//  @Test
//  void testExistingUserWithNewEmailDatabaseDown() {
//    when(table.getItem(eq("email"), eq("test@test.com"))).thenThrow(AmazonClientException.class);
//
//    DatabaseException e = assertThrows(DatabaseException.class,
//        () -> usersDao.update("originalemail@gmail.com", USER));
//
//    assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
//    verify(table, times(1)).getItem(eq("email"), eq("test@test.com"));
//  }
//
//  @Test
//  void testUpdateGetNotFound() {
//    when(table.getItem(eq("email"), eq("test@test.com"))).thenReturn(null);
//
//    DatabaseException e = assertThrows(DatabaseException.class,
//        () -> usersDao.update(null, USER));
//
//    assertEquals(DatabaseError.USER_NOT_FOUND, e.getErrorKind());
//    verify(table, times(1)).getItem(eq("email"), eq("test@test.com"));
//  }
//
//  @Test
//  @SuppressWarnings("unchecked")
//  void testUpdateGetDatabaseDown() {
//    when(table.getItem(eq("email"), eq("test@test.com"))).thenThrow(AmazonClientException.class);
//
//    DatabaseException e = assertThrows(DatabaseException.class,
//        () -> usersDao.update(null, USER));
//
//    assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
//    verify(table, times(1)).getItem(eq("email"), eq("test@test.com"));
//  }
//
//  @Test
//  @SuppressWarnings("unchecked")
//  void testUpdatePutConflict() {
//    when(table.getItem(eq("email"), eq("test@test.com"))).thenReturn(ITEM);
//    when(table.putItem(any(), any())).thenThrow(ConditionalCheckFailedException.class);
//
//    DatabaseException e = assertThrows(DatabaseException.class,
//        () -> usersDao.update(null, USER));
//
//    assertEquals(DatabaseError.CONFLICT, e.getErrorKind());
//    verify(table, times(1)).getItem(eq("email"), eq("test@test.com"));
//    verify(table, times(1)).putItem(any(Item.class), any(Expected.class));
//  }
//
//  @Test
//  @SuppressWarnings("unchecked")
//  void testUpdatePutUnsupportedData() {
//    when(table.getItem(eq("email"), eq("test@test.com"))).thenReturn(ITEM);
//    when(table.putItem(any(), any())).thenThrow(AmazonServiceException.class);
//
//    DatabaseException e = assertThrows(DatabaseException.class,
//        () -> usersDao.update(null, USER));
//
//    assertEquals(DatabaseError.REQUEST_REJECTED, e.getErrorKind());
//    verify(table, times(1)).getItem(eq("email"), eq("test@test.com"));
//    verify(table, times(1)).putItem(any(Item.class), any(Expected.class));
//  }
//
//  @Test
//  @SuppressWarnings("unchecked")
//  void testUpdatePutDatabaseDown() {
//    when(table.getItem(eq("email"), eq("test@test.com"))).thenReturn(ITEM);
//    when(table.putItem(any(), any())).thenThrow(AmazonClientException.class);
//
//    DatabaseException e = assertThrows(DatabaseException.class,
//        () -> usersDao.update(null, USER));
//
//    assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
//    verify(table, times(1)).getItem(eq("email"), eq("test@test.com"));
//    verify(table, times(1)).putItem(any(Item.class), any(Expected.class));
//  }
//
//  /* delete() */
//
//  @Test
//  void testSuccessfulDelete() {
//    when(table.getItem(eq("email"), eq("test@test.com"))).thenReturn(ITEM);
//
//    User result = usersDao.delete("test@test.com");
//
//    verify(table, times(1)).getItem(eq("email"), eq("test@test.com"));
//    verify(table, times(1)).deleteItem(any(DeleteItemSpec.class));
//    assertEquals(USER, result);
//  }
//
//  @Test
//  @SuppressWarnings("unchecked")
//  void testUnsuccessfulDeleteGetFailure() {
//    when(table.getItem(eq("email"), eq("test@test.com"))).thenThrow(AmazonClientException.class);
//
//    DatabaseException e = assertThrows(DatabaseException.class,
//        () -> usersDao.delete("test@test.com"));
//
//    assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
//    verify(table, times(1)).getItem(eq("email"), eq("test@test.com"));
//  }
//
//  @Test
//  @SuppressWarnings("unchecked")
//  void testUnsuccessfulDeleteNotFound() {
//    when(table.getItem(eq("email"), eq("test@test.com"))).thenReturn(ITEM);
//    when(table.deleteItem(any(DeleteItemSpec.class)))
//        .thenThrow(ConditionalCheckFailedException.class);
//
//    DatabaseException e = assertThrows(DatabaseException.class,
//        () -> usersDao.delete("test@test.com"));
//
//    assertEquals(DatabaseError.USER_NOT_FOUND, e.getErrorKind());
//    verify(table, times(1)).getItem(eq("email"), eq("test@test.com"));
//    verify(table, times(1)).deleteItem(any(DeleteItemSpec.class));
//  }
//
//  @Test
//  @SuppressWarnings("unchecked")
//  void testUnsuccessfulDeleteDatabaseDown() {
//    when(table.getItem(eq("email"), eq("test@test.com"))).thenReturn(ITEM);
//    when(table.deleteItem(any(DeleteItemSpec.class)))
//        .thenThrow(AmazonClientException.class);
//
//    DatabaseException e = assertThrows(DatabaseException.class,
//        () -> usersDao.delete("test@test.com"));
//
//    assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
//    verify(table, times(1)).getItem(eq("email"), eq("test@test.com"));
//    verify(table, times(1)).deleteItem(any(DeleteItemSpec.class));
//  }
//}
