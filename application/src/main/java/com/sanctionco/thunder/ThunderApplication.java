package com.sanctionco.thunder;

import com.sanctionco.thunder.authentication.Key;
import com.sanctionco.thunder.dao.DaoModule;
import com.sanctionco.thunder.dao.dynamodb.DynamoDbModule;
import com.sanctionco.thunder.email.EmailModule;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
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

  }

  @Override
  public void run(ThunderConfiguration config, Environment env) {
    ThunderComponent component = DaggerThunderComponent.builder()
        .daoModule(new DaoModule(config.getDynamoConfiguration().getTableName()))
        .dynamoDbModule(new DynamoDbModule(config.getDynamoConfiguration()))
        .emailModule(new EmailModule(config.getEmailConfiguration()))
        .thunderModule(new ThunderModule(env.metrics(), config))
        .build();

    // Authentication
    env.jersey().register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<Key>()
            .setAuthenticator(component.getThunderAuthenticator())
            .setRealm("THUNDER - AUTHENTICATION")
            .buildAuthFilter()));

    env.jersey().register(new AuthValueFactoryProvider.Binder<>(Key.class));

    // HealthChecks
    env.healthChecks().register("DynamoDB", component.getDynamoDbHealthCheck());

    // Resources
    env.jersey().register(component.getUserResource());

    // Only register verification resource if it is enabled
    if (config.getEmailConfiguration().isEnabled()) {
      env.jersey().register(component.getVerificationResource());
    }
  }
}
