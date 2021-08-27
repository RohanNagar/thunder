package com.sanctionco.thunder.dao.inmemorydb;

import com.sanctionco.thunder.dao.DatabaseException;
import com.sanctionco.thunder.models.Email;
import com.sanctionco.thunder.models.User;

import java.time.Instant;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import static com.sanctionco.thunder.dao.DatabaseTestUtil.assertDatabaseError;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InMemoryDbUsersDaoTest {
  private static final Email EMAIL = new Email("test@test.com", true, "testToken");
  private static final User USER = new User(EMAIL, "password",
      Collections.singletonMap("testProperty", "test"));
  private static final long CURR_TIME = Instant.now().toEpochMilli();

  private static final int MAX_MEMORY_PERCENTAGE = 75;
  private static final MemoryInfo MEMORY_INFO = new RuntimeMemoryInfo(Runtime.getRuntime());

  @Test
  void insert_ShouldSucceed() {
    var dao = new InMemoryDbUsersDao(MEMORY_INFO, MAX_MEMORY_PERCENTAGE);

    var result = dao.insert(USER).join();

    // The creation time and update time will have been created on insert
    long creationTime = (Long) result.getProperties().get("creationTime");
    long updateTime = (Long) result.getProperties().get("lastUpdateTime");

    assertTrue(creationTime > CURR_TIME);
    assertTrue(updateTime > CURR_TIME);

    assertEquals(creationTime, updateTime);

    assertEquals(USER.withTime(creationTime, updateTime), result);
  }

  @Test
  void insert_ConflictShouldFail() {
    var dao = new InMemoryDbUsersDao(MEMORY_INFO, MAX_MEMORY_PERCENTAGE);
    dao.insert(USER).join();

    assertDatabaseError(DatabaseException.Error.CONFLICT,
        () -> dao.insert(USER).join());
  }

  @Test
  void insert_OutOfMemoryShouldFail() {
    // Pretend that we have only 20% of memory remaining - so we've used 80%
    var mockMemoryInfo = mock(MemoryInfo.class);
    when(mockMemoryInfo.maxMemory()).thenReturn(100L);
    when(mockMemoryInfo.totalMemory()).thenReturn(100L);
    when(mockMemoryInfo.freeMemory()).thenReturn(20L);

    var dao = new InMemoryDbUsersDao(mockMemoryInfo, 75);

    // We should fail since we have used 80% and only allow 75%
    assertDatabaseError(DatabaseException.Error.DATABASE_DOWN,
        () -> dao.insert(USER).join());
  }

  @Test
  void findByEmail_ShouldSucceed() {
    var dao = new InMemoryDbUsersDao(MEMORY_INFO, MAX_MEMORY_PERCENTAGE);

    var inserted = dao.insert(USER).join();
    var result = dao.findByEmail("test@test.com").join();

    assertEquals(inserted, result);
  }

  @Test
  void findByEmail_NotFoundShouldFail() {
    var dao = new InMemoryDbUsersDao(MEMORY_INFO, MAX_MEMORY_PERCENTAGE);

    assertDatabaseError(DatabaseException.Error.USER_NOT_FOUND,
        () -> dao.findByEmail("test@test.com").join());
  }

  @Test
  void update_NewEmailShouldSucceed() {
    var dao = new InMemoryDbUsersDao(MEMORY_INFO, MAX_MEMORY_PERCENTAGE);
    var userToUpdate = new User(
        Email.unverified("test2@test.com"), "password", Collections.emptyMap());

    var inserted = dao.insert(USER).join();
    var updateResult = dao.update("test@test.com", userToUpdate).join();

    assertAll("Properties are correct",
        () -> assertNotEquals(inserted.getEmail(), updateResult.getEmail()),
        () -> assertEquals(inserted.getPassword(), updateResult.getPassword()),
        // Properties should only have creation and update time
        () -> assertEquals(2, updateResult.getProperties().size()));

    var getResult = dao.findByEmail("test2@test.com").join();

    assertEquals(updateResult, getResult);
  }

  @Test
  void update_ShouldSucceed() {
    var dao = new InMemoryDbUsersDao(MEMORY_INFO, MAX_MEMORY_PERCENTAGE);
    var userToUpdate = new User(EMAIL, "password", Collections.emptyMap());

    var inserted = dao.insert(USER).join();
    var updateResult = dao.update(null, userToUpdate).join();

    assertAll("Properties are correct",
        () -> assertEquals(inserted.getEmail(), updateResult.getEmail()),
        () -> assertEquals(inserted.getPassword(), updateResult.getPassword()),
        // Properties should only have creation and update time
        () -> assertEquals(2, updateResult.getProperties().size()));

    var getResult = dao.findByEmail("test@test.com").join();

    assertEquals(updateResult, getResult);
  }

  @Test
  void update_ShouldSucceedWithSameEmail() {
    var dao = new InMemoryDbUsersDao(MEMORY_INFO, MAX_MEMORY_PERCENTAGE);
    var userToUpdate = new User(EMAIL, "password", Collections.emptyMap());

    var inserted = dao.insert(USER).join();
    var updateResult = dao.update(EMAIL.getAddress(), userToUpdate).join();

    assertAll("Properties are correct",
        () -> assertEquals(inserted.getEmail(), updateResult.getEmail()),
        () -> assertEquals(inserted.getPassword(), updateResult.getPassword()),
        // Properties should only have creation and update time
        () -> assertEquals(2, updateResult.getProperties().size()));

    var getResult = dao.findByEmail("test@test.com").join();

    assertEquals(updateResult, getResult);
  }

  @Test
  void update_NotFoundShouldFail() {
    var dao = new InMemoryDbUsersDao(MEMORY_INFO, MAX_MEMORY_PERCENTAGE);

    assertDatabaseError(DatabaseException.Error.USER_NOT_FOUND,
        () -> dao.update(null, USER).join());
  }

  @Test
  void delete_ShouldSucceed() {
    var dao = new InMemoryDbUsersDao(MEMORY_INFO, MAX_MEMORY_PERCENTAGE);

    var inserted = dao.insert(USER).join();
    var deleted = dao.delete(EMAIL.getAddress()).join();

    assertEquals(inserted, deleted);

    assertDatabaseError(DatabaseException.Error.USER_NOT_FOUND,
        () -> dao.findByEmail(EMAIL.getAddress()).join());
  }
}
