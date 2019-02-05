package com.sanctionco.thunder.dao;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.sanctionco.thunder.dao.dynamodb.DynamoDbUsersDao;

import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Provides object dependencies, including the application's database access object.
 *
 * @see com.sanctionco.thunder.ThunderComponent ThunderComponent
 */
@Module
public class DaoModule {

  @Singleton
  @Provides
  UsersDao provideUsersDao(DynamoDbClient dynamoDbClient,
                           @Named("tableName") String tableName,
                           ObjectMapper mapper) {
    return new DynamoDbUsersDao(dynamoDbClient, tableName, mapper);
  }
}
