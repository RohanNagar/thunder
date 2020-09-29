package com.sanctionco.thunder.authentication.basic;

import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

/**
 * Provides the Thunder implementation for Dropwizard authentication. Provides a method to
 * authenticate an incoming request. Can be registered with Jersey as an authenticator for
 * basic authentication on resource methods. See {@code Authenticator} in the
 * {@code io.dropwizard.auth} module for information on the base interface.
 */
public class ThunderAuthenticator implements Authenticator<BasicCredentials, Key> {
  private final List<Key> allKeys;

  /**
   * Constructs a new {@code ThunderAuthenticator} with the given keys.
   *
   * @param allKeys the keys that are approved to access protected resources
   */
  @Inject
  public ThunderAuthenticator(List<Key> allKeys) {
    this.allKeys = Objects.requireNonNull(allKeys);
  }

  /**
   * Determines if the basic credentials are approved.
   *
   * @param credentials the credentials of an incoming request
   * @return the key if the credentials are approved or an empty {@code Optional} if they are not
   */
  @Override
  public Optional<Key> authenticate(BasicCredentials credentials) {
    // Check for null argument
    if (Objects.isNull(credentials)) {
      return Optional.empty();
    }

    // Construct a key from incoming credentials
    Key key = new Key(credentials.getUsername(), credentials.getPassword());

    // Check if that key exists in the list of approved keys
    return allKeys.contains(key)
        ? Optional.of(key)
        : Optional.empty();
  }
}
