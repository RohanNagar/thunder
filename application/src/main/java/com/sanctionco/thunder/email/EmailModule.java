package com.sanctionco.thunder.email;

import com.codahale.metrics.MetricRegistry;

import dagger.Module;
import dagger.Provides;

import java.util.Objects;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides object dependencies needed to send verification emails, including the email service
 * and message options.
 *
 * @see com.sanctionco.thunder.ThunderComponent ThunderComponent
 */
@Module
public class EmailModule {
  private final EmailServiceFactory emailServiceFactory;

  /**
   * Constructs a new {@code EmailModule} object with the given configuration.
   *
   * @param emailServiceFactory the configuration that holds SES and message option configuration
   */
  public EmailModule(EmailServiceFactory emailServiceFactory) {
    this.emailServiceFactory = Objects.requireNonNull(emailServiceFactory);
  }

  @Singleton
  @Provides
  EmailService provideEmailService(MetricRegistry metrics) {
    return emailServiceFactory.createEmailService(metrics);
  }

  @Singleton
  @Provides
  EmailHealthCheck provideEmailHealthCheck() {
    return emailServiceFactory.createHealthCheck();
  }
}
