package com.sanctionco.thunder.dao.mongodb;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sanctionco.thunder.dao.DatabaseHealthCheck;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.dao.UsersDaoFactory;

import java.util.Objects;

import javax.validation.constraints.NotEmpty;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the MongoDB implementation for the {@link UsersDaoFactory}. Provides methods
 * to construct new UsersDao and DatabaseHealthCheck objects that interact with MongoDB.
 *
 * <p>The application configuration file should use {@code type: mongodb} in order to use this
 * factory.
 *
 * <p>This class must be registered in
 * {@code /resources/META-INF/services/com.sanctionco.thunder.dao.UsersDaoFactory}.
 *
 * @see UsersDaoFactory
 */
@JsonTypeName("mongodb")
public class MongoDbUsersDaoFactory implements UsersDaoFactory {
  private static final Logger LOG = LoggerFactory.getLogger(MongoDbUsersDaoFactory.class);

  private MongoClient mongoClient;

  @NotEmpty
  @JsonProperty("connectionString")
  private final String connectionString = null;

  public String getConnectionString() {
    return connectionString;
  }

  @NotEmpty
  @JsonProperty("databaseName")
  private final String databaseName = null;

  public String getDatabaseName() {
    return databaseName;
  }

  @NotEmpty
  @JsonProperty("collectionName")
  private final String collectionName = null;

  public String getCollectionName() {
    return collectionName;
  }

  /**
   * Constructs a new {@link MongoDbUsersDao} instance.
   *
   * @param mapper the ObjectMapper instance to use
   * @return the created {@link MongoDbUsersDao} instance
   */
  @Override
  @SuppressWarnings("ConstantConditions")
  public UsersDao createUsersDao(ObjectMapper mapper) {
    LOG.info("Creating MongoDB implementation of UsersDao");

    initializeMongoClient();

    Objects.requireNonNull(databaseName);
    Objects.requireNonNull(collectionName);

    MongoDatabase database = mongoClient.getDatabase(databaseName);
    MongoCollection<Document> collection = database.getCollection(collectionName);

    return new MongoDbUsersDao(collection, mapper);
  }

  /**
   * Constructs a new {@link MongoDbHealthCheck} instance.
   *
   * @return the created {@link MongoDbHealthCheck} instance
   */
  @Override
  public DatabaseHealthCheck createHealthCheck() {
    LOG.info("Creating MongoDB implementation of DatabaseHealthCheck");

    initializeMongoClient();

    return new MongoDbHealthCheck(mongoClient);
  }

  /**
   * Initializes the MongoDB client that will be passed into the DAO and
   * HealthCheck instances.
   */
  @SuppressWarnings("ConstantConditions")
  private void initializeMongoClient() {
    if (this.mongoClient != null) {
      return;
    }

    Objects.requireNonNull(connectionString);

    this.mongoClient = MongoClients.create(connectionString);
  }
}
