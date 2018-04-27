package com.sanction.thunder.util;

public class EmailUtilities {
  private static final String URL_PLACEHOLDER = "CODEGEN-URL";
  private static final String URL_ENDPOINT = "/verify?email=%s&token=%s&response_type=html";

  public static String replaceUrlPlaceholder(String fileContents, String url) {
    return fileContents.replaceAll(URL_PLACEHOLDER, url);
  }

  public static String buildVerificationUrl(String baseUrl, String email, String token) {
    return String.format(baseUrl + URL_ENDPOINT, email, token);
  }
}
