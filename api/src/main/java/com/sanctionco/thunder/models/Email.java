package com.sanctionco.thunder.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * Represents an email address, providing access to the email address,
 * verification status, and verification token.
 *
 * @see User
 */
public class Email {
  private final String address;
  private final boolean verified;
  private final String verificationToken;

  /**
   * Constructs a new email with the given address, verified status, and verification token.
   *
   * @param address the email's address
   * @param verified whether the email address has been verified
   * @param verificationToken the email's verification token used to verify the email address
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
    return address;
  }

  public String getVerificationToken() {
    return verificationToken;
  }

  public boolean isVerified() {
    return verified;
  }

  /**
   * Creates a copy of this {@code Email} instance with the verified
   * property set to true.
   *
   * @return a new instance of {@code Email} with the verified property set to true
   */
  public Email verifiedCopy() {
    return new Email(address, true, verificationToken);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof Email other)) {
      return false;
    }

    return Objects.equals(this.address, other.address)
        && Objects.equals(this.verified, other.verified)
        && Objects.equals(this.verificationToken, other.verificationToken);
  }

  @Override
  public int hashCode() {
    return Objects.hash(address, verified, verificationToken);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", "Email [", "]")
        .add(String.format("address=%s", address))
        .add(String.format("verified=%b", verified))
        .add(String.format("verificationToken=%s", verificationToken))
        .toString();
  }

  /**
   * Creates a new unverified {@code Email} instance.
   *
   * @param address the email address
   * @return a new {@code Email} instance
   */
  public static Email unverified(String address) {
    return new Email(address, false, null);
  }
}
