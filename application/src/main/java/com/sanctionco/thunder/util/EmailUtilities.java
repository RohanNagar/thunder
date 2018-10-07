package com.sanctionco.thunder.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utility methods related to email addresses and email messages.
 *
 * @see com.sanctionco.thunder.email.EmailService EmailService
 */
public class EmailUtilities {
  private static final Logger LOG = LoggerFactory.getLogger(EmailUtilities.class);

  /**
   * Replaces the placeholder inside the given file contents with the given URL.
   *
   * @param fileContents the file contents that contain the placeholder
   * @param placeholder the placeholder string that exists in the file contents
   * @param url the URL to insert in place of the placeholder
   * @return the modified file contents
   */
  public static String replaceUrlPlaceholder(String fileContents, String placeholder, String url) {
    if (!fileContents.contains(placeholder)) {
      LOG.warn("The file contents do not contain any instances of the URL placeholder {}",
          placeholder);
    }

    return fileContents.replaceAll(placeholder, url);
  }
}
