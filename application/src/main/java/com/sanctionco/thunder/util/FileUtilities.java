package com.sanctionco.thunder.util;

import com.google.common.io.Resources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileUtilities {

  /**
   * Reads a file from the resources folder.
   *
   * @param fileName the name of the file
   * @return the file's contents
   * @throws IllegalStateException if the file was not found or there was an error reading the file
   */
  public static String readFileAsResources(String fileName) {
    try {
      return Resources.toString(Resources.getResource(fileName), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Error reading file from resources folder", e);
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("Default file not found in resources folder", e);
    }
  }
}
