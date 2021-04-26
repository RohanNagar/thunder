package com.sanctionco.thunder.authentication.oauth;

import com.codahale.metrics.MetricRegistry;
import com.sanctionco.thunder.util.FileUtilities;

import java.security.Principal;
import java.security.interfaces.RSAPublicKey;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <a href=https://jwt.io>jwt.io</a> is a good resource to generate test JWT tokens.
 */
class OAuthAuthenticatorTest {
  private static final String SECRET = "qwertyuiopasdfghjklzxcvbnm123456";
  private static final RSAPublicKey RSA_KEY = FileUtilities
      .readPublicKeyFromPath("src/test/resources/fixtures/test-rsa-public-key.der");
  private static final String ISSUER = "thunder-oauth-issuer";
  private static final String AUDIENCE = "thunder";
  private static final MetricRegistry METRICS = new MetricRegistry();

  @Test
  void nullTokenFailsAuthentication() {
    var authenticator = new OAuthAuthenticator(SECRET, ISSUER, AUDIENCE, RSA_KEY, METRICS);

    assertTrue(authenticator.authenticate(null).isEmpty());
  }

  @Test
  void invalidFormatTokenFailsAuthentication() {
    var authenticator = new OAuthAuthenticator(SECRET, ISSUER, AUDIENCE, RSA_KEY, METRICS);

    assertTrue(authenticator.authenticate("thistokenisnotright").isEmpty());
  }

  @Test
  void unrecognizedAlgorithmFailsAuthentication() {
    var authenticator = new OAuthAuthenticator(SECRET, ISSUER, AUDIENCE, RSA_KEY, METRICS);
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
    var authenticator = new OAuthAuthenticator(SECRET, ISSUER, AUDIENCE, RSA_KEY, METRICS);
    var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0aHVuZGVyLW9hdXRoLWlzc3VlciIsIml"
        + "hdCI6MTYxOTIxNDQ2NSwiZXhwIjoxNjE5MjIwMjg4LCJhdWQiOiJ0aHVuZGVyIiwic3ViIjoidGVzdEB0ZXN0LmN"
        + "vbSJ9.RnJ5mzn1sSj4b9hGKTJXGjHFAu1cCOT8zYyvFYfmzFI";

    assertTrue(authenticator.authenticate(token).isEmpty());
  }

  @Test
  void incorrectAudienceFailsAuthentication() {
    var authenticator = new OAuthAuthenticator(SECRET, ISSUER, AUDIENCE, RSA_KEY, METRICS);
    var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0aHVuZGVyLW9hdXRoLWlzc3VlciIsIml"
        + "hdCI6MTYxOTIxNDQ2NSwiZXhwIjo2NDc2NDY2MDM2OCwiYXVkIjoibGlnaHRuaW5nIiwic3ViIjoidGVzdEB0ZXN"
        + "0LmNvbSJ9.weQgXZZ4h7facHnyQcFwLIkFKtmoE61Srj0kWjMD5ns";

    assertTrue(authenticator.authenticate(token).isEmpty());
  }

  @Test
  void incorrectIssuerFailsAuthentication() {
    var authenticator = new OAuthAuthenticator(SECRET, ISSUER, AUDIENCE, RSA_KEY, METRICS);
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
    var authenticator = new OAuthAuthenticator(SECRET, ISSUER, AUDIENCE, RSA_KEY, METRICS);
    var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0aHVuZGVyLW9hdXRoLWlzc3VlciIsIml"
        + "hdCI6MTYxOTIxNDQ2NSwiZXhwIjo2NDc2NDY2MDM2OCwiYXVkIjoidGh1bmRlciIsInN1YiI6InRlc3RAdGVzdC5"
        + "jb20ifQ.tQirYW-zHSAsS3xRYczyfsfVfolvv8O91HMhGz1qoGk";

    Optional<Principal> actor = authenticator.authenticate(token);

    assertTrue(actor.isPresent());
    assertEquals("test@test.com", actor.get().getName());
  }

