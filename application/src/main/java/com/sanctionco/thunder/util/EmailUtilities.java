package com.sanctionco.thunder.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utility classes related to email addresses and messages. Used
 * in the {@link com.sanctionco.thunder.email.EmailService EmailService} class.
 */
public class EmailUtilities {
  private static final Logger LOG = LoggerFactory.getLogger(EmailUtilities.class);

  /**
   * Replaces the placeholder inside the given file contents with the given URL.
   *
   * @param fileContents The file contents to replace in.
   * @param url The URL to insert in place of the placeholder.
   * @return A string with modified file contents including the URL.
   */
  public static String replaceUrlPlaceholder(String fileContents, String placeholder, String url) {
    if (!fileContents.contains(placeholder)) {
      LOG.warn("The file contents do not contain any instances of the URL placeholder {}",
          placeholder);
    }

    return fileContents.replaceAll(placeholder, url);
  }
}
