package com.sanctionco.thunder.validation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * Represents a rule for {@code User} (in the {@code api} module) property validation. Provides
 * access to the name and type of the property. These rules define what additional properties
 * a user should have in their property map.
 */
public class PropertyValidationRule {
  private final String name;
  private final Class<?> type;

  /**
   * Constructs a new PropertyValidationRule with the given name and type.
   *
   * @param name the property's name
   * @param type the property's type
   */
  public PropertyValidationRule(@JsonProperty("name") String name,
                                @JsonProperty("type") String type) {
    this.name = Objects.requireNonNull(name);
    this.type = PropertyValidator.getType(Objects.requireNonNull(type));
  }

  public String getName() {
    return name;
  }

  public Class<?> getType() {
    return type;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof PropertyValidationRule)) {
      return false;
    }

    PropertyValidationRule other = (PropertyValidationRule) obj;
    return Objects.equals(this.name, other.name)
        && Objects.equals(this.type, other.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", "PropertyValidationRule [", "]")
        .add(String.format("name=%s", name))
        .add(String.format("type=%s", type))
        .toString();
  }
}
