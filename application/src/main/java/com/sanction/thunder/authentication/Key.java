package com.sanction.thunder.authentication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class Key {

  private final String application;
  private final String secret;

  @JsonCreator
  public Key(@JsonProperty("application") String application,
             @JsonProperty("secret") String secret) {
    this.application = checkNotNull(application);
    this.secret = checkNotNull(secret);
  }

  public String getApplication() {
    return application;
  }

  public String getSecret() {
    return secret;
  }

  /**
   * Compares for equality in another key object.
   *
   * @param key another object of type Key
   * @return a boolean
   */
  @Override
  public boolean equals(Object key) {
    if (this == key) {
      return true;
    }

    if (!(key instanceof Key)) {
      return false;
    }

    Key other = (Key) key;
    return Objects.equals(this.application, other.application)
            && Objects.equals(this.secret, other.secret);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("application", application)
      .toString();
  }
}