package com.sanction.thunder.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class PilotUser {
  private final Email email;
  private final String password;
  private final Map<String, Object> properties;

  /**
   * Constructs a new PilotUser given the specified parameters.
   *
   * @param email The email of the user.
   * @param password The salted and hashed password of the user.
   * @param properties A map of additional user properties.
   */
  @JsonCreator
  public PilotUser(@JsonProperty("email") Email email,
                   @JsonProperty("password") String password,
                   @JsonProperty("properties") Map<String, Object> properties) {
    this.email = email;
    this.password = password;
    this.properties = properties;
  }

  public Email getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof PilotUser)) {
      return false;
    }

    PilotUser other = (PilotUser) obj;
    return Objects.equals(this.email, other.email);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.email);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", "PilotUser [", "]")
        .add(String.format("email=%s", email))
        .toString();
  }
}
