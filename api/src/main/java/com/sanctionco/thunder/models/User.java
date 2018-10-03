package com.sanctionco.thunder.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Represents a user, providing access to the user's email, password, and additional properties.
 */
public class User {
  private final Email email;
  private final String password;
  private final Map<String, Object> properties;

  /**
   * Constructs a new user with the given email, password, and additional properties.
   *
   * @param email the user's email. This is the user's primary key.
   * @param password the user's hashed (not plaintext) password
   * @param properties the map of additional user properties. If null, an empty map will be used.
   */
  @JsonCreator
  public User(@JsonProperty("email") Email email,
              @JsonProperty("password") String password,
              @JsonProperty("properties") Map<String, Object> properties) {
    this.email = email;
    this.password = password;
    this.properties = Optional.ofNullable(properties).orElse(Collections.emptyMap());
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

    if (!(obj instanceof User)) {
      return false;
    }

    User other = (User) obj;
    return Objects.equals(this.email, other.email)
        && Objects.equals(this.password, other.password)
        && Objects.equals(this.properties, other.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(email, password, properties);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", "User [", "]")
        .add(String.format("email=%s", email))
        .add(String.format("password=%s", password))
        .add(String.format("properties=%s", properties))
        .toString();
  }
}
