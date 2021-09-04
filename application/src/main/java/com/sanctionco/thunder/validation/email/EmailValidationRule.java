package com.sanctionco.thunder.validation.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sanctionco.jmail.Email;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Predicate;

/**
 * Represents an email address validation configuration rule.
 *
 * @see EmailValidationConfiguration
 */
public class EmailValidationRule {
  private final Check check;
  private final String value;

  /**
   * Construct a new instance of {@code EmailValidationRule}.
   *
   * @param check the type of {@link Check} to perform
   * @param value the value that is used to match, depending on the type of {@link Check}
   */
  public EmailValidationRule(@JsonProperty("check") Check check,
                             @JsonProperty("value") String value) {
    this.check = Objects.requireNonNull(check);
    this.value = Objects.requireNonNull(value);
  }

  public Check getCheck() {
    return check;
  }

  public String getValue() {
    return value;
  }

  /**
   * Get the rule as a {@link Predicate} that can be used to test any given {@link Email}.
   *
   * @return a predicate that can test email addresses
   */
  public Predicate<Email> getRule() {
    return check.getPredicate(value);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof EmailValidationRule other)) {
      return false;
    }

    return Objects.equals(this.check, other.check)
        && Objects.equals(this.value, other.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(check, value);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", "EmailValidationRule [", "]")
        .add(String.format("check=%s", check))
        .add(String.format("value=%s", value))
        .toString();
  }

  /**
   * Represents all types email address validation checks.
   */
  public enum Check {
    /**
     * Checks that a given email address starts with a string.
     */
    STARTSWITH {
      @Override
      public Predicate<Email> getPredicate(String value) {
        return email -> email.localPart().startsWith(value);
      }
    },

    /**
     * Checks that a given email address ends with a string.
     */
    ENDSWITH {
      @Override
      public Predicate<Email> getPredicate(String value) {
        return email -> email.domain().endsWith(value);
      }
    },

    /**
     * Checks that a given email address contains a string.
     */
    CONTAINS {
      @Override
      public Predicate<Email> getPredicate(String value) {
        return email -> email.toString().contains(value);
      }
    },

    /**
     * Checks that a given email address does not contain a string.
     */
    DOESNOTCONTAIN {
      @Override
      public Predicate<Email> getPredicate(String value) {
        return email -> !email.toString().contains(value);
      }
    };

    /**
     * Get this check as a {@link Predicate} that can be used to test any given {@link Email}.
     *
     * @param value the string value to check against that is used when performing the check
     * @return a predicate that can test email addresses
     */
    public abstract Predicate<Email> getPredicate(String value);
  }
}
