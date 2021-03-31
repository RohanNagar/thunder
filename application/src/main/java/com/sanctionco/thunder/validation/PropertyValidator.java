package com.sanctionco.thunder.validation;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides validation methods for {@code User} (in the {@code api} module) properties.
 */
public class PropertyValidator {
  private static final Logger LOG = LoggerFactory.getLogger(PropertyValidator.class);

  private final PropertyValidationConfiguration validationOptions;

  /**
   * Constructs a new {@code PropertyValidator} with the given validation configuration.
   *
   * @param validationOptions the configuration used to validate user property maps
   */
  public PropertyValidator(PropertyValidationConfiguration validationOptions) {
    this.validationOptions = Objects.requireNonNull(validationOptions);
  }

  /**
   * Determines if a given property map is valid, based on the validation rules.
   *
   * @param properties the property map to test for validity
   * @return {@code true} if the property map is valid; {@code false} otherwise
   */
  public boolean isValidPropertiesMap(Map<String, Object> properties) {
    if (!verifySize(properties)) {
      LOG.info("The size of the property map {} failed verification.", properties);
      return false;
    }

    // Both false. All specified fields must exist and be correct, and no more.
    if (!validationOptions.allowSuperset() && !validationOptions.allowSubset()) {
      return validationOptions.getValidationRules().stream()
          .allMatch(rule -> properties.containsKey(rule.getName())
              && rule.getType().isInstance(properties.get(rule.getName())));
    }

    // allowSuperset true and allowSubset false. All specified fields must exist and be correct
    if (validationOptions.allowSuperset() && !validationOptions.allowSubset()) {
      return validationOptions.getValidationRules().stream()
          .allMatch(rule -> properties.containsKey(rule.getName())
              && rule.getType().isInstance(properties.get(rule.getName())));
    }

    // allowSuperset false and allowSubset true. All properties must be in the list of specified
    // properties.
    if (!validationOptions.allowSuperset() && validationOptions.allowSubset()) {
      // Make sure all properties names exist in the rules
      Map<String, Class<?>> allowedMap = validationOptions.getValidationRules().stream()
          .collect(Collectors.toMap(
              PropertyValidationRule::getName,
              PropertyValidationRule::getType));

      return properties.entrySet().stream().allMatch(entry ->
          allowedMap.containsKey(entry.getKey())
              && allowedMap.get(entry.getKey()).isInstance(entry.getValue()));
    }

    // Both true. The properties that are present and specified will be checked to make sure they
    // are the correct type.
    Map<String, Class<?>> allowedMap = validationOptions.getValidationRules().stream()
        .collect(Collectors.toMap(
            PropertyValidationRule::getName,
            PropertyValidationRule::getType));

    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      if (allowedMap.containsKey(entry.getKey())
          && !allowedMap.get(entry.getKey()).isInstance(entry.getValue())) {
        return false;
      }
    }

    return true;
  }

  boolean verifySize(Map<String, Object> properties) {
    // Neither subset or superset allowed, the size must match
    if (!validationOptions.allowSuperset() && !validationOptions.allowSubset()) {
      LOG.info("Verifying that the property map has the same size as the validation rules...");
      return properties.size() == validationOptions.getValidationRules().size();
    }

    // Only subset allowed, the size must be less than or equal to
    if (validationOptions.allowSubset() && !validationOptions.allowSuperset()) {
      LOG.info("Verifying that the property map size is <= to the validation rules...");
      return properties.size() <= validationOptions.getValidationRules().size();
    }

    // Only superset allowed, the size must be greater than or equal to
    if (validationOptions.allowSuperset() && !validationOptions.allowSubset()) {
      LOG.info("Verifying that the property map size is >= to the validation rules...");
      return properties.size() >= validationOptions.getValidationRules().size();
    }

    // Both subset and superset allowed, the size can be anything
    LOG.info("Property map size validation not required.");
    return true;
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
      case "string", "str" -> String.class;
      case "integer", "int" -> Integer.class;
      case "boolean", "bool" -> Boolean.class;
      case "double" -> Double.class;
      case "list" -> List.class;
      case "map" -> Map.class;
      default -> Object.class;
    };
  }
}
