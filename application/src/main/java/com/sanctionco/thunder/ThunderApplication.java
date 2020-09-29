package com.sanctionco.thunder;

import com.sanctionco.thunder.dao.DaoModule;
import com.sanctionco.thunder.email.EmailModule;
import com.sanctionco.thunder.openapi.OpenApiBundle;
import com.sanctionco.thunder.openapi.OpenApiConfiguration;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Starts up the Thunder application. The run method will add resources, health checks,
 * and authenticators to the Jersey servlet in order to start the application. See
 * {@code Application} in the {@code io.dropwizard} module for details on the base class. Also see
 * <a href="https://www.dropwizard.io/1.3.5/docs/manual/core.html#application">the Dropwizard
 * manual</a> to learn more.
 *
 * @see com.sanctionco.thunder.resources.UserResource UserResource
 * @see com.sanctionco.thunder.resources.VerificationResource VerificationResource
 */
public class ThunderApplication extends Application<ThunderConfiguration> {

  public static void main(String[] args) throws Exception {
    new ThunderApplication().run(args);
  }

  @Override
  public void initialize(Bootstrap<ThunderConfiguration> bootstrap) {
    bootstrap.addBundle(new OpenApiBundle<ThunderConfiguration>() {
      @Override
      public OpenApiConfiguration getOpenApiConfiguration(ThunderConfiguration configuration) {
        return configuration.getOpenApiConfiguration();
      }
    });
  }

  @Override
  public void run(ThunderConfiguration config, Environment env) {
    ThunderComponent component = DaggerThunderComponent.builder()
        .daoModule(new DaoModule(config.getUsersDaoFactory()))
        .emailModule(new EmailModule(config.getEmailServiceFactory()))
        .thunderModule(new ThunderModule(env.metrics(), config))
        .build();

    // Authentication
    config.getAuthConfiguration().registerAuthentication(env);

    // HealthChecks
    env.healthChecks().register("Database", component.getDatabaseHealthCheck());

    // Resources
    env.jersey().register(component.getUserResource());

    // Only register verification resource if emails are enabled
    if (config.getEmailServiceFactory().isEnabled()) {
      env.jersey().register(component.getVerificationResource());
      env.healthChecks().register("Email", component.getEmailHealthCheck());
    }
  }
}
