package com.sanctionco.thunder.dao;

import com.fasterxml.jackson.databind.ObjectMapper;

import dagger.Module;
import dagger.Provides;

import java.util.Objects;

import javax.inject.Singleton;

/**
 * Provides object dependencies, including the application's database access object.
 *
 * @see com.sanctionco.thunder.ThunderComponent ThunderComponent
 */
@Module
public class DaoModule {
  private final UsersDaoFactory usersDaoFactory;

  /**
   * Constructs a new {@code DaoModule} object.
   *
   * @param usersDaoFactory the factory used to create the {@code UsersDao} and related instances
   */
  public DaoModule(UsersDaoFactory usersDaoFactory) {
    this.usersDaoFactory = Objects.requireNonNull(usersDaoFactory);
  }

  @Singleton
  @Provides
  UsersDao provideUsersDao(ObjectMapper mapper) {
    return usersDaoFactory.createUsersDao(mapper);
  }

  @Singleton
  @Provides
  DatabaseHealthCheck provideHealthCheck() {
    return usersDaoFactory.createHealthCheck();
  }
}
