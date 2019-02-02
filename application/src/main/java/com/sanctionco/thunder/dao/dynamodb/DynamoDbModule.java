package com.sanctionco.thunder.dao.dynamodb;

import dagger.Module;
import dagger.Provides;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;
import java.util.Objects;
import javax.inject.Named;
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
   * Constructs a new {@code DynamoDbModule} object from the configuration.
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
  DynamoDbClient provideDynamoDbClient() {
    return DynamoDbClient.builder()
        .region(Region.of(region))
        .endpointOverride(URI.create(endpoint))
        .build();
  }

  @Singleton
  @Provides
  @Named("tableName")
  String provideDynamoDbTableName() {
    return tableName;
  }
}
