package com.sanctionco.thunder.dao.dynamodb;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;

import dagger.Module;
import dagger.Provides;

import java.util.Objects;
import javax.inject.Singleton;

/**
 * Provides object dependencies needed to connect to DynamoDB. This module is
 * eventually used by {@link com.sanctionco.thunder.dao.DaoModule DaoModule} in order
 * to construct a {@link DynamoDbUsersDao}.
 *
 * @see com.sanctionco.thunder.dao.DaoModule DaoModule
 */
@Module
public class DynamoDbModule {
  private final String endpoint;
  private final String region;
  private final String tableName;

  /**
   * Constructs a new DynamoDbModule object from the configuration.
   *
   * @param dynamoConfiguration the configuration containing DynamoDB information
   */
  public DynamoDbModule(DynamoDbConfiguration dynamoConfiguration) {
    Objects.requireNonNull(dynamoConfiguration);

    this.endpoint = Objects.requireNonNull(dynamoConfiguration.getEndpoint());
    this.region = Objects.requireNonNull(dynamoConfiguration.getRegion());
    this.tableName = Objects.requireNonNull(dynamoConfiguration.getTableName());
  }

  @Singleton
  @Provides
  DynamoDB provideDynamoDb() {
    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
        .build();

    return new DynamoDB(client);
  }

  @Singleton
  @Provides
  Table provideTable(DynamoDB dynamo) {
    return dynamo.getTable(tableName);
  }
}
