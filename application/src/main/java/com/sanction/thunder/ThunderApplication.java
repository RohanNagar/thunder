package com.sanction.thunder;

import com.sanction.thunder.dao.DaoModule;
import com.sanction.thunder.dynamodb.DynamoDbModule;
import io.dropwizard.Application;
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
        .thunderModule(new ThunderModule())
        .build();

    env.jersey().register(component.getUserResource());
  }
}
