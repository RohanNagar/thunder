package com.sanctionco.thunder.authentication.basic;

import com.sanctionco.thunder.TestResources;

import io.dropwizard.auth.basic.BasicCredentials;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BasicAuthenticatorTest {
  private static final Key KEY = new Key("application", "secret");
  private static final List<Key> KEYS = Collections.singletonList(KEY);

  private final BasicAuthenticator authenticator
      = new BasicAuthenticator(KEYS, TestResources.METRICS);

  @Test
  void testNullConstructorArgumentThrows() {
    assertThrows(NullPointerException.class,
        () -> new BasicAuthenticator(null, TestResources.METRICS));
  }

  @Test
  @SuppressWarnings("OptionalGetWithoutIsPresent")
  void testAuthenticateWithValidCredentials() {
    BasicCredentials credentials = new BasicCredentials("application", "secret");

    Optional<Principal> result = authenticator.authenticate(credentials);

    assertAll("Assert authentication success",
        () -> assertTrue(result::isPresent),
        () -> assertTrue(result.get() instanceof Key),
        () -> assertEquals(KEY, result.get()));
  }

  @Test
  void testAuthenticateWithNullCredentials() {
    Optional<Principal> result = authenticator.authenticate(null);

    assertFalse(result::isPresent);
  }

  @Test
  void testAuthenticateWithInvalidCredentials() {
    BasicCredentials credentials = new BasicCredentials("invalidApplication", "secret");

    Optional<Principal> result = authenticator.authenticate(credentials);

    assertFalse(result::isPresent);
  }
}
