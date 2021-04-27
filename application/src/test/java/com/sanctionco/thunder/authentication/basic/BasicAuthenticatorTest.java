package com.sanctionco.thunder.authentication.basic;

import com.codahale.metrics.MetricRegistry;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BasicAuthenticatorTest {
  private static final Key KEY = new Key("application", "secret");
  private static final List<Key> KEYS = Collections.singletonList(KEY);

  private final BasicAuthenticator authenticator
      = new BasicAuthenticator(KEYS, new MetricRegistry());

  @Test
  void testNullConstructorArgumentThrows() {
    assertThrows(NullPointerException.class,
        () -> new BasicAuthenticator(null, new MetricRegistry()));
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

  @Test
  void testAuthenticateWithExceptionThrown() {
    var keys = mock(List.class);

    when(keys.contains(any())).thenThrow(RuntimeException.class);

    var basicAuthenticator = new BasicAuthenticator(keys, TestResources.METRICS);

    assertThrows(RuntimeException.class,
        () -> basicAuthenticator.authenticate(new BasicCredentials("name", "pass")));
  }
}
