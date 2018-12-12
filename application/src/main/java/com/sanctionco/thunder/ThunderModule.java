package com.sanctionco.thunder;

import com.codahale.metrics.MetricRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.sanctionco.thunder.authentication.Key;
import com.sanctionco.thunder.crypto.HashService;
import com.sanctionco.thunder.validation.PropertyValidator;
import com.sanctionco.thunder.validation.RequestValidator;

import dagger.Module;
import dagger.Provides;

import io.dropwizard.jackson.Jackson;

import java.util.List;
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
  List<Key> provideApprovedKeys() {
    return config.getApprovedKeys();
  }

  @Singleton
  @Provides
  PropertyValidator providePropertyValidator() {
    return new PropertyValidator(config.getValidationRules());
  }

  @Singleton
  @Provides
  RequestValidator provideRequestValidator(PropertyValidator propertyValidator) {
    LOG.info("Password header check: {}", config.getHashConfiguration().isHeaderCheckEnabled());

    return new RequestValidator(
        propertyValidator,
        config.getHashConfiguration().isHeaderCheckEnabled());
  }

  @Singleton
  @Provides
  HashService provideHashService() {
    LOG.info("Using {} as the password hashing algorithm.",
        config.getHashConfiguration().getAlgorithm());
    LOG.info("Server-side hashing: {}",
        config.getHashConfiguration().serverSideHash());

    return config.getHashConfiguration().getAlgorithm()
        .newHashService(config.getHashConfiguration().serverSideHash());
  }
}
