package com.sanctionco.thunder.dao.mongodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoTimeoutException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import com.sanctionco.thunder.dao.DatabaseError;
import com.sanctionco.thunder.dao.DatabaseException;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.models.User;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

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
  public User insert(User user) {
    Objects.requireNonNull(user);

    long now = Instant.now().toEpochMilli();

    Document doc = new Document("_id", user.getEmail().getAddress()) // _id is the primary key
        .append("id", UUID.randomUUID().toString())
        .append("version", UUID.randomUUID().toString())
        .append("creation_time", String.valueOf(now))
        .append("update_time", String.valueOf(now))
        .append("document", UsersDao.toJson(mapper, user));

    try {
      mongoCollection.insertOne(doc);
    } catch (MongoWriteException e) {
      switch (e.getError().getCategory()) {
        case DUPLICATE_KEY:
          LOG.error("The user {} already exists in the database.", user.getEmail(), e);
          throw new DatabaseException("The user already exists.",
              DatabaseError.CONFLICT);

        case EXECUTION_TIMEOUT:
          LOG.error("The insert operation for user {} timed out.", user.getEmail(), e);
          throw new DatabaseException("The insert operation timed out.",
              DatabaseError.DATABASE_DOWN);

        case UNCATEGORIZED:
        default:
          LOG.error("The insert for {} was rejected for an unknown reason.", user.getEmail(), e);
          throw new DatabaseException("The insert request was rejected for an unknown reason.",
              DatabaseError.REQUEST_REJECTED);
      }
    } catch (MongoTimeoutException e) {
      LOG.error("The database is currently unresponsive.", e);
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    return user;
  }

  @Override
  public User findByEmail(String email) {
    Objects.requireNonNull(email);

    Document doc;
    try {
      doc = mongoCollection.find(eq("_id", email)).first();
    } catch (MongoTimeoutException e) {
      LOG.error("The database is currently unresponsive.", e);
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    if (doc == null) {
      LOG.warn("The email {} was not found in the database.", email);
      throw new DatabaseException("The user was not found.", DatabaseError.USER_NOT_FOUND);
    }

    return UsersDao.fromJson(mapper, doc.getString("document"));
  }

  @Override
  public User update(@Nullable String existingEmail, User user) {
    Objects.requireNonNull(user);

    // Different email (primary key) means we need to delete and insert
    if (existingEmail != null && !existingEmail.equals(user.getEmail().getAddress())) {
      LOG.info("User to update has new email. The user will be deleted and then reinserted.");
      return updateEmail(existingEmail, user);
    }

    // Get the old version
    Document existingUser;
    try {
      existingUser = mongoCollection.find(eq("_id", user.getEmail().getAddress())).first();
    } catch (MongoTimeoutException e) {
      LOG.error("The database is currently unresponsive.", e);
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    if (existingUser == null) {
      LOG.warn("The user {} was not found in the database.", user.getEmail().getAddress());
      throw new DatabaseException("The user was not found.", DatabaseError.USER_NOT_FOUND);
    }

    // Compute the new data
    long now = Instant.now().toEpochMilli();
    String newVersion = UUID.randomUUID().toString();
    String document = UsersDao.toJson(mapper, user);

    // Perform update, query based on old version and only update fields that should change
    try {
      mongoCollection.updateOne(
          eq("version", existingUser.getString("version")),
          Updates.combine(
              Updates.set("version", newVersion),
              Updates.set("update_time", String.valueOf(now)),
              Updates.set("document", document)));
    } catch (MongoWriteException e) {
      switch (e.getError().getCategory()) {
        case EXECUTION_TIMEOUT:
          LOG.error("The update operation for user {} timed out.", user.getEmail(), e);
          throw new DatabaseException("The update operation timed out.",
              DatabaseError.DATABASE_DOWN);

        case UNCATEGORIZED:
        default:
          LOG.error("The update for {} was rejected for an unknown reason.", user.getEmail(), e);
          throw new DatabaseException("The update request was rejected for an unknown reason.",
              DatabaseError.REQUEST_REJECTED);
      }
    } catch (MongoTimeoutException e) {
      LOG.error("The database is currently unresponsive.", e);
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    return user;
  }

  @Override
  public User delete(String email) {
    Objects.requireNonNull(email);

    // Get the item that will be deleted to return it
    User user = findByEmail(email);

    try {
      mongoCollection.deleteOne(eq("_id", email));
    } catch (MongoTimeoutException e) {
      LOG.error("The database is currently unresponsive.", e);
      throw new DatabaseException("The database is currently unavailable.",
          DatabaseError.DATABASE_DOWN);
    }

    return user;
  }
}
