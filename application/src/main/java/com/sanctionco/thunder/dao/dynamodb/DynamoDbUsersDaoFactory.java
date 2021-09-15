package com.sanctionco.thunder.dao.dynamodb;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanctionco.thunder.dao.DatabaseHealthCheck;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.dao.UsersDaoFactory;

import java.net.URI;
import java.util.Objects;
import javax.validation.constraints.NotEmpty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

/**
 * Provides the Amazon DynamoDB implementation for the {@link UsersDaoFactory}. Provides methods
 * to construct new UsersDao and DatabaseHealthCheck objects that interact with DynamoDB.
 *
 * <p>The application configuration file should use {@code type: dynamodb} in order to use this
 * factory.
 *
 * <p>This class must be registered in
 * {@code /resources/META-INF/services/com.sanctionco.thunder.dao.UsersDaoFactory}.
 *
 * @see UsersDaoFactory
 */
@JsonTypeName("dynamodb")
public class DynamoDbUsersDaoFactory implements UsersDaoFactory {
  private static final Logger LOG = LoggerFactory.getLogger(DynamoDbUsersDaoFactory.class);
  private static final Long READ_CAPACITY_UNITS = 5L;
  private static final Long WRITE_CAPACITY_UNITS = 5L;

  DynamoDbAsyncClient dynamoDbClient; // package-private for testing

  @NotEmpty @JsonProperty("endpoint")
  private final String endpoint = null;

  @NotEmpty @JsonProperty("region")
  private final String region = null;

  @NotEmpty @JsonProperty("tableName")
  private final String tableName = null;

  public String getEndpoint() {
    return endpoint;
  }

  public String getRegion() {
    return region;
  }

  public String getTableName() {
    return tableName;
  }

  /**
   * Constructs a new {@link DynamoDbUsersDao} instance.
   *
   * @param mapper the ObjectMapper instance to use
   * @return the created {@link DynamoDbUsersDao} instance
   */
  @Override
  @SuppressWarnings("ConstantConditions")
  public UsersDao createUsersDao(ObjectMapper mapper) {
    LOG.info("Creating DynamoDB implementation of UsersDao");

    initializeDynamoDbClient();
    initializeDynamoDbTable();

    return new DynamoDbUsersDao(dynamoDbClient, tableName, mapper);
  }

  /**
   * Constructs a new {@link DynamoDbHealthCheck} instance.
   *
   * @return the created {@link DynamoDbHealthCheck} instance
   */
  @Override
  public DatabaseHealthCheck createHealthCheck() {
    LOG.info("Creating DynamoDB implementation of DatabaseHealthCheck");

    initializeDynamoDbClient();

    return new DynamoDbHealthCheck(dynamoDbClient);
  }

  /**
   * Initializes the Amazon DynamoDB client that will be passed into the DAO and
   * HealthCheck instances.
   */
  @SuppressWarnings("ConstantConditions")
  private synchronized void initializeDynamoDbClient() {
    if (this.dynamoDbClient != null) {
      return;
    }

    Objects.requireNonNull(region);
    Objects.requireNonNull(endpoint);

    this.dynamoDbClient = DynamoDbAsyncClient.builder()
        .region(Region.of(region))
        .endpointOverride(URI.create(endpoint))
        .build();
  }

  /**
   * Ensures that the DynamoDB table exists. If not, it creates the table.
   */
  @SuppressWarnings("ConstantConditions")
  private void initializeDynamoDbTable() {
    if (!dynamoDbClient.listTables().join().tableNames().contains(tableName)) {
      LOG.warn("The DynamoDB table {} does not exist."
              + " Creating this table with {} read capacity units and {} write capacity units.",
          tableName, READ_CAPACITY_UNITS, WRITE_CAPACITY_UNITS);

      dynamoDbClient.createTable(CreateTableRequest.builder()
          .tableName(tableName)
          .attributeDefinitions(AttributeDefinition.builder()
              .attributeName("email")
              .attributeType(ScalarAttributeType.S)
              .build())
          .keySchema(KeySchemaElement.builder()
              .attributeName("email")
              .keyType(KeyType.HASH)
              .build())
          .provisionedThroughput(ProvisionedThroughput.builder()
              .readCapacityUnits(READ_CAPACITY_UNITS)
              .writeCapacityUnits(WRITE_CAPACITY_UNITS)
              .build())
          .build());
    }
  }
}
