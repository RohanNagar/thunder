package com.sanction.thunder.dao;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.sanction.thunder.ThunderConfiguration;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class DaoModule {
  private final ThunderConfiguration config;

  public DaoModule(ThunderConfiguration config) {
    this.config = config;
  }

  @Singleton
  @Provides
  AmazonDynamoDBClient provideDynamoClient() {
    String endpoint = config.getDynamoEndpoint();
    if (endpoint != null) {
      AmazonDynamoDBClient client = new AmazonDynamoDBClient();
      client.setEndpoint(endpoint);

      return client;
    }

    Region region = Regions.getCurrentRegion();
    return region.createClient(AmazonDynamoDBClient.class, null, null);
  }

  @Singleton
  @Provides
  DynamoDB provideDynamoDb(AmazonDynamoDBClient client) {
    return new DynamoDB(client);
  }

  @Singleton
  @Provides
  StormUsersDao provideStormUsersDao(DynamoDB dynamo) {
    return new StormUsersDao(dynamo);
  }

}
