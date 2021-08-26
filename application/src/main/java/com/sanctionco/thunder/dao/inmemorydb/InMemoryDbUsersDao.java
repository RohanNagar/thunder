package com.sanctionco.thunder.dao.inmemorydb;

import com.sanctionco.thunder.dao.DatabaseException;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.models.User;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the in-memory implementation for {@link UsersDao}. Provides methods to
 * insert, update, get, and delete a {@code User} (in the {@code api} module) in the database.
 *
 * @see UsersDao
 */
public class InMemoryDbUsersDao implements UsersDao {
  private static final Logger LOG = LoggerFactory.getLogger(InMemoryDbUsersDao.class);

  private final ConcurrentMap<String, User> database = new ConcurrentHashMap<>();

  private final MemoryInfo currentMemory;
  private final int maxPercentageOfMemoryToUse;

  /**
   * Construct a new {@code InMemoryDbUsersDao}.
   *
   * @param memoryInfo the {@link MemoryInfo} instanced used to get information about JVM memory
   * @param maxPercentageOfMemoryToUse the maximum percentage of JVM memory that can be used
   *                                   before {@link #insert(User)} requests fail
   */
  public InMemoryDbUsersDao(MemoryInfo memoryInfo, int maxPercentageOfMemoryToUse) {
    this.currentMemory = memoryInfo;
    this.maxPercentageOfMemoryToUse = maxPercentageOfMemoryToUse;
  }

  @Override
  public CompletableFuture<User> insert(User user) {
    if (!memoryAvailable()) {
      return CompletableFuture.failedFuture(
          new DatabaseException("There is no more memory available in the in-memory database.",
              DatabaseException.Error.DATABASE_DOWN));
    }

    Objects.requireNonNull(user);

    var now = Instant.now().toEpochMilli();
    var userWithTime = user.withTime(now, now);

    return database.putIfAbsent(userWithTime.getEmail().getAddress(), userWithTime) == null
        ? CompletableFuture.completedFuture(userWithTime)
        : CompletableFuture.failedFuture(
            new DatabaseException("A user with the same email address already exists.",
                DatabaseException.Error.CONFLICT));
  }

  @Override
  public CompletableFuture<User> findByEmail(String email) {
    Objects.requireNonNull(email);

    return userOrNotFound(database.get(email));
  }

  @Override
  public CompletableFuture<User> update(@Nullable String existingEmail, User user) {
    Objects.requireNonNull(user);

    // Different email (primary key) means we need to delete and insert
    if (existingEmail != null && !existingEmail.equals(user.getEmail().getAddress())) {
      LOG.info("User to update has new email. The user will be deleted and then reinserted.");
      return updateEmail(existingEmail, user);
    }

    var now = Instant.now().toEpochMilli();

    return userOrNotFound(database.computeIfPresent(user.getEmail().getAddress(),
        (key, oldUser) -> user.withTime((long) oldUser.getProperties().get("creationTime"), now)));
  }

  @Override
  public CompletableFuture<User> delete(String email) {
    Objects.requireNonNull(email);

    return userOrNotFound(database.remove(email));
  }

  private CompletableFuture<User> userOrNotFound(User user) {
    return user != null ? CompletableFuture.completedFuture(user)
        : CompletableFuture.failedFuture(
            new DatabaseException("User not found in the database.",
                DatabaseException.Error.USER_NOT_FOUND));
  }

  /**
   * Determine if there is memory available in the JVM based on the allowed
   * {@code maxPercentageOfMemoryToUse}.
   *
   * @return true if there is memory available, false otherwise
   */
  private boolean memoryAvailable() {
    var maxMemory = currentMemory.maxMemory();
    var freeMemory = currentMemory.freeMemory();
    var percentageUsed = ((double) (maxMemory - freeMemory) / maxMemory) * 100;

    return percentageUsed < maxPercentageOfMemoryToUse;
  }
}
