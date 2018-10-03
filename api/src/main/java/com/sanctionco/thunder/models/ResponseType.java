package com.sanctionco.thunder.models;

/**
 * Represents a type of HTTP response that can be expected from an API method.
 * This enum is typically used as a query parameter when an API method can return multiple
 * response types.
 */
public enum ResponseType {
  JSON("json"),
  HTML("html");

  private final String text;

  ResponseType(String text) {
    this.text = text;
  }

  /**
   * Provides a ResponseType representation of the given text.
   *
   * @param text the text to parse
   * @return the ResponseType representation or {@code null} if none match
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

