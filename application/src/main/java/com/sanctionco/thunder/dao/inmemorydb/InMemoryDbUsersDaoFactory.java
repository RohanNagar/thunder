package com.sanctionco.thunder.dao.inmemorydb;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanctionco.thunder.dao.DatabaseHealthCheck;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.dao.UsersDaoFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the in-memory implementation for the {@link UsersDaoFactory}. Provides methods
 * to construct new UsersDao and DatabaseHealthCheck objects that interact with an in-memory DB.
 *
 * <p>The application configuration file should use {@code type: memory} in order to use this
 * factory.
 *
 * <p>This class must be registered in
 * {@code /resources/META-INF/services/com.sanctionco.thunder.dao.UsersDaoFactory}.
 *
 * @see UsersDaoFactory
 */
@JsonTypeName("memory")
public class InMemoryDbUsersDaoFactory implements UsersDaoFactory {
  private static final Logger LOG = LoggerFactory.getLogger(InMemoryDbUsersDaoFactory.class);

  @Override
  public UsersDao createUsersDao(ObjectMapper mapper) {
    LOG.warn("CAUTION! Creating in-memory implementation of UsersDao. This configuration"
        + " should NOT be used in a production environment!");

    return new InMemoryDbUsersDao();
  }

  @Override
  public DatabaseHealthCheck createHealthCheck() {
    LOG.warn("CAUTION! Creating in-memory implementation of DatabaseHealthCheck. This configuration"
        + " should NOT be used in a production environment!");

    return new InMemoryDbHealthCheck();
  }
}
