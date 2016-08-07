package com.sanction.thunder.authentication;

import com.google.common.collect.Lists;
import com.sanction.thunder.ThunderConfiguration;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.basic.BasicCredentials;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ThunderAuthenticatorTest {
  private final Key key = new Key("application", "secret");
  private final List<Key> keys = Lists.newArrayList(key);
  private final ThunderConfiguration config = mock(ThunderConfiguration.class);

  private ThunderAuthenticator authenticator;

  @Before
  public void setup() {
    when(config.getApprovedKeys()).thenReturn(keys);

    authenticator = new ThunderAuthenticator(config);
  }

  @Test
  public void testAuthenticateWithValidCredentials() {
    BasicCredentials credentials = new BasicCredentials("application", "secret");
    when(config.getApprovedKeys()).thenReturn(keys);

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
    when(config.getApprovedKeys()).thenReturn(keys);

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
