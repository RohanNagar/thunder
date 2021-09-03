package com.sanctionco.thunder;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanctionco.jmail.EmailValidator;
import com.sanctionco.jmail.JMail;
import com.sanctionco.thunder.crypto.HashService;
import com.sanctionco.thunder.resources.RequestOptions;
import com.sanctionco.thunder.secrets.SecretProvider;
import com.sanctionco.thunder.validation.PropertyValidator;
import com.sanctionco.thunder.validation.RequestValidator;

import dagger.Module;
import dagger.Provides;

import io.dropwizard.jackson.Jackson;

import java.util.Objects;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides object dependencies used by objects constructed in the {@link ThunderComponent}.
 */
@Module
class ThunderModule {
  private static final Logger LOG = LoggerFactory.getLogger(ThunderModule.class);

  private final MetricRegistry metrics;
  private final ThunderConfiguration config;

  /**
   * Constructs a new {@code ThunderModule} with the given metrics and configuration.
   *
   * @param metrics the metrics that will be used to build meters and counters
   * @param config the Thunder configuration that provides information to build objects
   */
  ThunderModule(MetricRegistry metrics, ThunderConfiguration config) {
    this.metrics = Objects.requireNonNull(metrics);
    this.config = Objects.requireNonNull(config);
  }

  @Singleton
  @Provides
  ObjectMapper provideObjectMapper() {
    return Jackson.newObjectMapper();
  }

  @Singleton
  @Provides
  MetricRegistry provideMetricRegistry() {
    return metrics;
  }

  @Singleton
  @Provides
  EmailValidator provideEmailValidator() {
    return JMail.strictValidator();
  }

  @Singleton
  @Provides
  PropertyValidator providePropertyValidator() {
    LOG.info("Property validation: allowSubset: {}, allowSuperset: {}, rules: {}",
        config.getValidationConfiguration().allowSubset(),
        config.getValidationConfiguration().allowSuperset(),
        config.getValidationConfiguration().getValidationRules());

    return new PropertyValidator(config.getValidationConfiguration());
  }

  @Singleton
  @Provides
  RequestValidator provideRequestValidator(EmailValidator emailValidator,
                                           PropertyValidator propertyValidator,
                                           HashService hashService) {
    LOG.info("Password header check: {}", config.getHashConfiguration().isHeaderCheckEnabled());

    return new RequestValidator(
        emailValidator,
        propertyValidator,
        hashService,
        config.getHashConfiguration().isHeaderCheckEnabled());
  }

  @Singleton
  @Provides
  HashService provideHashService() {
    LOG.info("Hashing configuration: "
        + "algorithm: {}, server-side hashing: {}, allow common password mistakes: {}",
        config.getHashConfiguration().getAlgorithm(),
        config.getHashConfiguration().serverSideHash(),
        config.getHashConfiguration().allowCommonMistakes());

    return config.getHashConfiguration().getAlgorithm().newHashService(
        config.getHashConfiguration().serverSideHash(),
        config.getHashConfiguration().allowCommonMistakes());
  }

  @Singleton
  @Provides
  SecretProvider provideSecretProvider() {
    return config.getSecretProvider();
  }

  @Singleton
  @Provides
  RequestOptions provideRequestOptions() {
    return config.getRequestOptions();
  }
}
