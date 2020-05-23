package com.sanctionco.thunder;

import com.sanctionco.thunder.authentication.ThunderAuthenticator;
import com.sanctionco.thunder.dao.DaoModule;
import com.sanctionco.thunder.dao.DatabaseHealthCheck;
import com.sanctionco.thunder.email.EmailModule;
import com.sanctionco.thunder.resources.UserResource;
import com.sanctionco.thunder.resources.VerificationResource;

import dagger.Component;

import javax.inject.Singleton;

/**
 * Provides access to objects that need to be constructed through dependency injection. The
 * {@code Component} is a Dagger concept that uses multiple {@code Module} classes, including
 * {@link DaoModule}, {@link EmailModule}, and {@link ThunderModule}.
 * See {@code Component} in the {@code dagger} module for more information.
 */
@Singleton
@Component(modules = {DaoModule.class,
                      EmailModule.class,
                      ThunderModule.class})
public interface ThunderComponent {

  // Resources
  UserResource getUserResource();

  VerificationResource getVerificationResource();

  // HealthChecks
  DatabaseHealthCheck getDatabaseHealthCheck();

  // ThunderAuthenticator
  ThunderAuthenticator getThunderAuthenticator();
}
