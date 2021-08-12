package com.sanctionco.thunder.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.util.Duration;

import javax.validation.Valid;

/**
 * Provides optional configuration options for Thunder operations.
 * See the {@code ThunderConfiguration} class for more details.
 */
public class RequestOptions {
  private static final Duration DEFAULT_OPERATION_TIMEOUT = Duration.seconds(30);

  public RequestOptions() {
    this.operationTimeout = DEFAULT_OPERATION_TIMEOUT;
  }

  @Valid @JsonProperty("operationTimeout")
  private final Duration operationTimeout;

  public Duration operationTimeout() {
    return operationTimeout;
  }
}
