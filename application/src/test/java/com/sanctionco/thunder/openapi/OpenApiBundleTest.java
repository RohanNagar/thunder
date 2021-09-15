package com.sanctionco.thunder.openapi;

import com.sanctionco.thunder.ThunderConfiguration;

import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.swagger.v3.jaxrs2.SwaggerSerializers;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;

import java.util.List;
import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpenApiBundleTest {
  private static final ServletEnvironment SERVLET = mock(ServletEnvironment.class);
  private static final ThunderConfiguration CONFIG = mock(ThunderConfiguration.class);

  @BeforeAll
  static void setup() {
    when(SERVLET.addServlet(any(String.class), any(Servlet.class)))
        .thenReturn(mock(ServletRegistration.Dynamic.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  void testInitialize() {
    Bootstrap<ThunderConfiguration> bootstrap = mock(Bootstrap.class);
    when(bootstrap.getObjectMapper()).thenReturn(Jackson.newObjectMapper());

    OpenApiBundle<ThunderConfiguration> bundle = new OpenApiBundle<>() {
      @Override
      public OpenApiConfiguration getOpenApiConfiguration(ThunderConfiguration configuration) {
        return new OpenApiConfiguration();
      }
    };

    bundle.initialize(bootstrap);

    // Verify ViewBundle was added
    verify(bootstrap).addBundle(any(ViewBundle.class));
  }

  @Test
  void testRun() throws Exception {
    var environment = mock(Environment.class);
    var jersey = mock(JerseyEnvironment.class);

    when(environment.jersey()).thenReturn(jersey);
    when(environment.servlets()).thenReturn(SERVLET);

    OpenApiBundle<ThunderConfiguration> bundle = new OpenApiBundle<>() {
      @Override
      public OpenApiConfiguration getOpenApiConfiguration(ThunderConfiguration configuration) {
        return new OpenApiConfiguration();
      }
    };

    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

    bundle.run(CONFIG, environment);

    // Verify register was called on jersey and healthChecks
    verify(jersey, times(3)).register(captor.capture());

    // Make sure each class that should have been registered on jersey was registered
    List<Object> values = captor.getAllValues();

    assertAll("Assert all objects were registered to Jersey",
        () -> assertEquals(1,
            values.stream().filter(v -> v instanceof OpenApiResource).count()),
        () -> assertEquals(1,
            values.stream().filter(v -> v instanceof SwaggerSerializers).count()),
        () -> assertEquals(1,
            values.stream().filter(v -> v instanceof SwaggerResource).count()));
  }

  @Test
  void testRunDisabled() throws Exception {
    var environment = mock(Environment.class);
    var jersey = mock(JerseyEnvironment.class);

    when(environment.jersey()).thenReturn(jersey);
    when(environment.servlets()).thenReturn(SERVLET);

    OpenApiConfiguration mockedConfiguration = mock(OpenApiConfiguration.class);
    when(mockedConfiguration.isEnabled()).thenReturn(false);

    OpenApiBundle<ThunderConfiguration> bundle = new OpenApiBundle<>() {
      @Override
      public OpenApiConfiguration getOpenApiConfiguration(ThunderConfiguration configuration) {
        return mockedConfiguration;
      }
    };

    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

    bundle.run(CONFIG, environment);

    // Verify register was never called
    verify(jersey, never()).register(captor.capture());

    List<Object> values = captor.getAllValues();
    assertEquals(0, values.size());
  }

  @Test
  void testRunNullConfiguration() {
    var environment = mock(Environment.class);
    var jersey = mock(JerseyEnvironment.class);

    when(environment.jersey()).thenReturn(jersey);
    when(environment.servlets()).thenReturn(SERVLET);

    OpenApiBundle<ThunderConfiguration> bundle = new OpenApiBundle<>() {
      @Override
      public OpenApiConfiguration getOpenApiConfiguration(ThunderConfiguration configuration) {
        return null;
      }
    };

    assertThrows(IllegalStateException.class, () -> bundle.run(CONFIG, environment));
  }
}