  @Test
  void hs384SignedTokenPasses() {
    var authenticator = new OAuthAuthenticator(SECRET, ISSUER, AUDIENCE, RSA_KEY, METRICS);
    var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzM4NCJ9.eyJpc3MiOiJ0aHVuZGVyLW9hdXRoLWlzc3VlciIsIml"
        + "hdCI6MTYxOTIxNDQ2NSwiZXhwIjo2NDc2NDY2MDM2OCwiYXVkIjoidGh1bmRlciIsInN1YiI6InRlc3RAdGVzdC5"
        + "jb20ifQ.ByOQQd35fdOpqzOo5A3v1BAzKtVImFt9jbo_K8wbIxwN9KX0NrIJWN1HmypsQ_Rd";

    Optional<Principal> actor = authenticator.authenticate(token);

    assertTrue(actor.isPresent());
    assertEquals("test@test.com", actor.get().getName());
  }

  @Test
  void hs512SignedTokenPasses() {
    var authenticator = new OAuthAuthenticator(SECRET, ISSUER, AUDIENCE, RSA_KEY, METRICS);
    var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ0aHVuZGVyLW9hdXRoLWlzc3VlciIsIml"
        + "hdCI6MTYxOTIxNDQ2NSwiZXhwIjo2NDc2NDY2MDM2OCwiYXVkIjoidGh1bmRlciIsInN1YiI6InRlc3RAdGVzdC5"
        + "jb20ifQ.5LzREFp5kq01V5O8Sh4d3YsfU0kRvgqNWYzao_fmVwdeidycyeAIEJTbaRmKv-j9RxKGB0x40A6EEOuu"
        + "aVEBgg";

    Optional<Principal> actor = authenticator.authenticate(token);

    assertTrue(actor.isPresent());
    assertEquals("test@test.com", actor.get().getName());
  }

  @Test
  void rsa256SignedTokenPasses() {
    var authenticator = new OAuthAuthenticator(SECRET, ISSUER, AUDIENCE, RSA_KEY, METRICS);
    var token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiaXNzIjoidGh1b"
        + "mRlci1vYXV0aC1pc3N1ZXIiLCJhdWQiOiJ0aHVuZGVyIiwiaWF0IjoxNTE2MjM5MDIyfQ.BXxIYZZggoGKLaTfki"
        + "yzpjDswCAz4iaoFZJDdgKFrC-xB_VNtlk1_ijgjFwvUyxIcoEUieh_wDrc1Ok_FC5mJvFWigtiiMQEp0P87O5vSa"
        + "ySAwffwy2WQcV_Gv8a3az8CqNOJ_pFIiZt6RiI35yw9FuPQtNK_7Mh6Hxo05qLc7zaYHgLnhSN3GOyIQQV51O0gm"
        + "pPxPEnQO7G3UtcIFFuu047du0dmZaW53GyL3TTtFRnoy-fdqVzLFf9oNfIYMaXOjhyp6WNMYnQrkGHdhIKd_-qvA"
        + "fQ5-KGDdL0GZw-QPl7D55A402M2y2ss7OaNA7DcYF1LS_kHDd4AVXWsdO03w";

    Optional<Principal> actor = authenticator.authenticate(token);

    assertTrue(actor.isPresent());
    assertEquals("test@test.com", actor.get().getName());
  }

  @Test
  void rsa384SignedTokenPasses() {
    var authenticator = new OAuthAuthenticator(SECRET, ISSUER, AUDIENCE, RSA_KEY, METRICS);
    var token = "eyJhbGciOiJSUzM4NCIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiaXNzIjoidGh1b"
        + "mRlci1vYXV0aC1pc3N1ZXIiLCJhdWQiOiJ0aHVuZGVyIiwiaWF0IjoxNTE2MjM5MDIyfQ.RfIkF4f6EF9WVfIY4d"
        + "nn_oAvSlXoYr_xY2Y_-8Aw0plctLlq0JMDDtbBVYHbPHQYq8c11gD4lRCAABHQoXDhYLLwX3-WCnK-Yi1r7phs5d"
        + "SiWaKKXWoRdfN0TjPnDQHvfQMswJD9XiAfj6g-2Wu0wJUfU5mWL4Opjq_0gcclnpYC5xyK43dRzpsQLLfZDRx0gq"
        + "FR_WAqN6KVKDyfWfZbDhWuv2p9W_bX4_IiH2i6Qqe9Ku1c2aF-ArS_2vRRlylzCDtHCoDr5HMrLu5o3_bsatWL9_"
        + "_fYQyPZUpWHAoq5xKGD4GAPfhNLp5bUVW71ekqb9BI4SO1F4S-UQkr4Y8new";

    Optional<Principal> actor = authenticator.authenticate(token);

    assertTrue(actor.isPresent());
    assertEquals("test@test.com", actor.get().getName());
  }

