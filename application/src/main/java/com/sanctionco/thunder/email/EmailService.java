package com.sanctionco.thunder.email;

import com.sanctionco.thunder.models.Email;

/**
 * Interface used to handle sending emails to an email address.
 */
public interface EmailService {

  /**
   * Sends an email to the specified email address.
   *
   * @param to The Email to send to.
   * @param subjectString The subject of the email to be sent.
   * @param htmlBodyString The HTML body of the email to be sent.
   * @param bodyString The text body of the email to be sent.
   * @return A boolean indicating email send success or failure.
   */
  boolean sendEmail(Email to, String subjectString, String htmlBodyString, String bodyString);
}
