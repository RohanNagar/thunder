package com.sanction.thunder.authentication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class Key {

  @JsonProperty("application")
  private final String application;

  @JsonProperty("secret")
  private final String secret;

  @JsonCreator
  public Key(@JsonProperty("application") String application,
             @JsonProperty("secret") String secret) {
    this.application = checkNotNull(application);
    this.secret = checkNotNull(secret);
  }

  @JsonProperty("application")
  public String getApplication() {
    return application;
  }

  @JsonProperty("secret")
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
    return Objects.equal(this.application, other.application)
        && Objects.equal(this.secret, other.secret);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.application, this.secret);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("application", application)
      .toString();
  }
}
