package com.sanction.thunder.util;

public class EmailUtilities {
  private static final String URL_PLACEHOLDER = "CODEGEN-URL";
  private static final String URL_ENDPOINT = "verify?email=%s&token=%s&response_type=html";

  public static String replaceUrlPlaceholder(String fileContents, String url) {
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
