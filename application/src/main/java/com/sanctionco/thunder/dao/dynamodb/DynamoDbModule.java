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
 * A Dagger module that provides dependencies related to DynamoDB.
 */
@Module
public class DynamoDbModule {
  private final String endpoint;
  private final String region;
  private final String tableName;

  /**
   * Constructs a new DynamoDbModule object from the provided configuration.
   *
   * @param dynamoConfiguration The configuration to get DynamoDB information from.
   *
   * @see DynamoDbConfiguration
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
