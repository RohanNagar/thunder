package com.sanction.thunder;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

import com.sanction.thunder.dynamodb.DynamoDbConfiguration;
import com.sanction.thunder.dynamodb.DynamoDbHealthCheck;
import com.sanction.thunder.email.EmailConfiguration;
import com.sanction.thunder.resources.UserResource;

import com.sanction.thunder.resources.VerificationResource;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ThunderApplicationTest {
  private final Environment environment = mock(Environment.class);
  private final JerseyEnvironment jersey = mock(JerseyEnvironment.class);
  private final HealthCheckRegistry healthChecks = mock(HealthCheckRegistry.class);
  private final MetricRegistry metrics = mock(MetricRegistry.class);
  private final ThunderConfiguration config = mock(ThunderConfiguration.class);
  private final DynamoDbConfiguration dynamoConfig = mock(DynamoDbConfiguration.class);
  private final EmailConfiguration emailConfig = mock(EmailConfiguration.class);

  private final ThunderApplication application = new ThunderApplication();

  @Before
  public void setup() {
    when(environment.jersey()).thenReturn(jersey);
    when(environment.healthChecks()).thenReturn(healthChecks);
    when(environment.metrics()).thenReturn(metrics);

    when(dynamoConfig.getEndpoint()).thenReturn("http://localhost");
    when(dynamoConfig.getRegion()).thenReturn("us-east-1");
    when(dynamoConfig.getTableName()).thenReturn("sample-table");

    when(emailConfig.getEndpoint()).thenReturn("http://localhost");
    when(emailConfig.getRegion()).thenReturn("us-east-1");

    // ThunderConfiguration NotNull fields
    when(config.getApprovedKeys()).thenReturn(new ArrayList<>());
    when(config.getDynamoConfiguration()).thenReturn(dynamoConfig);
    when(config.getEmailConfiguration()).thenReturn(emailConfig);
  }

  @Test
  public void testRun() {
    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

    application.run(config, environment);

    // Verify register was called on jersey and healthChecks
    verify(jersey, atLeastOnce()).register(captor.capture());
    verify(healthChecks, times(1)).register(eq("DynamoDB"), any(DynamoDbHealthCheck.class));

    // Make sure each class that should have been registered on jersey was registered
    List<Object> values = captor.getAllValues();

    assertEquals(1, values.stream().filter(v -> v instanceof AuthDynamicFeature).count());
    assertEquals(1, values.stream().filter(v -> v instanceof UserResource).count());
    assertEquals(1, values.stream().filter(v -> v instanceof VerificationResource).count());
  }
}
