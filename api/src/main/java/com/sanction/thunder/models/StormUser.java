package com.sanction.thunder.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class StormUser {

  @JsonProperty("username")
  private final String username;

  @JsonProperty("password")
  private final String password;

  @JsonProperty("facebookAccessToken")
  private final String facebookAccessToken;

  /**
   * Constructs a new StormUser given the specified parameters.
   *
   * @param username The username of the user.
   * @param password The salted and hashed password of the user.
   * @param facebookAccessToken The facebook access token to authenticate the user.
   */
  @JsonCreator
  public StormUser(@JsonProperty("username") String username,
                   @JsonProperty("password") String password,
                   @JsonProperty("facebookAccessToken") String facebookAccessToken) {
    this.username = username;
    this.password = password;
    this.facebookAccessToken = facebookAccessToken;
  }

  @JsonProperty("username")
  public String getUsername() {
    return username;
  }

  @JsonProperty("password")
  public String getPassword() {
    return password;
  }

  @JsonProperty("facebookAccessToken")
  public String getFacebookAccessToken() {
    return facebookAccessToken;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("username", username)
        .toString();
  }
}
