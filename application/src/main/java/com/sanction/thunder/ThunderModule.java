package com.sanction.thunder;

import com.codahale.metrics.MetricRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.sanction.thunder.authentication.Key;
import com.sanction.thunder.validation.PropertyValidator;

import dagger.Module;
import dagger.Provides;

import io.dropwizard.jackson.Jackson;

import java.util.List;
import java.util.Objects;
import javax.inject.Singleton;

/**
 * A Dagger module that provides dependencies at the top level.
 */
@Module
class ThunderModule {
  private final MetricRegistry metrics;
  private final ThunderConfiguration config;

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
}
