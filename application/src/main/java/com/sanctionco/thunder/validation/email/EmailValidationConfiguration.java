package com.sanctionco.thunder.validation.email;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

import java.util.Collections;
import java.util.List;

/**
 * Provides optional configuration options for email address validation
 * See the {@code ThunderConfiguration} class for more details.
 */
public class EmailValidationConfiguration {
  private static final List<EmailValidationRule> DEFAULT_RULES = Collections.emptyList();

  /**
   * Constructs a new instance of {@code EmailValidationConfiguration} with default values.
   */
  public EmailValidationConfiguration() {
    this.rules = DEFAULT_RULES;
  }

  @Valid @JsonProperty("rules")
  private final List<EmailValidationRule> rules;

  public List<EmailValidationRule> getRules() {
    return rules;
  }
}
