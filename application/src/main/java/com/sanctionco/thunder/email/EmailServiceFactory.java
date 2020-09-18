package com.sanctionco.thunder.email;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.io.Resources;

import io.dropwizard.jackson.Discoverable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the base interface for the {@code EmailServiceFactory} which allows for instance
 * creation of {@link EmailService} objects and {@link EmailHealthCheck} objects.
 *
 * <p>This class is to be used within the Dropwizard configuration and provides polymorphic
 * configuration - which allows us to implement the {@code email} section of our configuration
 * with multiple configuration classes.
 *
 * <p>The {@code type} property on the configuration object is used to determine which implementing
 * class to construct.
 *
 * <p>This class must be registered in
 * {@code /resources/META-INF/services/io.dropwizard.jackson.Discoverable}.
 *
 * <p>See the {@code ThunderConfiguration} class for usage.
 */
@SuppressWarnings("ConstantConditions")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public abstract class EmailServiceFactory implements Discoverable {
  private static final Logger LOG = LoggerFactory.getLogger(EmailServiceFactory.class);

  private static final String DEFAULT_SUBJECT = "Account Verification";
  private static final String DEFAULT_BODY_HTML_FILE = "verification.html";
  private static final String DEFAULT_BODY_TEXT_FILE = "verification.txt";
  private static final String DEFAULT_PLACEHOLDER = "CODEGEN-URL";
  private static final String DEFAULT_SUCCESS_HTML_FILE = "success.html";

  @JsonProperty("messageOptions")
  private final MessageOptionsConfiguration messageOptionsConfiguration = null;

  @JsonGetter("messageOption")
  public MessageOptionsConfiguration getMessageOptionsConfiguration() {
    return messageOptionsConfiguration;
  }

  @JsonProperty("enabled")
  private final Boolean enabled = true;

  public Boolean isEnabled() {
    return enabled;
  }

  @JsonProperty("fromAddress")
  private final String fromAddress = null;

  public String getFromAddress() {
    return fromAddress;
  }

  /**
   * Creates a new instance of {@link EmailService}.
   *
   * @param metrics the MetricRegistry instance to use
   * @return the created EmailService object
   */
  public abstract EmailService createEmailService(MetricRegistry metrics);

  /**
   * Creates a new instance of {@link EmailHealthCheck}.
   *
   * @return the created EmailHealthCheck object
   */
  public abstract EmailHealthCheck createHealthCheck();

  /**
   * Provides email message options to use for building emails.
   *
   * @return configured message options or defaults
   */
  public MessageOptions getMessageOptions() {
    return getMessageOptions(getBodyHtml(), getBodyText(), getSuccessHtml());
  }

  /**
   * Provides email message options to use for building emails.
   *
   * @param bodyHtml the full email body HTML
   * @param bodyText the full email body text
   * @param successHtml the full success HTML page
   * @return configured message options or default
   */
  MessageOptions getMessageOptions(String bodyHtml, String bodyText, String successHtml) {
    if (messageOptionsConfiguration == null) {
      return new MessageOptions(
          DEFAULT_SUBJECT, bodyHtml, bodyText,
          DEFAULT_PLACEHOLDER, DEFAULT_PLACEHOLDER, successHtml);
    }

    // Use the placeholder string for the body files only if both are customized
    String bodyHtmlUrlPlaceholder = messageOptionsConfiguration.getUrlPlaceholderString() != null
        && messageOptionsConfiguration.getBodyHtmlFilePath() != null
        ? messageOptionsConfiguration.getUrlPlaceholderString() : DEFAULT_PLACEHOLDER;

    String bodyTextUrlPlaceholder = messageOptionsConfiguration.getUrlPlaceholderString() != null
        && messageOptionsConfiguration.getBodyTextFilePath() != null
        ? messageOptionsConfiguration.getUrlPlaceholderString() : DEFAULT_PLACEHOLDER;

    LOG.info("Using the URL Placeholder {} for the body HTML", bodyHtmlUrlPlaceholder);
    LOG.info("Using the URL Placeholder {} for the body text", bodyTextUrlPlaceholder);

    return new MessageOptions(
        Optional.ofNullable(messageOptionsConfiguration.getSubject()).orElse(DEFAULT_SUBJECT),
        bodyHtml, bodyText, bodyHtmlUrlPlaceholder, bodyTextUrlPlaceholder, successHtml);
  }

  /**
   * Reads the success HTML file contents into memory.
   *
   * @return the full success HTML
   */
  String getSuccessHtml() {
    var messageOptionsConfiguration = getMessageOptionsConfiguration();

    if (messageOptionsConfiguration != null
        && messageOptionsConfiguration.getSuccessHtmlFilePath() != null) {
      return readFileFromPath(messageOptionsConfiguration.getSuccessHtmlFilePath());
    }

    return readFileAsResources(DEFAULT_SUCCESS_HTML_FILE);
  }

  /**
   * Reads the email body HTML file contents into memory.
   *
   * @return the full body HTML
   */
  String getBodyHtml() {
    var messageOptionsConfiguration = getMessageOptionsConfiguration();

    if (messageOptionsConfiguration != null
        && messageOptionsConfiguration.getBodyHtmlFilePath() != null) {
      return readFileFromPath(messageOptionsConfiguration.getBodyHtmlFilePath());
    }

    return readFileAsResources(DEFAULT_BODY_HTML_FILE);
  }

  /**
   * Reads the email body text file contents into memory.
   *
   * @return the full body text
   */
  String getBodyText() {
    var messageOptionsConfiguration = getMessageOptionsConfiguration();

    if (messageOptionsConfiguration != null
        && messageOptionsConfiguration.getBodyTextFilePath() != null) {
      return readFileFromPath(messageOptionsConfiguration.getBodyTextFilePath());
    }

    return readFileAsResources(DEFAULT_BODY_TEXT_FILE);
  }

  /**
   * Reads a file as a {@code String} from the given path.
   *
   * @param path the path to the file
   * @return the file's contents
   * @throws EmailException if the file path is invalid or there was an error reading the file
   */
  private String readFileFromPath(String path) {
    try {
      return Files.readString(Paths.get(path), StandardCharsets.UTF_8);
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
   * @param fileName the name of the file
   * @return the file's contents
   * @throws EmailException if the file was not found or there was an error reading the file
   */
  private String readFileAsResources(String fileName) {
    try {
      return Resources.toString(Resources.getResource(fileName), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new EmailException("Error reading file from resources folder", e);
    } catch (IllegalArgumentException e) {
      throw new EmailException("Default file not found in resources folder", e);
    }
  }
}
