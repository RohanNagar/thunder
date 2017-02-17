package com.sanction.thunder.dynamodb;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
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
  DynamoDB provideDynamoDb() {
    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
        .withRegion(Regions.US_EAST_1)
        .build();

    return new DynamoDB(client);
  }

  @Singleton
  @Provides
  Table provideTable(DynamoDB dynamo) {
    String tableName = config.getDynamoTableName();
    checkNotNull(tableName);

    return dynamo.getTable(tableName);
  }
}
