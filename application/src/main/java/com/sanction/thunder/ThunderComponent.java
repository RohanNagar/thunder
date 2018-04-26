package com.sanction.thunder;

import com.sanction.thunder.authentication.ThunderAuthenticator;
import com.sanction.thunder.dao.DaoModule;
import com.sanction.thunder.dao.dynamodb.DynamoDbHealthCheck;
import com.sanction.thunder.dao.dynamodb.DynamoDbModule;
import com.sanction.thunder.email.EmailModule;
import com.sanction.thunder.resources.UserResource;
import com.sanction.thunder.resources.VerificationResource;

import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {DaoModule.class,
                      DynamoDbModule.class,
                      EmailModule.class,
                      ThunderModule.class})
public interface ThunderComponent {

  // Resources
  UserResource getUserResource();

  VerificationResource getVerificationResource();

  // HealthChecks
  DynamoDbHealthCheck getDynamoDbHealthCheck();

  // ThunderAuthenticator
  ThunderAuthenticator getThunderAuthenticator();
}
