package com.sanctionco.thunder;

import com.sanctionco.thunder.authentication.ThunderAuthenticator;
import com.sanctionco.thunder.dao.DaoModule;
import com.sanctionco.thunder.dao.dynamodb.DynamoDbHealthCheck;
import com.sanctionco.thunder.dao.dynamodb.DynamoDbModule;
import com.sanctionco.thunder.email.EmailModule;
import com.sanctionco.thunder.resources.UserResource;
import com.sanctionco.thunder.resources.VerificationResource;

import dagger.Component;

import javax.inject.Singleton;

/**
 * The Dagger component object that provides access to objects that need to be
 * constructed through dependency injection.
 */
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
