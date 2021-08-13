package com.sanctionco.thunder.authentication.oauth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.sanctionco.thunder.util.MetricNameUtil;

import io.dropwizard.auth.Authenticator;

import java.security.Principal;
import java.security.interfaces.RSAPublicKey;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an OAuth implementation for Dropwizard authentication. Provides a method to
 * authenticate an incoming request based on a JWT token. Can be registered with Jersey as an
 * authenticator for OAuth authentication on resource methods. See {@code Authenticator} in the
 * {@code io.dropwizard.auth} module for information on the base interface.
 */
public class OAuthAuthenticator implements Authenticator<String, Principal> {
  private static final Logger LOG = LoggerFactory.getLogger(OAuthAuthenticator.class);

  private final String issuer;
  private final String audience;
  private final String hmacSecret;
  private final RSAPublicKey rsaKey;

  private final Timer timer;
  private final Counter jwtVerificationFailureCounter;
  private final Counter jwtVerificationSuccessCounter;

  /**
   * Constructs a new instance of {@code OAuthAuthenticator}.
   *
   * @param hmacSecret the secret to use when verifying HMAC signed JWT tokens
   * @param issuer the name of the issuer to use when verifying JWT tokens
   * @param audience the name of the audience to use when verifying JWT tokens
   * @param rsaKey the RSAPublicKey to use when verifying RSA signed JWT tokens
   * @param metrics the {@code MetricRegistry} instance used to report metrics
   */
  public OAuthAuthenticator(String hmacSecret, String issuer,
                            String audience, RSAPublicKey rsaKey, MetricRegistry metrics) {
    this.issuer = issuer;
    this.audience = audience;
    this.hmacSecret = hmacSecret;
    this.rsaKey = rsaKey;

    timer = metrics.timer(MetricNameUtil.OAUTH_AUTH_TIMER);
    jwtVerificationFailureCounter = metrics.counter(MetricNameUtil.OAUTH_AUTH_FAILURES);
    jwtVerificationSuccessCounter = metrics.counter(MetricNameUtil.OAUTH_AUTH_SUCCESSES);
  }

  /**
   * Determines if the JWT token is valid.
   *
   * @param token the JWT token to validate
   * @return an {@link OAuthPrincipal} containing the subject claim from the JWT token if the token
   *     is valid, or an empty {@code Optional} if the token is invalid
   */
  @Override
  public Optional<Principal> authenticate(String token) {
    Timer.Context context = timer.time(); // start the timer

    Optional<Principal> result = Optional.ofNullable(token)
        .map(tok -> {
          try {
            return JWT.decode(tok);
          } catch (JWTDecodeException e) {
            LOG.warn("Unable to decode JWT token: {}", token, e);
            return null;
          }
        })
        .map(jwt -> getAlgorithm(jwt.getAlgorithm()))
        .map(algorithm -> {
          try {
            JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .withAudience(audience)
                .build();

            return verifier.verify(token);
          } catch (JWTVerificationException e) {
            LOG.warn("JWT token failed verification. Token: {}", token, e);
            return null;
          }
        })
        .map(jwt -> new OAuthPrincipal(jwt.getSubject()));

    result.ifPresentOrElse(principal -> {
      context.stop();
      jwtVerificationSuccessCounter.inc();
    }, () -> {
      context.stop();
      jwtVerificationFailureCounter.inc();
    });

    return result;
  }

  /**
   * Determines the correct algorithm to use based on the string.
   *
   * @param algorithm the algorithm string to parse
   * @return the associated {@code Algorithm} object, or {@code null} if unknown
   */
  private Algorithm getAlgorithm(String algorithm) {
    try {
      return switch (algorithm) {
        case "HS256" -> Algorithm.HMAC256(hmacSecret);
        case "HS384" -> Algorithm.HMAC384(hmacSecret);
        case "HS512" -> Algorithm.HMAC512(hmacSecret);
        case "RS256" -> Algorithm.RSA256(rsaKey, null);
        case "RS384" -> Algorithm.RSA384(rsaKey, null);
        case "RS512" -> Algorithm.RSA512(rsaKey, null);
        default -> null;
      };
    } catch (IllegalArgumentException e) {
      LOG.error("Unable to instantiate algorithm {}. The likely scenario is we received a JWT "
          + "token signed with an algorithm that was not configured on startup.", algorithm);

      // Return the none algorithm, which will end up causing verification failure.
      // We want to differentiate this result with an unsupported algorithm (which returns null)
      return Algorithm.none();
    }
  }
}
