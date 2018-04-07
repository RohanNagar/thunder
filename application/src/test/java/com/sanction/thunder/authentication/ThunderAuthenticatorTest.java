package com.sanction.thunder.authentication;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.basic.BasicCredentials;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ThunderAuthenticatorTest {
  private final Key key = new Key("application", "secret");
  private final List<Key> keys = Collections.singletonList(key);

  private ThunderAuthenticator authenticator;

  @Before
  public void setup() {
    authenticator = new ThunderAuthenticator(keys);
  }

  @Test
  public void testAuthenticateWithValidCredentials() {
    BasicCredentials credentials = new BasicCredentials("application", "secret");

    Optional<Key> result = Optional.empty();
    try {
      result = authenticator.authenticate(credentials);
    } catch (AuthenticationException e) {
      // This shouldn't happen, so fail the test.
      fail();
    }

    assertTrue(result.isPresent());
    assertEquals(key, result.get());
  }

  @Test
  public void testAuthenticateWithInvalidCredentials() {
    BasicCredentials credentials = new BasicCredentials("invalidApplication", "secret");

    Optional<Key> result = Optional.empty();
    try {
      result = authenticator.authenticate(credentials);
    } catch (AuthenticationException e) {
      // This shouldn't happen, so fail the test.
      fail();
    }

    assertFalse(result.isPresent());
  }
}
