package com.sanctionco.thunder.authentication.basic;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides the Basic auth implementation for Dropwizard authentication. Provides a method to
 * authenticate an incoming request. Can be registered with Jersey as an authenticator for
 * basic authentication on resource methods. See {@code Authenticator} in the
 * {@code io.dropwizard.auth} module for information on the base interface.
 */
public class BasicAuthenticator implements Authenticator<BasicCredentials, Principal> {
  private final List<Key> allKeys;

  private final Timer timer;
  private final Counter basicAuthVerificationFailureCounter;
  private final Counter basicAuthVerificationSuccessCounter;

  /**
   * Constructs a new {@code BasicAuthenticator} with the given approved keys.
   *
   * @param allKeys the keys that are approved to access protected resources
   * @param metrics the {@code MetricRegistry} instance used to set up metrics
   */
  public BasicAuthenticator(List<Key> allKeys, MetricRegistry metrics) {
    this.allKeys = Objects.requireNonNull(allKeys);

    timer = metrics.timer(MetricRegistry.name(
        BasicAuthenticator.class, "basic-auth-verification-time"));
    basicAuthVerificationFailureCounter = metrics.counter(MetricRegistry.name(
        BasicAuthenticator.class, "basic-auth-verification-failure"));
    basicAuthVerificationSuccessCounter = metrics.counter(MetricRegistry.name(
        BasicAuthenticator.class, "basic-auth-verification-success"));
  }

  /**
   * Determines if the basic credentials are approved.
   *
   * @param credentials the credentials of an incoming request
   * @return the key if the credentials are approved or an empty {@code Optional} if they are not
   */
  @Override
  public Optional<Principal> authenticate(BasicCredentials credentials) {
    try (Timer.Context cxt = timer.time()) {
      // Check for null argument
      if (Objects.isNull(credentials)) {
        basicAuthVerificationFailureCounter.inc();
        return Optional.empty();
      }

      // Construct a key from incoming credentials
      Key key = new Key(credentials.getUsername(), credentials.getPassword());

      // Check if that key exists in the list of approved keys
      if (!allKeys.contains(key)) {
        basicAuthVerificationFailureCounter.inc();
        return Optional.empty();
      }

      basicAuthVerificationSuccessCounter.inc();
      return Optional.of(key);
    }
  }
}
