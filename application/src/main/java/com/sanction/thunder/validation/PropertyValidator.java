package com.sanction.thunder.validation;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyValidator {
  private static final Logger LOG = LoggerFactory.getLogger(PropertyValidator.class);

  private final List<PropertyValidationRule> validationRules;
  private final boolean shouldValidateProperties;

  public PropertyValidator(List<PropertyValidationRule> validationRules) {
    this.validationRules = validationRules;
    this.shouldValidateProperties = validationRules != null;
  }

  /**
   * Determines if a given property map is valid.
   * @param properties The property map to test for validity.
   * @return True if the property map is valid, false otherwise.
   */
  public boolean isValidPropertiesMap(Map<String, Object> properties) {
    if (!shouldValidateProperties) {
      LOG.info("Skipping property validation because none was specified.");
      return true;
    }

    // Check for size
    if (properties.size() != validationRules.size()) {
      LOG.info("Properties size does not match the number of validation rules.");
      return false;
    }

    // Match name and type
    return validationRules.stream()
        .allMatch(rule -> properties.containsKey(rule.getName())
            && rule.getType().isInstance(properties.get(rule.getName())));
  }

  /**
   * Determines the Class object for a given string.
   * @param typename The string to parse the class object for.
   * @return The type that is represented by the string.
   */
  public static Class getType(String typename) {
    switch (typename) {
      case "string":
        return String.class;

      case "integer":
        return Integer.class;

      case "boolean":
        return Boolean.class;

      case "double":
        return Double.class;

      case "list":
        return List.class;

      case "map":
        return Map.class;

      default:
        return Object.class;
    }
  }
}
