package com.sanction.thunder.models;

public enum ResponseType {
  JSON("json"),
  HTML("html");

  private final String text;

  ResponseType(String text) {
    this.text = text;
  }

  /**
   * Provides a ResponseType representation of a given string.
   *
   * @param text The string to parse into a ResponseType.
   * @return The corresponding ResponseType representation or {@code null} if none match.
   */
  public static ResponseType fromString(String text) {
    for (ResponseType type : ResponseType.values()) {
      if (type.text.equalsIgnoreCase(text)) {
        return type;
      }
    }

    return null;
  }

  @Override
  public String toString() {
    return this.text;
  }
}

