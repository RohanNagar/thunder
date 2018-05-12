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
import java.util.Objects;
import java.util.Optional;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * A Dagger module that provides dependencies related to email services.
 */
@Module
public class EmailModule {
  private static final String DEFAULT_SUBJECT = "Account Verification";
  private static final String DEFAULT_VERIFICATION_HTML = "verification.html";
  private static final String DEFAULT_VERIFICATION_TEXT = "verification.txt";
  private static final String DEFAULT_PLACEHOLDER = "CODEGEN-URL";
  private static final String DEFAULT_SUCCESS_PAGE = "success.html";

  private final String endpoint;
  private final String region;
  private final String fromAddress;

  private final MessageOptionsConfiguration messageOptionsConfiguration;

  /**
   * Constructs a new EmailModule object.
   *
   * @param emailConfiguration The configuration to get SES information from.
   *
   * @see EmailConfiguration
   */
  public EmailModule(EmailConfiguration emailConfiguration) {
    this.endpoint = Objects.requireNonNull(emailConfiguration.getEndpoint());
    this.region = Objects.requireNonNull(emailConfiguration.getRegion());
    this.fromAddress = Objects.requireNonNull(emailConfiguration.getFromAddress());

    this.messageOptionsConfiguration = emailConfiguration.getMessageOptionsConfiguration();
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
  MessageOptions provideMessageOptions(@Named("bodyHtml") String bodyHtml,
                                       @Named("bodyText") String bodyText,
                                       @Named("successHtml") String successHtml) {
    if (messageOptionsConfiguration == null) {
      return new MessageOptions(
          DEFAULT_SUBJECT,
          bodyHtml,
          bodyText,
          DEFAULT_PLACEHOLDER,
          successHtml);
    }

    return new MessageOptions(
        Optional.ofNullable(messageOptionsConfiguration.getSubject())
            .orElse(DEFAULT_SUBJECT),
        bodyHtml,
        bodyText,
        Optional.ofNullable(messageOptionsConfiguration.getUrlPlaceholderString())
            .orElse(DEFAULT_PLACEHOLDER),
        successHtml);
  }

  @Singleton
  @Provides
  @Named("successHtml")
  String provideSuccessHtml() {
    if (messageOptionsConfiguration != null
        && messageOptionsConfiguration.getSuccessHtmlFilePath() != null) {
      return readFileFromPath(messageOptionsConfiguration.getSuccessHtmlFilePath());
    }

    return readFileAsResources(DEFAULT_SUCCESS_PAGE);
  }

  @Singleton
  @Provides
  @Named("bodyHtml")
  String provideBodyHtml() {
    if (messageOptionsConfiguration != null
        && messageOptionsConfiguration.getBodyHtmlFilePath() != null) {
      return readFileFromPath(messageOptionsConfiguration.getBodyHtmlFilePath());
    }

    return readFileAsResources(DEFAULT_VERIFICATION_HTML);
  }

  @Singleton
  @Provides
  @Named("bodyText")
  String provideBodyText() {
    if (messageOptionsConfiguration != null
        && messageOptionsConfiguration.getBodyTextFilePath() != null) {
      return readFileFromPath(messageOptionsConfiguration.getBodyTextFilePath());
    }

    return readFileAsResources(DEFAULT_VERIFICATION_TEXT);
  }

  /**
   * Reads a file as a {@code String} from a path.
   *
   * @param path The path to the file to be read.
   * @return The file contents as a {@code String}.
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
   * @return The contents of the file as a {@code String}.
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
