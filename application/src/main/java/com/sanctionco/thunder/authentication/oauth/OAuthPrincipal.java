package com.sanctionco.thunder.authentication.oauth;

import java.security.Principal;

/**
 * Represents an OAuth authentication principal used to authenticate requests to the API, providing
 * access to the name (the OAuth subject). This object should be used as the Dropwizard
 * {@code @Auth} parameter to protected methods on a resource. See
 * <a href=https://www.dropwizard.io/1.3.5/docs/manual/auth.html>the Dropwizard manual</a>
 * for more information on Dropwizard authentication.
 *
 * @param name the name of the authenticated actor
 * @see OAuthAuthenticator
 */
public record OAuthPrincipal(String name) implements Principal {
  @Override
  public String getName() {
    return name();
  }
}
