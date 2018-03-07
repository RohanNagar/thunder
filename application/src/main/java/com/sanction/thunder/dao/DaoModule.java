package com.sanction.thunder.dao;

import com.amazonaws.services.dynamodbv2.document.Table;
import com.fasterxml.jackson.databind.ObjectMapper;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class DaoModule {

  @Singleton
  @Provides
  UsersDao provideUsersDao(Table table, ObjectMapper mapper) {
    return new UsersDao(table, mapper);
  }
}
