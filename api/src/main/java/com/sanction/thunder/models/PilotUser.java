package com.sanction.thunder.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class PilotUser {

  @JsonProperty("username")
  private final String username;

  @JsonProperty("password")
  private final String password;

  @JsonProperty("facebookAccessToken")
  private final String facebookAccessToken;

  @JsonProperty("twitterAccessToken")
  private final String twitterAccessToken;

  @JsonProperty("twitterAccessSecret")
  private final String twitterAccessSecret;

  /**
   * Constructs a new PilotUser given the specified parameters.
   *
   * @param username The username of the user.
   * @param password The salted and hashed password of the user.
   * @param facebookAccessToken The Facebook access token to authenticate the user on Facebook.
   * @param twitterAccessToken The Twitter access token to authenticate the user on Twitter.
   * @param twitterAccessSecret The Twitter access secret to authenticate the user on Twitter.
   */
  @JsonCreator
  public PilotUser(@JsonProperty("username") String username,
                   @JsonProperty("password") String password,
                   @JsonProperty("facebookAccessToken") String facebookAccessToken,
                   @JsonProperty("twitterAccessToken") String twitterAccessToken,
                   @JsonProperty("twitterAccessSecret") String twitterAccessSecret) {
    this.username = username;
    this.password = password;
    this.facebookAccessToken = facebookAccessToken;
    this.twitterAccessToken = twitterAccessToken;
    this.twitterAccessSecret = twitterAccessSecret;
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

  @JsonProperty("twitterAccessToken")
  public String getTwitterAccessSecret() {
    return twitterAccessSecret;
  }

  @JsonProperty("twitterAccessSecret")
  public String getTwitterAccessToken() {
    return twitterAccessToken;
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
    return Objects.equal(this.username, other.username);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.username);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("username", username)
        .toString();
  }
}
