package com.sanctionco.thunder.dao;

import com.amazonaws.services.dynamodbv2.document.Table;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.sanctionco.thunder.dao.dynamodb.DynamoDbUsersDao;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Provides object dependencies, including the application's database access object.
 *
 * @see com.sanctionco.thunder.ThunderComponent ThunderComponent
 */
@Module
public class DaoModule {

  @Singleton
  @Provides
  UsersDao provideUsersDao(Table table, ObjectMapper mapper) {
    return new DynamoDbUsersDao(table, mapper);
  }
}
