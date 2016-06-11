package com.sanction.thunder.dao;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Expected;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.sanction.thunder.models.PilotUser;
import io.dropwizard.jackson.Jackson;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PilotUsersDaoTest {
  private final DynamoDB dynamo = mock(DynamoDB.class);
  private final Table table = mock(Table.class);
  private final Item item = mock(Item.class);
  private final ObjectMapper mapper = Jackson.newObjectMapper();

  private final PilotUser user = new PilotUser(
      "username", "password", "facebookAccessToken", "twitterAccessToken", "twitterAccessSecret");

  private PilotUsersDao usersDao;

  @Before
  public void setup() {
    when(dynamo.getTable(anyString())).thenReturn(table);

    when(item.getJSON(anyString())).thenReturn(toJson(mapper, user));
    when(item.getString(anyString())).thenReturn("example");

    when(item.withString(anyString(), anyString())).thenReturn(item);
    when(item.withLong(anyString(), anyLong())).thenReturn(item);
    when(item.withJSON(anyString(), anyString())).thenReturn(item);

    usersDao = new PilotUsersDao(dynamo, mapper);
  }

  @Test
  public void testSuccessfulInsert() {
    PilotUser result = usersDao.insert(user);

    verify(table, times(1)).putItem(any(Item.class), any(Expected.class));
    assertEquals(user, result);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testConflictingInsert() {
    when(table.putItem(any(), any())).thenThrow(ConditionalCheckFailedException.class);

    try {
      usersDao.insert(user);
    } catch (DatabaseException e) {
      assertEquals(DatabaseError.CONFLICT, e.getErrorKind());
      verify(table, times(1)).putItem(any(Item.class), any(Expected.class));

      return;
    }

    fail();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testInsertWithDatabaseDown() {
    when(table.putItem(any(), any())).thenThrow(AmazonClientException.class);

    try {
      usersDao.insert(user);
    } catch (DatabaseException e) {
      assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
      verify(table, times(1)).putItem(any(Item.class), any(Expected.class));

      return;
    }

    fail();
  }

  @Test
  public void testSuccessfulFindByUsername() {
    when(table.getItem(anyString(), anyString())).thenReturn(item);

    PilotUser result = usersDao.findByUsername("username");

    verify(table, times(1)).getItem(anyString(), anyString());
    assertEquals(user, result);
  }

  @Test
  public void testUnsuccessfulFindByUsername() {
    when(table.getItem(anyString(), anyString())).thenReturn(null);

    try {
      usersDao.findByUsername("username");
    } catch (DatabaseException e) {
      assertEquals(DatabaseError.USER_NOT_FOUND, e.getErrorKind());
      verify(table, times(1)).getItem(anyString(), anyString());

      return;
    }

    fail();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testFindByUsernameDatabaseDown() {
    when(table.getItem(anyString(), anyString())).thenThrow(AmazonClientException.class);

    try {
      usersDao.findByUsername("username");
    } catch (DatabaseException e) {
      assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
      verify(table, times(1)).getItem(anyString(), anyString());

      return;
    }

    fail();
  }

  @Test
  public void testSuccessfulUpdate() {
    when(table.getItem(anyString(), anyString())).thenReturn(item);

    PilotUser result = usersDao.update(user);

    verify(table, times(1)).getItem(anyString(), anyString());
    verify(table, times(1)).putItem(any(Item.class), any(Expected.class));
    assertEquals(user, result);
  }

  @Test
  public void testUpdateGetNotFound() {
    when(table.getItem(anyString(), anyString())).thenReturn(null);

    try {
      usersDao.update(user);
    } catch (DatabaseException e) {
      assertEquals(DatabaseError.USER_NOT_FOUND, e.getErrorKind());
      verify(table, times(1)).getItem(anyString(), anyString());

      return;
    }

    fail();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateGetDatabaseDown() {
    when(table.getItem(anyString(), anyString())).thenThrow(AmazonClientException.class);

    try {
      usersDao.update(user);
    } catch (DatabaseException e) {
      assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
      verify(table, times(1)).getItem(anyString(), anyString());

      return;
    }

    fail();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdatePutConflict() {
    when(table.getItem(anyString(), anyString())).thenReturn(item);
    when(table.putItem(any(), any())).thenThrow(ConditionalCheckFailedException.class);

    try {
      usersDao.update(user);
    } catch (DatabaseException e) {
      assertEquals(DatabaseError.CONFLICT, e.getErrorKind());
      verify(table, times(1)).getItem(anyString(), anyString());
      verify(table, times(1)).putItem(any(Item.class), any(Expected.class));

      return;
    }

    fail();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdatePutDatabaseDown() {
    when(table.getItem(anyString(), anyString())).thenReturn(item);
    when(table.putItem(any(), any())).thenThrow(AmazonClientException.class);

    try {
      usersDao.update(user);
    } catch (DatabaseException e) {
      assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
      verify(table, times(1)).getItem(anyString(), anyString());
      verify(table, times(1)).putItem(any(Item.class), any(Expected.class));

      return;
    }

    fail();
  }

  @Test
  public void testSuccessfulDelete() {
    when(table.getItem(anyString(), anyString())).thenReturn(item);

    PilotUser result = usersDao.delete("username");

    verify(table, times(1)).getItem(anyString(), anyString());
    verify(table, times(1)).deleteItem(any(DeleteItemSpec.class));
    assertEquals(user, result);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUnsuccessfulDeleteGetFailure() {
    when(table.getItem(anyString(), anyString())).thenThrow(AmazonClientException.class);

    try {
      usersDao.delete("username");
    } catch (DatabaseException e) {
      assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
      verify(table, times(1)).getItem(anyString(), anyString());

      return;
    }

    fail();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUnsuccessfulDeleteNotFound() {
    when(table.getItem(anyString(), anyString())).thenReturn(item);
    when(table.deleteItem(any(DeleteItemSpec.class)))
        .thenThrow(ConditionalCheckFailedException.class);

    try {
      usersDao.delete("username");
    } catch (DatabaseException e) {
      assertEquals(DatabaseError.USER_NOT_FOUND, e.getErrorKind());
      verify(table, times(1)).getItem(anyString(), anyString());
      verify(table, times(1)).deleteItem(any(DeleteItemSpec.class));

      return;
    }

    fail();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUnsuccessfulDeleteDatabaseDown() {
    when(table.getItem(anyString(), anyString())).thenReturn(item);
    when(table.deleteItem(any(DeleteItemSpec.class)))
        .thenThrow(AmazonClientException.class);

    try {
      usersDao.delete("username");
    } catch (DatabaseException e) {
      assertEquals(DatabaseError.DATABASE_DOWN, e.getErrorKind());
      verify(table, times(1)).getItem(anyString(), anyString());
      verify(table, times(1)).deleteItem(any(DeleteItemSpec.class));

      return;
    }

    fail();
  }

  private static String toJson(ObjectMapper mapper, PilotUser object) {
    try {
      return mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw Throwables.propagate(e);
    }
  }
}
