package com.sanctionco.thunder.validation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

import javax.validation.Valid;

/**
 * Provides optional configuration options for property validation, including the validated
 * properties. See the {@code ThunderConfiguration} class for more details.
 */
public class PropertyValidationConfiguration {
  private static final boolean DEFAULT_ALLOW_SUPERSET = true;
  private static final boolean DEFAULT_ALLOW_SUBSET = true;

  /**
   * Constructs a new instance of {@code PropertyValidationConfiguration} with default values.
   */
  public PropertyValidationConfiguration() {
    this.allowSuperset = DEFAULT_ALLOW_SUPERSET;
    this.allowSubset = DEFAULT_ALLOW_SUBSET;
    this.validationRules = Collections.emptyList();
  }

  @Valid @JsonProperty("allowSuperset")
  private final Boolean allowSuperset;

  @Valid @JsonProperty("allowSubset")
  private final Boolean allowSubset;

  @Valid @JsonProperty("allowed")
  private final List<PropertyValidationRule> validationRules;

  public Boolean allowSuperset() {
    return allowSuperset;
  }

  public Boolean allowSubset() {
    return allowSubset;
  }

  public List<PropertyValidationRule> getValidationRules() {
    return validationRules;
  }
}
