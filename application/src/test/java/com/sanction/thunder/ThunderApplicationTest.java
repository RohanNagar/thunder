package com.sanction.thunder;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

import com.sanction.thunder.dao.dynamodb.DynamoDbConfiguration;
import com.sanction.thunder.dao.dynamodb.DynamoDbHealthCheck;
import com.sanction.thunder.email.EmailConfiguration;
import com.sanction.thunder.resources.UserResource;
import com.sanction.thunder.resources.VerificationResource;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ThunderApplicationTest {
  private static final Environment environment = mock(Environment.class);
  private static final JerseyEnvironment jersey = mock(JerseyEnvironment.class);
  private static final HealthCheckRegistry healthChecks = mock(HealthCheckRegistry.class);
  private static final MetricRegistry metrics = mock(MetricRegistry.class);
  private static final ThunderConfiguration config = mock(ThunderConfiguration.class);
  private static final DynamoDbConfiguration dynamoConfig = mock(DynamoDbConfiguration.class);
  private static final EmailConfiguration emailConfig = mock(EmailConfiguration.class);

  @SuppressWarnings("unchecked")
  private final Bootstrap<ThunderConfiguration> bootstrap = mock(Bootstrap.class);

  private final ThunderApplication application = new ThunderApplication();

  @BeforeAll
  static void setup() {
    when(environment.jersey()).thenReturn(jersey);
    when(environment.healthChecks()).thenReturn(healthChecks);
    when(environment.metrics()).thenReturn(metrics);

    when(dynamoConfig.getEndpoint()).thenReturn("http://localhost");
    when(dynamoConfig.getRegion()).thenReturn("us-east-1");
    when(dynamoConfig.getTableName()).thenReturn("sample-table");

    when(emailConfig.isEnabled()).thenReturn(true);
    when(emailConfig.getEndpoint()).thenReturn("http://localhost");
    when(emailConfig.getRegion()).thenReturn("us-east-1");
    when(emailConfig.getFromAddress()).thenReturn("testAddress@test.com");
    when(emailConfig.getMessageOptionsConfiguration()).thenReturn(null);

    // ThunderConfiguration NotNull fields
    when(config.getApprovedKeys()).thenReturn(new ArrayList<>());
    when(config.getDynamoConfiguration()).thenReturn(dynamoConfig);
    when(config.getEmailConfiguration()).thenReturn(emailConfig);
  }

  @AfterEach
  void reset() {
    clearInvocations(jersey, healthChecks);
  }

  @Test
  void testInitialize() {
    application.initialize(bootstrap);

    // Nothing should happen in the initialize method
  }

  @Test
  void testRun() {
    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

    application.run(config, environment);

    // Verify register was called on jersey and healthChecks
    verify(jersey, atLeastOnce()).register(captor.capture());
    verify(healthChecks, times(1)).register(eq("DynamoDB"), any(DynamoDbHealthCheck.class));

    // Make sure each class that should have been registered on jersey was registered
    List<Object> values = captor.getAllValues();

    assertAll("Assert all objects were registered to Jersey",
        () -> assertEquals(1,
            values.stream().filter(v -> v instanceof AuthDynamicFeature).count()),
        () -> assertEquals(1,
            values.stream().filter(v -> v instanceof UserResource).count()),
        () -> assertEquals(1,
            values.stream().filter(v -> v instanceof VerificationResource).count()));
  }

  @Test
  void testRunWithoutVerification() {
    when(emailConfig.isEnabled()).thenReturn(false);

    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

    application.run(config, environment);

    // Verify register was called on jersey and healthChecks
    verify(jersey, atLeastOnce()).register(captor.capture());
    verify(healthChecks, times(1)).register(eq("DynamoDB"), any(DynamoDbHealthCheck.class));

    // Make sure each class that should have been registered on jersey was registered
    List<Object> values = captor.getAllValues();

    assertAll("Assert all objects were registered to Jersey",
        () -> assertEquals(1,
            values.stream().filter(v -> v instanceof AuthDynamicFeature).count()),
        () -> assertEquals(1,
            values.stream().filter(v -> v instanceof UserResource).count()),
        () -> assertEquals(0,
            values.stream().filter(v -> v instanceof VerificationResource).count()));
  }
}
