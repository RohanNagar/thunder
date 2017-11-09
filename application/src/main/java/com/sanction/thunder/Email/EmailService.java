package com.sanction.thunder.Email;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.sanction.thunder.models.Email;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.simpleemail.model.*;


public class EmailService {

  private final String FROM = "noreply@sanctionco.com";

  public void sendEmail(Email to, String subjectString, String bodyString) {
    Destination destination = new Destination().withToAddresses(new String[] {to.getAddress()});

    Content subjectText = new Content().withData(subjectString);
    Content bodyText = new Content().withData(bodyString);

    Body body = new Body().withText(bodyText);

    Message message = new Message().withSubject(subjectText).withBody(body);

    SendEmailRequest request = new SendEmailRequest()
        .withSource(FROM).withDestination(destination)
        .withMessage(message);

    ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();

    credentialsProvider.getCredentials();

    AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder
        .standard()
        .withCredentials(credentialsProvider)
        .withRegion("us-east-1")
        .build();

    client.sendEmail(request);
  }

}