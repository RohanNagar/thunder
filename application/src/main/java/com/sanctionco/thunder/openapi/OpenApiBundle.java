package com.sanctionco.thunder.openapi;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.jaxrs2.SwaggerSerializers;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link io.dropwizard.ConfiguredBundle} that provides configuration of Swagger and Swagger UI
 * on top of Dropwizard.
 *
 * <p>Code originally taken from <a href="https://github.com/smoketurner/dropwizard-swagger">
 *   Dropwizard Swagger</a>, with modifications for this project.
 *
 * @param <T> the custom configuration type for the application that is using the bundle
 */
public abstract class OpenApiBundle<T extends Configuration> implements ConfiguredBundle<T> {
  private static final Logger LOG = LoggerFactory.getLogger(OpenApiBundle.class);

  @Override
  public void initialize(Bootstrap<?> bootstrap) {
    bootstrap.addBundle(new ViewBundle<Configuration>());
    ModelConverters.getInstance().addConverter(new ModelResolver(bootstrap.getObjectMapper()));
  }

  @Override
  public void run(T configuration, Environment environment) throws Exception {
    OpenApiConfiguration openApiConfiguration = getOpenApiConfiguration(configuration);

    if (openApiConfiguration == null) {
      LOG.error("Cannot instantiate OpenApiBundle. Missing OpenApiConfiguration.");
      throw new IllegalStateException("You need to provide an instance of OpenApiConfiguration");
    }

    if (!openApiConfiguration.isEnabled()) {
      LOG.info("OpenAPI/Swagger is disabled.");
      return;
    }

    // Create the assets bundle used to serve the static Swagger assets
    new AssetsBundle("/swagger-static", "/swagger-static", null, "swagger-assets")
        .run(configuration, environment);

    // Build the OpenAPI configuration
    SwaggerConfiguration oasConfiguration = openApiConfiguration.build();
    new JaxrsOpenApiContextBuilder().openApiConfiguration(oasConfiguration).buildContext(true);

    // Register the OpenAPI and Swagger resources
    LOG.info("Registering OpenAPI and Swagger resources.");
    environment.jersey().register(new OpenApiResource().openApiConfiguration(oasConfiguration));
    environment.jersey().register(new SwaggerSerializers());
    environment.jersey().register(new SwaggerResource());
  }

  /**
   * Returns the {@link OpenApiConfiguration} object used to configure the bundle.
   *
   * @param configuration the configuration object that holds the OpenApiConfiguration
   * @return the OpenApiConfiguration object
   */
  public abstract OpenApiConfiguration getOpenApiConfiguration(T configuration);
}
