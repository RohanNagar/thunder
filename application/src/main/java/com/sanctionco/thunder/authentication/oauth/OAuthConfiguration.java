package com.sanctionco.thunder.authentication.oauth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sanctionco.thunder.authentication.AuthConfiguration;
import com.sanctionco.thunder.util.FileUtilities;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.setup.Environment;
import io.dropwizard.validation.ValidationMethod;

import java.security.Principal;
import java.security.interfaces.RSAPublicKey;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the OAuth2 Authentication implementation of {@link AuthConfiguration}.
 */
@JsonTypeName("oauth")
@SuppressWarnings("ConstantConditions")
public class OAuthConfiguration implements AuthConfiguration {
  private static final Logger LOG = LoggerFactory.getLogger(OAuthConfiguration.class);

  @NotEmpty @Valid @JsonProperty("issuer")
  private final String issuer = null;

  @Valid @JsonProperty("audience")
  private final String audience = null;

  @Valid @JsonProperty("hmacSecret")
  private final String hmacSecret = null;

  @Valid @JsonProperty("rsaPublicKeyFilePath")
  private final String rsaPublicKeyFilePath = null;

  public String getIssuer() {
    return issuer;
  }

  public String getAudience() {
    return audience;
  }

  public String getHmacSecret() {
    return hmacSecret;
  }

  public RSAPublicKey getRsaPublicKey() {
    return Optional.ofNullable(rsaPublicKeyFilePath)
        .map(FileUtilities::readPublicKeyFromPath)
        .orElse(null);
  }

  /**
   * Validates the email configuration to ensure the configuration is correctly set.
   *
   * @return {@code true} if validation is successful; {@code false} otherwise
   */
  @JsonIgnore
  @ValidationMethod(message = """
      At least one of either hmacSecret or rsaPublicKeyFilePath must be set for OAuth.""")
  public boolean hasKeysConfigured() {
    return hmacSecret != null || rsaPublicKeyFilePath != null;
  }

  @Override
  public void registerAuthentication(Environment environment) {
    LOG.info("Setting up OAuth authentication with issuer {} and audience {}", issuer, audience);

    var rsaKey = getRsaPublicKey();

    if (Objects.isNull(hmacSecret)) {
      LOG.warn("HMAC secret is null, verification of HMAC signed JWT tokens will fail.");
    }

    if (Objects.isNull(rsaKey)) {
      LOG.warn("RSA public key is null, verification of RSA signed JWT tokens will fail.");
    }

    var authenticator = new OAuthAuthenticator(
        hmacSecret, issuer, audience, rsaKey, environment.metrics());

    environment.jersey().register(new AuthDynamicFeature(
        new OAuthCredentialAuthFilter.Builder<>()
            .setAuthenticator(authenticator)
            .setPrefix("Bearer")
            .buildAuthFilter()));

    environment.jersey().register(new AuthValueFactoryProvider.Binder<>(Principal.class));
  }
}
