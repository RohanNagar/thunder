package com.sanction.thunder.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.sanction.thunder.ThunderConfiguration;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

@Module
public class DynamoDbModule {
  private final ThunderConfiguration config;

  public DynamoDbModule(ThunderConfiguration config) {
    this.config = config;
  }

  @Singleton
  @Provides
  AmazonDynamoDBClient provideDynamoClient() {
    String endpoint = config.getDynamoEndpoint();
    checkNotNull(endpoint);

    AmazonDynamoDBClient client = new AmazonDynamoDBClient();
    client.setEndpoint(endpoint);

    return client;
  }

  @Singleton
  @Provides
  DynamoDB provideDynamoDb(AmazonDynamoDBClient client) {
    return new DynamoDB(client);
  }

  @Singleton
  @Provides
  Table provideTable(DynamoDB dynamo) {
    return dynamo.getTable(config.getDynamoTableName());
  }
}
