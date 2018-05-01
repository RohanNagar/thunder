package com.sanction.thunder.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailUtilities {
  private static final Logger LOG = LoggerFactory.getLogger(EmailUtilities.class);

  private static final String URL_PLACEHOLDER = "CODEGEN-URL";
  private static final String URL_ENDPOINT = "verify?email=%s&token=%s&response_type=html";

  /**
   * Replaces the placeholder inside the given file contents with the given URL.
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

  /**
   * Builds an appropriate verification URL to send to a user in an email.
   *
   * @param baseUrl The base URL, including `http://` or `https://`.
   * @param email The email address of the user that the URL will be sent to.
   * @param token The verification token to include in the URL.
   * @return The full verification URL to send to the user.
   */
  public static String buildVerificationUrl(String baseUrl, String email, String token) {
    String url = baseUrl.endsWith("/")
        ? baseUrl + URL_ENDPOINT
        : baseUrl + "/" + URL_ENDPOINT;

    return String.format(url, email, token);
  }
}
