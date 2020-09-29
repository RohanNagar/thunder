package com.sanctionco.thunder.authentication;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.dropwizard.jackson.Discoverable;
import io.dropwizard.setup.Environment;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface AuthConfiguration extends Discoverable {

  void registerAuthentication(Environment environment);
}
