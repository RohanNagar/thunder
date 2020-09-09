package com.sanctionco.thunder.email.ses;

import com.codahale.metrics.health.HealthCheck;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.GetAccountSendingEnabledResponse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SesHealthCheckTest extends HealthCheck {

  @Test
  void testNullConstructorArgumentThrows() {
    assertThrows(NullPointerException.class,
        () -> new SesHealthCheck(null));
  }

  @Test
  void testCheckHealthy() {
    SesClient client = mock(SesClient.class);
    SesHealthCheck healthCheck = new SesHealthCheck(client);

    when(client.getAccountSendingEnabled()).thenReturn(
        GetAccountSendingEnabledResponse.builder().enabled(true).build());

    assertTrue(healthCheck.check()::isHealthy);
  }

  @Test
  void testCheckUnhealthy() {
    SesClient client = mock(SesClient.class);
    SesHealthCheck healthCheck = new SesHealthCheck(client);

    when(client.getAccountSendingEnabled()).thenReturn(
        GetAccountSendingEnabledResponse.builder().enabled(false).build());

    assertFalse(healthCheck.check()::isHealthy);
  }

  // Not used - exists in order to extend HealthCheck
  @Override protected Result check() {
    return Result.healthy();
  }
}
