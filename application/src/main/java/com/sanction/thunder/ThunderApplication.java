package com.sanction.thunder;

import com.sanction.thunder.authentication.Key;
import com.sanction.thunder.authentication.ThunderAuthenticator;
import com.sanction.thunder.dao.DaoModule;
import com.sanction.thunder.dynamodb.DynamoDbModule;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.basic.BasicAuthFactory;
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
        .dynamoDbModule(new DynamoDbModule(config))
        .thunderModule(new ThunderModule(env.metrics()))
        .build();

    env.jersey().register(AuthFactory.binder(new BasicAuthFactory<>(new ThunderAuthenticator(
        config.getApprovedKeys()), "THUNDER - AUTHENTICATION", Key.class)));

    env.jersey().register(component.getUserResource());
  }
}
