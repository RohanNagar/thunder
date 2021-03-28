package com.sanctionco.thunder.validation;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides validation methods for {@code User} (in the {@code api} module) properties.
 */
public class PropertyValidator {
  private static final Logger LOG = LoggerFactory.getLogger(PropertyValidator.class);

  private final List<PropertyValidationRule> validationRules;
  private final boolean skipValidation;

  /**
   * Constructs a new {@code PropertyValidator} with the given validation rules.
   *
   * @param validationRules the rules used to validate user property maps
   */
  public PropertyValidator(List<PropertyValidationRule> validationRules) {
    this.skipValidation = validationRules == null;
    this.validationRules = validationRules;
  }

  /**
   * Determines if a given property map is valid, based on the validation rules.
   *
   * @param properties the property map to test for validity
   * @return {@code true} if the property map is valid; {@code false} otherwise
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
   * Determines the Java class represented by the given string. Currently, this method handles
   * the following types:
   *
   * <ul>
   * <li>String</li>
   * <li>Integer</li>
   * <li>Boolean</li>
   * <li>Double</li>
   * <li>List</li>
   * <li>Map</li>
   * <li>Object</li>
   * </ul>
   *
   * @param typename the string that represents a Java type
   * @return the class of the type
   */
  static Class<?> getType(String typename) {
    return switch (typename) {
      case "string" -> String.class;
      case "integer" -> Integer.class;
      case "boolean" -> Boolean.class;
      case "double" -> Double.class;
      case "list" -> List.class;
      case "map" -> Map.class;
      default -> Object.class;
    };
  }
}
