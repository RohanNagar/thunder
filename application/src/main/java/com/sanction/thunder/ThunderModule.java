package com.sanction.thunder;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanction.thunder.authentication.Key;
import dagger.Module;
import dagger.Provides;
import io.dropwizard.jackson.Jackson;

import java.util.List;

import javax.inject.Singleton;

@Module
public class ThunderModule {
  private final MetricRegistry metrics;
  private final ThunderConfiguration config;

  public ThunderModule(MetricRegistry metrics, ThunderConfiguration config) {
    this.metrics = metrics;
    this.config = config;
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
}
