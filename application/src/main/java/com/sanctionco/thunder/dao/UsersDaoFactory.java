package com.sanctionco.thunder.dao;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.jackson.Discoverable;

/**
 * Provides the base interface for the {@code UsersDaoFactory} which allows for instance
 * creation of {@link UsersDao} objects and {@link DatabaseHealthCheck} objects.
 *
 * <p>This class is to be used within the Dropwizard configuration and provides polymorphic
 * configuration - which allows us to implement the {@code database} section of our configuration
 * with multiple configuration classes.
 *
 * <p>The {@code type} property on the configuration object is used to determine which implementing
 * class to construct.
 *
 * <p>This class must be registered in
 * {@code /resources/META-INF/services/io.dropwizard.jackson.Discoverable}.
 *
 * <p>See the {@code ThunderConfiguration} class for usage.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface UsersDaoFactory extends Discoverable {

  /**
   * Creates a new instance of {@code UsersDao}.
   *
   * @param mapper the ObjectMapper instance to use
   * @return the created UsersDao object
   */
  UsersDao createUsersDao(ObjectMapper mapper);

  /**
   * Creates a new instance of {@code DatabaseHealthCheck}.
   *
   * @return the created DatabaseHealthCheck object
   */
  DatabaseHealthCheck createHealthCheck();
}
