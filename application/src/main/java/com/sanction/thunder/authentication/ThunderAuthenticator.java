package com.sanction.thunder.authentication;

import com.google.common.base.Optional;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

import io.dropwizard.auth.basic.BasicCredentials;

import java.util.List;

public class ThunderAuthenticator implements Authenticator<BasicCredentials, Key> {

  private final List<Key> allKeys;

  public ThunderAuthenticator(List<Key> keys) {
    this.allKeys = keys;
  }

  @Override
  public Optional<Key> authenticate(BasicCredentials credentials) throws AuthenticationException {
    // Construct a key from incoming credentials
    Key key = new Key(credentials.getUsername(), credentials.getPassword());

    // Check if that key exists in the list of approved keys
    if (!allKeys.contains(key)) {
      return Optional.absent();
    }

    return Optional.of(key);
  }
}
