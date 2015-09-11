package com.sanction.thunder;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Module;
import dagger.Provides;
import io.dropwizard.jackson.Jackson;

import javax.inject.Singleton;

@Module
public class ThunderModule {
  private final MetricRegistry metrics;

  public ThunderModule(MetricRegistry metrics) {
    this.metrics = metrics;
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
}
