package com.sanctionco.thunder.authentication.oauth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import io.dropwizard.auth.Authenticator;

import java.security.Principal;
import java.util.Objects;
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

  private final String hmacSecret;
  private final String issuer;
  private final String audience;

  private final Timer timer;
  private final Counter jwtVerificationFailureCounter;
  private final Counter jwtVerificationSuccessCounter;

  /**
   * Constructs a new instance of {@code OAuthAuthenticator}.
   *
   * @param hmacSecret the secret to use when verifying HMAC signed JWT tokens
   * @param issuer the name of the issuer to use when verifying JWT tokens
   * @param audience the name of the audience to use when verifying JWT tokens
   * @param metrics the {@code MetricRegistry} instance used to report metrics
   */
  public OAuthAuthenticator(String hmacSecret, String issuer,
                            String audience, MetricRegistry metrics) {
    this.hmacSecret = hmacSecret;
    this.issuer = issuer;
    this.audience = audience;

    timer = metrics.timer(MetricRegistry.name(
        OAuthAuthenticator.class, "jwt-verification-time"));
    jwtVerificationFailureCounter = metrics.counter(MetricRegistry.name(
        OAuthAuthenticator.class, "jwt-verification-failure"));
    jwtVerificationSuccessCounter = metrics.counter(MetricRegistry.name(
        OAuthAuthenticator.class, "jwt-verification-success"));
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

    if (Objects.isNull(token)) {
      return validationFailure(context);
    }

    // Decode the token
    DecodedJWT jwt;
    try {
      jwt = JWT.decode(token);
    } catch (JWTDecodeException e) {
      LOG.warn("Unable to decode JWT token: {}", token, e);

      return validationFailure(context);
    }

    // Determine the algorithm
    Algorithm algorithm = getAlgorithm(jwt.getAlgorithm());

    if (Objects.isNull(algorithm)) {
      LOG.warn("Unable to determine algorithm ({}) from JWT token header.", jwt.getAlgorithm());

      return validationFailure(context);
    }

    // Verify the token
    try {
      JWTVerifier verifier = JWT.require(algorithm)
          .withIssuer(issuer)
          .withAudience(audience)
          .build();

      verifier.verify(jwt);
    } catch (JWTVerificationException e) {
      LOG.warn("JWT token failed verification. Token: {}", token, e);

      return validationFailure(context);
    }

    // If we successfully verified, return the authenticated actor
    context.stop();
    jwtVerificationSuccessCounter.inc();

    return Optional.of(new OAuthPrincipal(jwt.getSubject()));
  }

  /**
   * Determines the correct algorithm to use based on the string.
   *
   * @param algorithm the algorithm string to parse
   * @return the associated {@code Algorithm} object, or {@code null} if unknown
   */
  private Algorithm getAlgorithm(String algorithm) {
    return switch (algorithm) {
      case "HS256" -> Algorithm.HMAC256(hmacSecret);
      case "HS384" -> Algorithm.HMAC384(hmacSecret);
      case "HS512" -> Algorithm.HMAC512(hmacSecret);
      default -> null;
    };
  }

  /**
   * Stops the given timer and updates JWT failure metrics, returning {@code Optional.empty()}.
   *
   * @param context the current running timer to stop
   * @return an empty {@code Optional}
   */
  private Optional<Principal> validationFailure(Timer.Context context) {
    context.stop();
    jwtVerificationFailureCounter.inc();

    return Optional.empty();
  }
}
