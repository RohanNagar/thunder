package com.sanction.thunder.validation;

import java.util.List;
import java.util.Map;

public class PropertyValidator {
  private final List<PropertyValidationRule> validationRules;

  public PropertyValidator(List<PropertyValidationRule> validationRules) {
    this.validationRules = validationRules;
  }

  /**
   * Determines if a given property map is valid.
   * @param properties The property map to test for validity.
   * @return True if the property map is valid, false otherwise.
   */
  public boolean isValidPropertiesMap(Map<String, Object> properties) {
    return validationRules.stream()
        .allMatch(rule -> properties.containsKey(rule.getName())
            && rule.getType().isInstance(properties.get(rule.getName())));
  }
}
