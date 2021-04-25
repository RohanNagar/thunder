package com.sanctionco.thunder.authentication.basic;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.security.Principal;

/**
 * Represents a basic authentication principal used to authenticate requests to the API, providing
 * access to the name and secret. This object should be used as the Dropwizard {@code @Auth}
 * parameter to protected methods on a resource. See
 * <a href=https://www.dropwizard.io/1.3.5/docs/manual/auth.html>the Dropwizard manual</a>
 * for more information on Dropwizard authentication.
 *
 * @param name the name of the application that owns the key
 * @param secret the secret token associated with the named application
 * @see BasicAuthenticator
 */
public record Key(@JsonProperty("application") String name,
                  @JsonProperty("secret") String secret) implements Principal {

  @Override
  public String getName() {
    return name;
  }
}
