package com.sanction.thunder.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.StringJoiner;

public class Email {
  private final String address;
  private final boolean verified;
  private final String verificationToken;

  /**
   * Constructs a new Email class given the specified parameters.
   *
   * @param address The email address of the user.
   * @param verified Whether or not the email has been validated.
   * @param verificationToken Hash token used to verify the email is valid.
   */
  @JsonCreator
  public Email(@JsonProperty("address") String address,
               @JsonProperty("verified") boolean verified,
               @JsonProperty("verificationToken") String verificationToken) {
    this.address = address;
    this.verified = verified;
    this.verificationToken = verificationToken;
  }

  public String getAddress() {
    return this.address;
  }

  public String getVerificationToken() {
    return verificationToken;
  }

  public boolean getVerified() {
    return this.verified;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof Email)) {
      return false;
    }

    Email other = (Email) obj;
    return Objects.equals(this.address, other.address)
        && Objects.equals(this.verified, other.verified)
        && Objects.equals(this.verificationToken, other.verificationToken);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.address);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", "Email [", "]")
        .add(String.format("address=%s", address))
        .add(String.format("verified=%b", verified))
        .add(String.format("verificationToken=%s", verificationToken))
        .toString();
  }
}
