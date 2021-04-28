package com.sanctionco.thunder.dao.mongodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Updates;
import com.sanctionco.thunder.dao.DatabaseError;
import com.sanctionco.thunder.dao.DatabaseException;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.models.User;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mongodb.client.model.Filters.eq;

/**
 * Provides the MongoDB implementation for the {@link UsersDao}. Provides methods to
 * insert, update, get, and delete a {@code User} (in the {@code api} module) in the database.
 *
 * @see UsersDao
 */
public class MongoDbUsersDao implements UsersDao {
  private static final Logger LOG = LoggerFactory.getLogger(MongoDbUsersDao.class);

  private final MongoCollection<Document> mongoCollection;
  private final ObjectMapper mapper;

  /**
   * Constructs a new {@code MongoDbUsersDao} object with the given mongoCollection and mapper.
   *
   * @param mongoCollection the MongoCollection instance to perform operations on
   * @param mapper the mapper used to serialize and deserialize JSON
   */
  @Inject
  public MongoDbUsersDao(MongoCollection<Document> mongoCollection, ObjectMapper mapper) {
    this.mongoCollection = Objects.requireNonNull(mongoCollection);
    this.mapper = Objects.requireNonNull(mapper);
  }

  @Override
  public CompletableFuture<User> insert(User user) {
    Objects.requireNonNull(user);

    long now = Instant.now().toEpochMilli();

    Document doc = new Document("_id", user.getEmail().getAddress()) // _id is the primary key
        .append("id", UUID.randomUUID().toString())
        .append("version", UUID.randomUUID().toString())
        .append("creation_time", now)
        .append("update_time", now)
        .append("document", UsersDao.toJson(mapper, user));

    return CompletableFuture.supplyAsync(() -> mongoCollection.insertOne(doc))
        .thenApply(result -> user.withTime(now, now))
        .exceptionally(throwable -> {
          throw convertToDatabaseException(throwable.getCause(), user.getEmail().getAddress());
        });
  }

  @Override
  public CompletableFuture<User> findByEmail(String email, boolean unused) {
    Objects.requireNonNull(email);

    return CompletableFuture.supplyAsync(() -> mongoCollection.find(eq("_id", email)))
        .thenApply(MongoIterable::first)
        .thenApply(doc -> {
          if (doc == null) {
            LOG.warn("The email {} was not found in the database.", email);
            throw new DatabaseException("The user was not found.", DatabaseError.USER_NOT_FOUND);
          }

          return UsersDao.fromJson(mapper, doc.getString("document")).withTime(
              doc.getLong("creation_time"),
              doc.getLong("update_time"));
        }).exceptionally(throwable -> {
          throw convertToDatabaseException(throwable.getCause(), email);
        });
  }

  @Override
  public CompletableFuture<User> update(@Nullable String existingEmail, User user) {
    Objects.requireNonNull(user);

    // Different email (primary key) means we need to delete and insert
    if (existingEmail != null && !existingEmail.equals(user.getEmail().getAddress())) {
      LOG.info("User to update has new email. The user will be deleted and then reinserted.");
      return updateEmail(existingEmail, user);
    }

    long now = Instant.now().toEpochMilli();

    return CompletableFuture
        .supplyAsync(() -> mongoCollection.find(eq("_id", user.getEmail().getAddress())))
        .thenApply(MongoIterable::first)
        .thenApply(existingUser -> {
          if (existingUser == null) {
            LOG.warn("The user {} was not found in the database.", user.getEmail().getAddress());
            throw new DatabaseException("The user was not found.", DatabaseError.USER_NOT_FOUND);
          }

          // Compute the new data
          String newVersion = UUID.randomUUID().toString();
          String document = UsersDao.toJson(mapper, user);

          mongoCollection.updateOne(
              eq("version", existingUser.getString("version")),
              Updates.combine(
                  Updates.set("version", newVersion),
                  Updates.set("update_time", now),
                  Updates.set("document", document)));

          return existingUser.getLong("creation_time");
        })
        .thenApply(creationTime -> user.withTime(creationTime, now))
        .exceptionally(throwable -> {
          throw convertToDatabaseException(throwable.getCause(), user.getEmail().getAddress());
        });
  }

  @Override
  public CompletableFuture<User> delete(String email) {
    Objects.requireNonNull(email);

    return findByEmail(email, true)
        .thenCombine(CompletableFuture.supplyAsync(
            () -> mongoCollection.deleteOne(eq("_id", email))),
            (user, result) -> user)
        .exceptionally(throwable -> {
          throw convertToDatabaseException(throwable.getCause(), email);
        });
  }

  /**
   * Converts a throwable received from MongoDB into a {@link DatabaseException}.
   *
   * @param throwable the throwable to convert
   * @param email the email address that was operated on
   * @return a new {@link DatabaseException}
   */
  private DatabaseException convertToDatabaseException(Throwable throwable, String email) {
    if (throwable instanceof DatabaseException e) {
      return e;
    }

    if (throwable instanceof MongoWriteException e) {
      switch (e.getError().getCategory()) {
        case DUPLICATE_KEY -> {
          LOG.error("The user {} already exists in the database.", email, e);
          throw new DatabaseException("The user already exists.", DatabaseError.CONFLICT);
        }
        case EXECUTION_TIMEOUT -> {
          LOG.error("The operation for user {} timed out.", email, e);
          throw new DatabaseException("The operation timed out.", DatabaseError.DATABASE_DOWN);
        }
        default -> {
          LOG.error("The operation for {} was rejected for an unknown reason.", email, e);
          throw new DatabaseException("The insert request was rejected for an unknown reason.",
              DatabaseError.REQUEST_REJECTED);
        }
      }
    }

    if (throwable instanceof MongoCommandException e) {
      LOG.error("There was a MongoDB exception (code: {}, reason: {}) "
              + "when performing operation on user {}.",
          e.getErrorCode(), e.getErrorMessage(), email, e);
      return new DatabaseException("The request was rejected for the following reason: "
          + e.getErrorMessage(), DatabaseError.REQUEST_REJECTED);
    }

    if (throwable instanceof MongoTimeoutException) {
      LOG.error("The database is currently unresponsive (User {}).", email, throwable);
      return new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    LOG.error("Unknown database error (User {}).", email, throwable);
    return new DatabaseException("Unknown database error.", DatabaseError.DATABASE_DOWN);
  }
}
