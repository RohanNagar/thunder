package com.sanctionco.thunder.authentication;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.dropwizard.core.setup.Environment;
import io.dropwizard.jackson.Discoverable;

/**
 * Provides the base interface for {@code AuthConfiguration} objects. Implementing classes
 * should be various types of HTTP authentication. Provides a method to set up authentication
 * with a Dropwizard environment.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface AuthConfiguration extends Discoverable {

  /**
   * Registers all necessary authentication objects with the Dropwizard environment.
   *
   * @param environment the Dropwizard environment to set up
   */
  void registerAuthentication(Environment environment);
}
