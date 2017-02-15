package com.sanction.thunder.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.security.Principal;

import static com.google.common.base.Preconditions.checkNotNull;

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
    this.name = checkNotNull(name);
    this.secret = checkNotNull(secret);
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
    return Objects.equal(this.name, other.name)
        && Objects.equal(this.secret, other.secret);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.name, this.secret);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .toString();
  }
}
