package com.sanction.thunder.dao;

import com.amazonaws.services.dynamodbv2.document.Table;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.sanction.thunder.dao.dynamodb.DynamoDbUsersDao;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * A Dagger module that provides dependencies related to the general DAO.
 *
 * @see UsersDao
 */
@Module
public class DaoModule {

  @Singleton
  @Provides
  UsersDao provideUsersDao(Table table, ObjectMapper mapper) {
    return new DynamoDbUsersDao(table, mapper);
  }
}
