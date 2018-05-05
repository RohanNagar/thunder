package com.sanction.thunder.authentication;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

public class ThunderAuthenticator implements Authenticator<BasicCredentials, Key> {
  private final List<Key> allKeys;

  @Inject
  public ThunderAuthenticator(List<Key> allKeys) {
    this.allKeys = Objects.requireNonNull(allKeys);
  }

  @Override
  public Optional<Key> authenticate(BasicCredentials credentials) throws AuthenticationException {
    // Construct a key from incoming credentials
    Key key = new Key(credentials.getUsername(), credentials.getPassword());

    // Check if that key exists in the list of approved keys
    if (!allKeys.contains(key)) {
      return Optional.empty();
    }

    return Optional.of(key);
  }
}
