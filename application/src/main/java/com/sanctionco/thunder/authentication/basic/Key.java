package com.sanctionco.thunder.authentication.basic;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.security.Principal;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Represents an authentication principal used to authenticate requests to the API, providing
 * access to the name and secret. This object should be used as the Dropwizard {@code @Auth}
 * parameter to protected methods on a resource. See
 * <a href=https://www.dropwizard.io/1.3.5/docs/manual/auth.html>the Dropwizard manual</a>
 * for more information on Dropwizard authentication.
 *
 * @see ThunderAuthenticator
 */
public class Key implements Principal {
  private final String name;
  private final String secret;

  /**
   * Constructs a new key with the given name and secret.
   *
   * @param name the name of the application that owns the key
   * @param secret the secret token associated with the named application
   */
  public Key(@JsonProperty("application") String name,
             @JsonProperty("secret") String secret) {
    this.name = Objects.requireNonNull(name);
    this.secret = Objects.requireNonNull(secret);
  }

  public String getName() {
    return name;
  }

  public String getSecret() {
    return secret;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof Key)) {
      return false;
    }

    Key other = (Key) obj;
    return Objects.equals(this.name, other.name)
        && Objects.equals(this.secret, other.secret);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, secret);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", "Key [", "]")
        .add(String.format("name=%s", name))
        .add(String.format("secret=%s", secret))
        .toString();
  }
}
