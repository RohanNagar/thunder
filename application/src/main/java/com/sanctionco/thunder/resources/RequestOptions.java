package com.sanctionco.thunder.resources;

import com.codahale.metrics.Counter;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.util.Duration;

import jakarta.validation.Valid;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.Response;

import java.util.concurrent.TimeUnit;

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

  /**
   * Set the timeout and timeout handler for the given {@code AsyncResponse} instance.
   *
   * @param response the response instance to set the timeout on
   * @param timeoutCounter the counter to increment if a timeout occurs
   */
  public void setTimeout(AsyncResponse response, Counter timeoutCounter) {
    response.setTimeoutHandler(resp -> {
      timeoutCounter.inc();
      resp.resume(Response.status(Response.Status.REQUEST_TIMEOUT)
          .entity("The request timed out.").build());
    });

    response.setTimeout(operationTimeout.toMilliseconds(), TimeUnit.MILLISECONDS);
  }
}
