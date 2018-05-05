package com.sanction.thunder.validation;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides validation methods for {@link com.sanction.thunder.models.User User} properties.
 */
public class PropertyValidator {
  private static final Logger LOG = LoggerFactory.getLogger(PropertyValidator.class);

  private final List<PropertyValidationRule> validationRules;
  private final boolean skipValidation;

  public PropertyValidator(List<PropertyValidationRule> validationRules) {
    this.skipValidation = validationRules == null;
    this.validationRules = validationRules;
  }

  /**
   * Determines if a given User property map is valid, based on the validation rules.
   *
   * @param properties The property map to test for validity.
   * @return True if the property map is valid, false otherwise.
   */
  public boolean isValidPropertiesMap(Map<String, Object> properties) {
    if (skipValidation) {
      LOG.info("Skipping property validation because no properties were specified.");
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
   * Determines the Class represented by a given string.
   *
   * @param typename The string to parse.
   * @return The type that is represented by the string.
   */
  static Class getType(String typename) {
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
