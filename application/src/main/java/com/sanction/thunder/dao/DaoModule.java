package com.sanction.thunder.dao;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class DaoModule {

  @Singleton
  @Provides
  StormUsersDao provideStormUsersDao(DynamoDB dynamo, ObjectMapper mapper) {
    return new StormUsersDao(dynamo, mapper);
  }

}
