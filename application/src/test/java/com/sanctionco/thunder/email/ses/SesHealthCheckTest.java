package com.sanctionco.thunder.email.ses;

import com.codahale.metrics.health.HealthCheck;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.ses.SesAsyncClient;
import software.amazon.awssdk.services.ses.model.GetAccountSendingEnabledResponse;
import software.amazon.awssdk.services.ses.model.SesException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SesHealthCheckTest extends HealthCheck {

  @Test
  void testNullConstructorArgumentThrows() {
    assertThrows(NullPointerException.class, () -> new SesHealthCheck(null));
  }

  @Test
  void testCheckHealthy() {
    SesAsyncClient client = mock(SesAsyncClient.class);
    SesHealthCheck healthCheck = new SesHealthCheck(client);

    when(client.getAccountSendingEnabled()).thenReturn(CompletableFuture.completedFuture(
        GetAccountSendingEnabledResponse.builder().enabled(true).build()));

    assertTrue(healthCheck.check()::isHealthy);
  }

  @Test
  void testCheckUnhealthy() {
    SesAsyncClient client = mock(SesAsyncClient.class);
    SesHealthCheck healthCheck = new SesHealthCheck(client);

    when(client.getAccountSendingEnabled()).thenReturn(CompletableFuture.completedFuture(
        GetAccountSendingEnabledResponse.builder().enabled(false).build()));

    assertFalse(healthCheck.check()::isHealthy);
  }

  @Test
  void testCheckUnhealthyException() {
    SesAsyncClient client = mock(SesAsyncClient.class);
    SesHealthCheck healthCheck = new SesHealthCheck(client);

    when(client.getAccountSendingEnabled())
        .thenReturn(CompletableFuture.failedFuture(mock(SesException.class)));

    assertFalse(healthCheck.check()::isHealthy);
  }

  // Not used - exists in order to extend HealthCheck
  @Override protected Result check() {
    return Result.healthy();
  }
}
