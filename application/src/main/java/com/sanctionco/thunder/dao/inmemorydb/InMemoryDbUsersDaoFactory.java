package com.sanctionco.thunder.dao.inmemorydb;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanctionco.thunder.dao.DatabaseHealthCheck;
import com.sanctionco.thunder.dao.UsersDao;
import com.sanctionco.thunder.dao.UsersDaoFactory;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

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

  @Min(1) @Max(100) @JsonProperty("maxMemoryPercentage")
  private final Integer maxMemoryPercentage = 75;

  public Integer getMaxMemoryPercentage() {
    return maxMemoryPercentage;
  }

  @Override
  public UsersDao createUsersDao(ObjectMapper mapper) {
    LOG.warn("CAUTION! Creating in-memory implementation of UsersDao. This configuration"
        + " should NOT be used in a production environment!");
    LOG.info("In-memory database will use up to {}% of available JVM memory.", maxMemoryPercentage);

    var runtime = Runtime.getRuntime();

    return new InMemoryDbUsersDao(new MemoryInfo() {
      @Override
      public long maxMemory() {
        return runtime.maxMemory();
      }

      @Override
      public long freeMemory() {
        return runtime.freeMemory();
      }
    }, maxMemoryPercentage);
  }

  @Override
  public DatabaseHealthCheck createHealthCheck() {
    LOG.warn("CAUTION! Creating in-memory implementation of DatabaseHealthCheck. This configuration"
        + " should NOT be used in a production environment!");

    return new InMemoryDbHealthCheck();
  }
}
