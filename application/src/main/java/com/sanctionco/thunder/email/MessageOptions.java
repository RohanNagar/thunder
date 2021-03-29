package com.sanctionco.thunder.email;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Provides email message configuration, including email content and the HTML to display upon
 * successful verification.
 *
 * @param subject the subject of the email message
 * @param bodyHtml the body of the email message in HTML form
 * @param bodyText the body of the email message in plaintext form
 * @param bodyHtmlUrlPlaceholder the placeholder string found in the body HTML that should be
 *                               replaced by a custom URL on each message request
 * @param bodyTextUrlPlaceholder the placeholder string found in the body text that should be
 *                               replaced by a custom URL on each message request
 * @param successHtml the HTML contents to display on successful verification
 */
public record MessageOptions(@JsonProperty("subject") String subject,
                             @JsonProperty("bodyHtmlFile") String bodyHtml,
                             @JsonProperty("bodyTextFile") String bodyText,
                             @JsonProperty("bodyHtmlUrlPlaceholder") String bodyHtmlUrlPlaceholder,
                             @JsonProperty("bodyTextUrlPlaceholder") String bodyTextUrlPlaceholder,
                             @JsonProperty("successHtmlFile") String successHtml) {}