  @Test
  void rsa512SignedTokenPasses() {
    var authenticator = new OAuthAuthenticator(SECRET, ISSUER, AUDIENCE, RSA_KEY, METRICS);
    var token = "eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiaXNzIjoidGh1b"
        + "mRlci1vYXV0aC1pc3N1ZXIiLCJhdWQiOiJ0aHVuZGVyIiwiaWF0IjoxNTE2MjM5MDIyfQ.VYw8anCkcoJGqe2bm7"
        + "HYTqqZselfK4N9x5i--eAgLdbMviTD_Sf8Eb3l9Hb0FPvHe5WAIiR1oUmAZgL2UXJvWjsZ6pkwE-qVHucY_lBpIP"
        + "dnFvcA92R3-JBraED9KuUpJoXGOcvjS23T1bF4wQgKyrCjGC_sk4uzQAQLLGU1xzFhLCsL03cEToOG-fIGfaHvha"
        + "opQSaQIcaqhTlzqrvgj2MaqKFw6cyxNgak6PGJlH-e4GLyxHc8MmrWKaE-lmW8RGWGXRnd97E_a07nID_njOUkti"
        + "l5Zn1-tv2ZdwuF3TSIRl0EkQBAVkDykqkR_rxKvsDlf6TVv7lhfJ4W3ml3yA";

    Optional<Principal> actor = authenticator.authenticate(token);

    assertTrue(actor.isPresent());
    assertEquals("test@test.com", actor.get().getName());
  }

  @Test
  void rsaTokenFailsWhenKeyIsNull() {
    var authenticator = new OAuthAuthenticator(SECRET, ISSUER, AUDIENCE, null, METRICS);
    var token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiaXNzIjoidGh1b"
        + "mRlci1vYXV0aC1pc3N1ZXIiLCJhdWQiOiJ0aHVuZGVyIiwiaWF0IjoxNTE2MjM5MDIyfQ.BXxIYZZggoGKLaTfki"
        + "yzpjDswCAz4iaoFZJDdgKFrC-xB_VNtlk1_ijgjFwvUyxIcoEUieh_wDrc1Ok_FC5mJvFWigtiiMQEp0P87O5vSa"
        + "ySAwffwy2WQcV_Gv8a3az8CqNOJ_pFIiZt6RiI35yw9FuPQtNK_7Mh6Hxo05qLc7zaYHgLnhSN3GOyIQQV51O0gm"
        + "pPxPEnQO7G3UtcIFFuu047du0dmZaW53GyL3TTtFRnoy-fdqVzLFf9oNfIYMaXOjhyp6WNMYnQrkGHdhIKd_-qvA"
        + "fQ5-KGDdL0GZw-QPl7D55A402M2y2ss7OaNA7DcYF1LS_kHDd4AVXWsdO03w";

    Optional<Principal> actor = authenticator.authenticate(token);

    assertFalse(actor.isPresent());
  }

  @Test
  void hmacTokenFailsWhenKeyIsNull() {
    var authenticator = new OAuthAuthenticator(null, ISSUER, AUDIENCE, RSA_KEY, METRICS);
    var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ0aHVuZGVyLW9hdXRoLWlzc3VlciIsIml"
        + "hdCI6MTYxOTIxNDQ2NSwiZXhwIjo2NDc2NDY2MDM2OCwiYXVkIjoidGh1bmRlciIsInN1YiI6InRlc3RAdGVzdC5"
        + "jb20ifQ.5LzREFp5kq01V5O8Sh4d3YsfU0kRvgqNWYzao_fmVwdeidycyeAIEJTbaRmKv-j9RxKGB0x40A6EEOuu"
        + "aVEBgg";

    Optional<Principal> actor = authenticator.authenticate(token);

    assertFalse(actor.isPresent());
  }
}
