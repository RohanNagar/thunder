package com.sanctionco.thunder.email;

import com.sanctionco.thunder.models.Email;

/**
 * Provides the base interface for the EmailService. Provides a method to send an email message.
 */
public interface EmailService {

  /**
   * Sends an email with the given subject and body to the given email address.
   *
   * @param to the message recipient's email information
   * @param subjectString the subject of the message
   * @param htmlBodyString the HTML body of the message
   * @param bodyString the text body of the message
   * @return {@code true} if the message was successfully sent; {@code false} otherwise
   */
  boolean sendEmail(Email to, String subjectString, String htmlBodyString, String bodyString);
}
