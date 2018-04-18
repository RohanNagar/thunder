package com.sanction.thunder.validation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.StringJoiner;

public class PropertyValidationRule {
  private final String name;
  private final Class type;

  public PropertyValidationRule(@JsonProperty("name") String name,
                                @JsonProperty("type") String type) {
    this.name = name;
    this.type = type.equalsIgnoreCase("string") ? String.class : Object.class;
  }

  public String getName() {
    return name;
  }

  public Class getType() {
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
    return Objects.hash(this.name, this.type);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", "PropertyValidationRule [", "]")
        .add(String.format("name=%s", name))
        .add(String.format("type=%s", type))
        .toString();
  }
}
