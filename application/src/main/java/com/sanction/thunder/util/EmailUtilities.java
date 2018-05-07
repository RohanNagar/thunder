package com.sanction.thunder.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utility classes related to email addresses and messages. Used
 * in the {@link com.sanction.thunder.email.EmailService EmailService} class.
 */
public class EmailUtilities {
  private static final Logger LOG = LoggerFactory.getLogger(EmailUtilities.class);

  private static final String URL_PLACEHOLDER = "CODEGEN-URL";

  /**
   * Replaces the placeholder inside the given file contents with the given URL.
   *
   * @param fileContents The file contents to replace in.
   * @param url The URL to insert in place of the placeholder.
   * @return A string with modified file contents including the URL.
   */
  public static String replaceUrlPlaceholder(String fileContents, String url) {
    if (!fileContents.contains(URL_PLACEHOLDER)) {
      LOG.warn("The email file contents do not contain any instances of the URL placeholder {}",
          URL_PLACEHOLDER);
    }

    return fileContents.replaceAll(URL_PLACEHOLDER, url);
  }
}
