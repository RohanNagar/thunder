package com.sanction.thunder.email;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import dagger.Module;
import dagger.Provides;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
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
    if (successHtmlPath != null) {
      try {
        return new String(Files.readAllBytes(Paths.get(successHtmlPath)));
      } catch (InvalidPathException e) {
        throw new EmailException("HTML success page path is invalid", e);
      } catch (IOException e) {
        throw new EmailException("Error reading file from path: " + successHtmlPath, e);
      } catch (SecurityException e) {
        throw new EmailException("Error reading file due to invalid file permissions", e);
      }
    }

    try {
      return Resources.toString(Resources.getResource("success.html"), Charsets.UTF_8);
    } catch (IOException e) {
      throw new EmailException("Error reading file from resources folder", e);
    } catch (IllegalArgumentException e) {
      throw new EmailException("Default HTML success file was not found in resources folder", e);
    }
  }

  @Singleton
  @Provides
  @Named("verificationHtml")
  String provideVerificationHtml() {
    if (verificationHtmlPath != null) {
      try {
        return new String(Files.readAllBytes(Paths.get(verificationHtmlPath)));
      } catch (InvalidPathException e) {
        throw new EmailException("HTML verification page path is invalid", e);
      } catch (IOException e) {
        throw new EmailException("Error reading file from path: " + successHtmlPath, e);
      } catch (SecurityException e) {
        throw new EmailException("Error reading file due to invalid file permissions", e);
      }
    }

    try {
      return Resources.toString(Resources.getResource("verification.html"), Charsets.UTF_8);
    } catch (IOException e) {
      throw new EmailException("Error reading file from resources folder", e);
    } catch (IllegalArgumentException e) {
      throw new EmailException("Default HTML verification file not found in resources folder", e);
    }
  }

  @Singleton
  @Provides
  @Named("verificationText")
  String provideVerificationText() {
    if (verificationTextPath != null) {
      try {
        return new String(Files.readAllBytes(Paths.get(verificationTextPath)));
      } catch (InvalidPathException e) {
        throw new EmailException("Text verification page path is invalid", e);
      } catch (IOException e) {
        throw new EmailException("Error reading file from path", e);
      } catch (SecurityException e) {
        throw new EmailException("Error reading file due to invalid file permissions", e);
      }
    }

    try {
      return Resources.toString(Resources.getResource("verification.txt"), Charsets.UTF_8);
    } catch (IOException e) {
      throw new EmailException("Error reading file from resources folder", e);
    } catch (IllegalArgumentException e) {
      throw new EmailException("Default Text verification file not found in resources folder", e);
    }
  }
}
