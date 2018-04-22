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
      readFileFromPath(successHtmlPath);
    }

    return readFileAsResources("success.html");
  }

  @Singleton
  @Provides
  @Named("verificationHtml")
  String provideVerificationHtml() {
    if (verificationHtmlPath != null) {
      return readFileFromPath(verificationHtmlPath);
    }

    return readFileAsResources("verification.html");
  }

  @Singleton
  @Provides
  @Named("verificationText")
  String provideVerificationText() {
    if (verificationTextPath != null) {
      return readFileFromPath(verificationTextPath);
    }

    return readFileAsResources("verification.txt");
  }

  /**
   * Reads a file as a <code>String</code> from a path.
   *
   * @param path The path to the file to be read.
   * @return The file contents as a <code>String</code>.
   */
  private String readFileFromPath(String path) {
    try {
      return new String(Files.readAllBytes(Paths.get(path)));
    } catch (InvalidPathException e) {
      throw new EmailException("File path is invalid", e);
    } catch (IOException e) {
      throw new EmailException("Error reading file from path", e);
    } catch (SecurityException e) {
      throw new EmailException("Error reading file due to invalid file permissions", e);
    }
  }

  /**
   * Reads a file from the resources folder.
   *
   * @param fileName The name of the file to be read.
   * @return The contents of the file as a <code>String</code>.
   */
  private String readFileAsResources(String fileName) {
    try {
      return Resources.toString(Resources.getResource(fileName), Charsets.UTF_8);
    } catch (IOException e) {
      throw new EmailException("Error reading file from resources folder", e);
    } catch (IllegalArgumentException e) {
      throw new EmailException("Default file not found in resources folder", e);
    }
  }
}
