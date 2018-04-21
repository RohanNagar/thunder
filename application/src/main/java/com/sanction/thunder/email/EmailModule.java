package com.sanction.thunder.email;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import dagger.Module;
import dagger.Provides;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class EmailModule {
  private final String endpoint;
  private final String region;
  private final String fromAddress;
  private final String successHtmlPath;
  private final String verificationHtmlPath;
  private final String verificationTextPath;

  /**
   * Constructs a new EmailModule object.
   *
   * @param emailConfiguration The configuration to get SES information from
   */
  public EmailModule(EmailConfiguration emailConfiguration) {
    this.endpoint = emailConfiguration.getEndpoint();
    this.region = emailConfiguration.getRegion();
    this.fromAddress = emailConfiguration.getFromAddress();
    this.successHtmlPath = emailConfiguration.getSuccessHtmlPath();
    this.verificationHtmlPath = emailConfiguration.getVerificationHtmlPath();
    this.verificationTextPath = emailConfiguration.getVerificationTextPath();
  }

  @Singleton
  @Provides
  AmazonSimpleEmailService provideAmazonSimpleEmailService() {
    return AmazonSimpleEmailServiceClientBuilder.standard()
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
        .build();
  }

  @Singleton
  @Provides
  EmailService provideEmailService(AmazonSimpleEmailService emailService) {
    return new EmailService(emailService, fromAddress);
  }

  @Singleton
  @Provides
  @Named("successHtml")
  String provideSuccessHtml() {
    try {
      return Resources.toString(Resources.getResource(successHtmlPath), Charsets.UTF_8);
    } catch (IOException e) {
      throw new EmailException("Error reading file from path: " + successHtmlPath, e);
    } catch (Exception e) {
      throw new EmailException("Error while providing success HTML", e);
    }
  }

  @Singleton
  @Provides
  @Named("verificationHtml")
  String provideVerificationHtml() {
    try {
      return Resources.toString(Resources.getResource(verificationHtmlPath), Charsets.UTF_8);
    } catch (IOException e) {
      throw new EmailException("Error reading file from path: " + verificationHtmlPath, e);
    } catch (Exception e) {
      throw new EmailException("Error while providing verification HTML", e);
    }
  }

  @Singleton
  @Provides
  @Named("verificationText")
  String provideVerificationText() {
    try {
      return Resources.toString(Resources.getResource(verificationTextPath), Charsets.UTF_8);
    } catch (IOException e) {
      throw new EmailException("Error reading file from path:" + verificationTextPath, e);
    } catch (Exception e) {
      throw new EmailException("Error while providing verification text", e);
    }
  }
}
