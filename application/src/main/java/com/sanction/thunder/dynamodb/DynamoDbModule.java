package com.sanction.thunder.dynamodb;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;

import dagger.Module;
import dagger.Provides;

import java.util.Objects;
import javax.inject.Singleton;

@Module
public class DynamoDbModule {
  private final String endpoint;
  private final String region;
  private final String tableName;

  /**
   * Constructs a new DynamoDbModule object.
   *
   * @param dynamoConfiguration The configuration to get DynamoDB information from
   */
  public DynamoDbModule(DynamoDbConfiguration dynamoConfiguration) {
    this.endpoint = dynamoConfiguration.getEndpoint();
    this.region = dynamoConfiguration.getRegion();
    this.tableName = dynamoConfiguration.getTableName();
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
    Objects.requireNonNull(tableName);

    return dynamo.getTable(tableName);
  }
}
