package com.sanction.thunder.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.security.Principal;
import java.util.Objects;
import java.util.StringJoiner;

public class Key implements Principal {
  private final String name;
  private final String secret;

  /**
   * Constructs a new Key given the specified parameters.
   *
   * @param name The name of the application that owns the key.
   * @param secret The secret token associated with the named application.
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
    return Objects.hash(this.name, this.secret);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", "Key [", "]")
        .add(String.format("name=%s", name))
        .add(String.format("secret=%s", secret))
        .toString();
  }
}
