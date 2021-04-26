package com.sanctionco.thunder.authentication.oauth;

import com.codahale.metrics.MetricRegistry;

import java.security.Principal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OAuthAuthenticatorTest {
  private static final String SECRET = "qwertyuiopasdfghjklzxcvbnm123456";
  private static final String ISSUER = "thunder-oauth-issuer";
  private static final String AUDIENCE = "thunder";
  private static final MetricRegistry METRICS = new MetricRegistry();

  @Test
  void nullTokenFailsAuthentication() {
    var authenticator = new OAuthAuthenticator(SECRET, ISSUER, AUDIENCE, METRICS);

    assertTrue(authenticator.authenticate(null).isEmpty());
  }

  @Test
  void invalidFormatTokenFailsAuthentication() {
    var authenticator = new OAuthAuthenticator(SECRET, ISSUER, AUDIENCE, METRICS);

    assertTrue(authenticator.authenticate("thistokenisnotright").isEmpty());
  }

  @Test
  void unrecognizedAlgorithmFailsAuthentication() {
    var authenticator = new OAuthAuthenticator(SECRET, ISSUER, AUDIENCE, METRICS);
    var token = "eyJhbGciOiJQUzM4NCIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4"
        + "gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.MqF1AKsJkijKnfqEI3VA1OnzAL2S4eIpAuievMg"
        + "D3tEFyFMU67gCbg-fxsc5dLrxNwdZEXs9h0kkicJZ70mp6p5vdv-j2ycDKBWg05Un4OhEl7lYcdIsCsB8QUPmstF"
        + "-lQWnNqnq3wra1GynJrOXDL27qIaJnnQKlXuayFntBF0j-82jpuVdMaSXvk3OGaOM-7rCRsBcSPmocaAO-uWJEGP"
        + "w_OWVaC5RRdWDroPi4YL4lTkDEC-KEvVkqCnFm_40C-T_siXquh5FVbpJjb3W2_YvcqfDRj44TsRrpVhk6ohsHMN"
        + "eUad_cxnFnpolIKnaXq_COv35e9EgeQIPAbgIeg";

    assertTrue(authenticator.authenticate(token).isEmpty());
  }

  @Test
  void expiredTokenFailsAuthentication() {
    var authenticator = new OAuthAuthenticator(SECRET, ISSUER, AUDIENCE, METRICS);
    var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0aHVuZGVyLW9hdXRoLWlzc3VlciIsIml"
        + "hdCI6MTYxOTIxNDQ2NSwiZXhwIjoxNjE5MjIwMjg4LCJhdWQiOiJ0aHVuZGVyIiwic3ViIjoidGVzdEB0ZXN0LmN"
        + "vbSJ9.RnJ5mzn1sSj4b9hGKTJXGjHFAu1cCOT8zYyvFYfmzFI";

    assertTrue(authenticator.authenticate(token).isEmpty());
  }

  @Test
  void incorrectAudienceFailsAuthentication() {
    var authenticator = new OAuthAuthenticator(SECRET, ISSUER, AUDIENCE, METRICS);
    var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0aHVuZGVyLW9hdXRoLWlzc3VlciIsIml"
        + "hdCI6MTYxOTIxNDQ2NSwiZXhwIjo2NDc2NDY2MDM2OCwiYXVkIjoibGlnaHRuaW5nIiwic3ViIjoidGVzdEB0ZXN"
        + "0LmNvbSJ9.weQgXZZ4h7facHnyQcFwLIkFKtmoE61Srj0kWjMD5ns";

    assertTrue(authenticator.authenticate(token).isEmpty());
  }

  @Test
  void incorrectIssuerFailsAuthentication() {
    var authenticator = new OAuthAuthenticator(SECRET, ISSUER, AUDIENCE, METRICS);
    var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0aHVuZGVyLW9hdXRoLWJhZC1pc3N1ZXI"
        + "iLCJpYXQiOjE2MTkyMTQ0NjUsImV4cCI6NjQ3NjQ2NjAzNjgsImF1ZCI6InRodW5kZXIiLCJzdWIiOiJ0ZXN0QHR"
        + "lc3QuY29tIn0.CePraWtys45_CtN6hCjH2uA-yyy5XZitcsKKcS3ckIU";

    assertTrue(authenticator.authenticate(token).isEmpty());
  }

  // The below tests use JWT tokens that expire in the year 4022.
  // If this code is somehow still being used in 4022, and someone is reading this,
  // these tokens will need to be updated in order for tests to pass.

  @Test
  void hs256SignedTokenPasses() {
    var authenticator = new OAuthAuthenticator(SECRET, ISSUER, AUDIENCE, METRICS);
    var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0aHVuZGVyLW9hdXRoLWlzc3VlciIsIml"
        + "hdCI6MTYxOTIxNDQ2NSwiZXhwIjo2NDc2NDY2MDM2OCwiYXVkIjoidGh1bmRlciIsInN1YiI6InRlc3RAdGVzdC5"
        + "jb20ifQ.tQirYW-zHSAsS3xRYczyfsfVfolvv8O91HMhGz1qoGk";

    Optional<Principal> actor = authenticator.authenticate(token);

    assertTrue(actor.isPresent());
    assertEquals("test@test.com", actor.get().getName());
  }

  @Test
  void hs384SignedTokenPasses() {
    var authenticator = new OAuthAuthenticator(SECRET, ISSUER, AUDIENCE, METRICS);
    var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzM4NCJ9.eyJpc3MiOiJ0aHVuZGVyLW9hdXRoLWlzc3VlciIsIml"
        + "hdCI6MTYxOTIxNDQ2NSwiZXhwIjo2NDc2NDY2MDM2OCwiYXVkIjoidGh1bmRlciIsInN1YiI6InRlc3RAdGVzdC5"
        + "jb20ifQ.ByOQQd35fdOpqzOo5A3v1BAzKtVImFt9jbo_K8wbIxwN9KX0NrIJWN1HmypsQ_Rd";

    Optional<Principal> actor = authenticator.authenticate(token);

    assertTrue(actor.isPresent());
    assertEquals("test@test.com", actor.get().getName());
  }

  @Test
  void hs512SignedTokenPasses() {
    var authenticator = new OAuthAuthenticator(SECRET, ISSUER, AUDIENCE, METRICS);
    var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ0aHVuZGVyLW9hdXRoLWlzc3VlciIsIml"
        + "hdCI6MTYxOTIxNDQ2NSwiZXhwIjo2NDc2NDY2MDM2OCwiYXVkIjoidGh1bmRlciIsInN1YiI6InRlc3RAdGVzdC5"
        + "jb20ifQ.5LzREFp5kq01V5O8Sh4d3YsfU0kRvgqNWYzao_fmVwdeidycyeAIEJTbaRmKv-j9RxKGB0x40A6EEOuu"
        + "aVEBgg";

    Optional<Principal> actor = authenticator.authenticate(token);

    assertTrue(actor.isPresent());
    assertEquals("test@test.com", actor.get().getName());
  }
}
