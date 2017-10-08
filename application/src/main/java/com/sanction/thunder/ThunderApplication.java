package com.sanction.thunder;

import com.sanction.thunder.authentication.Key;
import com.sanction.thunder.dao.DaoModule;
import com.sanction.thunder.dynamodb.DynamoDbModule;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

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
        .daoModule(new DaoModule())
        .dynamoDbModule(new DynamoDbModule(config.getDynamoTableName()))
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
  }
}
