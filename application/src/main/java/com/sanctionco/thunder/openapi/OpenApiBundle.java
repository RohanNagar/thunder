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

/**
 * A {@link io.dropwizard.ConfiguredBundle} that provides configuration of Swagger and Swagger UI
 * on top of Dropwizard.
 */
public abstract class OpenApiBundle<T extends Configuration> implements ConfiguredBundle<T> {

  @Override
  public void initialize(Bootstrap<?> bootstrap) {
    bootstrap.addBundle(new ViewBundle<Configuration>());
    ModelConverters.getInstance().addConverter(new ModelResolver(bootstrap.getObjectMapper()));
  }

  @Override
  public void run(T configuration, Environment environment) throws Exception {
    OpenApiConfiguration openApiConfiguration = getOpenApiConfiguration(configuration);

    if (openApiConfiguration == null) {
      throw new IllegalStateException(
          "You need to provide an instance of OpenApiConfiguration");
    }

    if (!openApiConfiguration.isEnabled()) {
      return;
    }

    ConfigurationHelper configurationHelper
        = new ConfigurationHelper(configuration, openApiConfiguration);

    new AssetsBundle(
        "/swagger-static", configurationHelper.getSwaggerUriPath(), null, "swagger-assets")
        .run(environment);

    new AssetsBundle(
        "/swagger-static/oauth2-redirect.html",
        configurationHelper.getOAuth2RedirectUriPath(),
        null,
        "swagger-oauth2-connect")
        .run(environment);

    final SwaggerConfiguration oasConfiguration = openApiConfiguration.build();
    new JaxrsOpenApiContextBuilder().openApiConfiguration(oasConfiguration).buildContext(true);

    environment.jersey().register(new OpenApiResource().openApiConfiguration(oasConfiguration));
    environment.jersey().register(new BackwardsCompatibleSwaggerResource());
    environment.jersey().register(new SwaggerSerializers());
    environment.jersey().register(new SwaggerResource(
        configurationHelper.getUrlPattern(),
        openApiConfiguration.getSwaggerViewConfiguration(),
        openApiConfiguration.getSwaggerOAuth2Configuration()));
  }

  protected abstract OpenApiConfiguration getOpenApiConfiguration(T configuration);
}
