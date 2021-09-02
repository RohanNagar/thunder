import { describe } from 'https://jslib.k6.io/expect/0.0.5/index.js';
import { Httpx } from 'https://jslib.k6.io/httpx/0.0.5/index.js';

export const options = {
  thresholds: {
    checks: [{
      threshold:   'rate == 1.00',
      abortOnFail: true
    }]
  },
  vus:        1,
  iterations: 1
};

const runMetricsTest = true;

const metricNames = {
  jwtFailureCounter:
    'com.sanctionco.thunder.authentication.oauth.OAuthAuthenticator.jwt-verification-failure',
  jwtSuccessCounter:
    'com.sanctionco.thunder.authentication.oauth.OAuthAuthenticator.jwt-verification-success'
};

const session = new Httpx({ baseURL: 'http://localhost:8080' });
const adminSession = new Httpx({ baseURL: 'http://localhost:8081' });

/* eslint-disable key-spacing */
const bearerTokens = {
  correct: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ0aHVuZGVyLW9hdXRoLWlzc3VlciIsImlhdCI6' +
      'MTYxOTIxNDQ2NSwiZXhwIjo2NDc2NDY2MDM2OCwiYXVkIjoidGh1bmRlciIsInN1YiI6InRlc3RAdGVzdC5jb20ifQ' +
      '.5LzREFp5kq01V5O8Sh4d3YsfU0kRvgqNWYzao_fmVwdeidycyeAIEJTbaRmKv-j9RxKGB0x40A6EEOuuaVEBgg',
  expired: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0aHVuZGVyLW9hdXRoLWlzc3VlciIsImlhdCI6' +
      'MTYxOTIxNDQ2NSwiZXhwIjoxNjE5MjIwMjg4LCJhdWQiOiJ0aHVuZGVyIiwic3ViIjoidGVzdEB0ZXN0LmNvbSJ9.R' +
      'nJ5mzn1sSj4b9hGKTJXGjHFAu1cCOT8zYyvFYfmzFI',
  nbf4022: 'eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9l' +
      'IiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMiwibmJmIjo2NDc2NDY2MDM2OH0.LZzojeyPFFIbollBFQMN1D' +
      'hazkH37gB42BdzzPb4_ZN7J0klrz8xD1sXz4gFw_XL0RVu8gu-d653meMlNMX-oQ',
  invalid: 'this-token-is-not-right',
  unknownAlgo: 'eyJhbGciOiJQUzM4NCIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4g' +
      'RG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.MqF1AKsJkijKnfqEI3VA1OnzAL2S4eIpAuievMgD3t' +
      'EFyFMU67gCbg-fxsc5dLrxNwdZEXs9h0kkicJZ70mp6p5vdv-j2ycDKBWg05Un4OhEl7lYcdIsCsB8QUPmstF-lQWn' +
      'Nqnq3wra1GynJrOXDL27qIaJnnQKlXuayFntBF0j-82jpuVdMaSXvk3OGaOM-7rCRsBcSPmocaAO-uWJEGPw_OWVaC' +
      '5RRdWDroPi4YL4lTkDEC-KEvVkqCnFm_40C-T_siXquh5FVbpJjb3W2_YvcqfDRj44TsRrpVhk6ohsHMNeUad_cxnF' +
      'npolIKnaXq_COv35e9EgeQIPAbgIeg',
  incorrectAudience: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ0aHVuZGVyLW9hdXRoLWlzc3Vlci' +
      'IsImlhdCI6MTYxOTIxNDQ2NSwiZXhwIjo2NDc2NDY2MDM2OCwiYXVkIjoidGh1bmRlcjU1Iiwic3ViIjoidGVzdEB0' +
      'ZXN0LmNvbSJ9.-bS1_APekpAYZEUiM1Gc_cVGwV3Bcghrb3bI-lqa_h37E0dDzg1bs1S1X7b9MRdS7ofS8ohoA7Lw_' +
      'gpXuS5N5Q',
  incorrectIssuer: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0aHVuZGVyLW9hdXRoLWJhZC1pc3N1' +
      'ZXIiLCJpYXQiOjE2MTkyMTQ0NjUsImV4cCI6NjQ3NjQ2NjAzNjgsImF1ZCI6InRodW5kZXIiLCJzdWIiOiJ0ZXN0QH' +
      'Rlc3QuY29tIn0.CePraWtys45_CtN6hCjH2uA-yyy5XZitcsKKcS3ckIU',
  incorrectSignature: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0aHVuZGVyLW9hdXRoLWlzc3Vlc' +
      'iIsImlhdCI6MTYxOTIxNDQ2NSwiZXhwIjo2NDc2NDY2MDM2OCwiYXVkIjoidGh1bmRlciIsInN1YiI6InRlc3RAdGV' +
      'zdC5jb20ifQ.xXDnxiccspOUs_j0Ulrdhggd1Bc2gUlc414SFDm9lHs',
  hs512: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ0aHVuZGVyLW9hdXRoLWlzc3VlciIsImlhdCI6MT' +
      'YxOTIxNDQ2NSwiZXhwIjo2NDc2NDY2MDM2OCwiYXVkIjoidGh1bmRlciIsInN1YiI6InRlc3RAdGVzdC5jb20ifQ.5' +
      'LzREFp5kq01V5O8Sh4d3YsfU0kRvgqNWYzao_fmVwdeidycyeAIEJTbaRmKv-j9RxKGB0x40A6EEOuuaVEBgg',
  hs384: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzM4NCJ9.eyJpc3MiOiJ0aHVuZGVyLW9hdXRoLWlzc3VlciIsImlhdCI6MT' +
      'YxOTIxNDQ2NSwiZXhwIjo2NDc2NDY2MDM2OCwiYXVkIjoidGh1bmRlciIsInN1YiI6InRlc3RAdGVzdC5jb20ifQ.B' +
      'yOQQd35fdOpqzOo5A3v1BAzKtVImFt9jbo_K8wbIxwN9KX0NrIJWN1HmypsQ_Rd',
  hs256: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0aHVuZGVyLW9hdXRoLWlzc3VlciIsImlhdCI6MT' +
      'YxOTIxNDQ2NSwiZXhwIjo2NDc2NDY2MDM2OCwiYXVkIjoidGh1bmRlciIsInN1YiI6InRlc3RAdGVzdC5jb20ifQ.t' +
      'QirYW-zHSAsS3xRYczyfsfVfolvv8O91HMhGz1qoGk',
  rs512: 'eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiaXNzIjoidGh1bmRlci' +
      '1vYXV0aC1pc3N1ZXIiLCJhdWQiOiJ0aHVuZGVyIiwiaWF0IjoxNTE2MjM5MDIyfQ.VYw8anCkcoJGqe2bm7HYTqqZs' +
      'elfK4N9x5i--eAgLdbMviTD_Sf8Eb3l9Hb0FPvHe5WAIiR1oUmAZgL2UXJvWjsZ6pkwE-qVHucY_lBpIPdnFvcA92R' +
      '3-JBraED9KuUpJoXGOcvjS23T1bF4wQgKyrCjGC_sk4uzQAQLLGU1xzFhLCsL03cEToOG-fIGfaHvhaopQSaQIcaqh' +
      'Tlzqrvgj2MaqKFw6cyxNgak6PGJlH-e4GLyxHc8MmrWKaE-lmW8RGWGXRnd97E_a07nID_njOUktil5Zn1-tv2Zdwu' +
      'F3TSIRl0EkQBAVkDykqkR_rxKvsDlf6TVv7lhfJ4W3ml3yA',
  rs384: 'eyJhbGciOiJSUzM4NCIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiaXNzIjoidGh1bmRlci' +
      '1vYXV0aC1pc3N1ZXIiLCJhdWQiOiJ0aHVuZGVyIiwiaWF0IjoxNTE2MjM5MDIyfQ.RfIkF4f6EF9WVfIY4dnn_oAvS' +
      'lXoYr_xY2Y_-8Aw0plctLlq0JMDDtbBVYHbPHQYq8c11gD4lRCAABHQoXDhYLLwX3-WCnK-Yi1r7phs5dSiWaKKXWo' +
      'RdfN0TjPnDQHvfQMswJD9XiAfj6g-2Wu0wJUfU5mWL4Opjq_0gcclnpYC5xyK43dRzpsQLLfZDRx0gqFR_WAqN6KVK' +
      'DyfWfZbDhWuv2p9W_bX4_IiH2i6Qqe9Ku1c2aF-ArS_2vRRlylzCDtHCoDr5HMrLu5o3_bsatWL9__fYQyPZUpWHAo' +
      'q5xKGD4GAPfhNLp5bUVW71ekqb9BI4SO1F4S-UQkr4Y8new',
  rs256: 'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiaXNzIjoidGh1bmRlci' +
      '1vYXV0aC1pc3N1ZXIiLCJhdWQiOiJ0aHVuZGVyIiwiaWF0IjoxNTE2MjM5MDIyfQ.BXxIYZZggoGKLaTfkiyzpjDsw' +
      'CAz4iaoFZJDdgKFrC-xB_VNtlk1_ijgjFwvUyxIcoEUieh_wDrc1Ok_FC5mJvFWigtiiMQEp0P87O5vSaySAwffwy2' +
      'WQcV_Gv8a3az8CqNOJ_pFIiZt6RiI35yw9FuPQtNK_7Mh6Hxo05qLc7zaYHgLnhSN3GOyIQQV51O0gmpPxPEnQO7G3' +
      'UtcIFFuu047du0dmZaW53GyL3TTtFRnoy-fdqVzLFf9oNfIYMaXOjhyp6WNMYnQrkGHdhIKd_-qvAfQ5-KGDdL0GZw' +
      '-QPl7D55A402M2y2ss7OaNA7DcYF1LS_kHDd4AVXWsdO03w'
};
/* eslint-enable key-spacing */

const user = {
  email:    { address: 'test@test.com' },
  password: 'password'
};

function ensureGetFailure(testName, token) {
  return describe(testName, (t) => {
    const resp = session.get(`/users?email=${user.email.address}`, null, {
      headers: {
        'password':      user.password,
        'Authorization': 'Bearer ' + token
      } });

    t.expect(resp.status).as('status').toEqual(401)
        .and(resp.body).as('error message')
        .toEqual('Credentials are required to access this resource.');
  });
}

function ensureGetSuccess(testName, token) {
  return describe(testName, (t) => {
    const resp = session.get(`/users?email=${user.email.address}`, null, {
      headers: {
        'password':      user.password,
        'Authorization': 'Bearer ' + token
      } });

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(user.email.address);
  });
}

export default function testSuite() {
  describe('create a user', (t) => {
    const resp = session.post(`/users`, JSON.stringify(user), {
      headers: {
        'Content-Type':  'application/json',
        'Authorization': 'Bearer ' + bearerTokens.correct
      } });

    t.expect(resp.status).as('status').toEqual(201)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(user.email.address);
  }) &&

  ensureGetFailure('get with no jwt token', '') &&
  ensureGetFailure('get with expired jwt token', bearerTokens.expired) &&
  ensureGetFailure('get with not before 4022 jwt token', bearerTokens.nbf4022) &&
  ensureGetFailure('get with invalid jwt token', bearerTokens.invalid) &&
  ensureGetFailure('get with unknown jwt algorithm', bearerTokens.unknownAlgo) &&
  ensureGetFailure('get with incorrect jwt audience', bearerTokens.incorrectAudience) &&
  ensureGetFailure('get with incorrect jwt issuer', bearerTokens.incorrectIssuer) &&
  ensureGetFailure('get with incorrectly signed jwt token', bearerTokens.incorrectSignature) &&

  ensureGetSuccess('get with HS512 JWT token', bearerTokens.hs512) &&
  ensureGetSuccess('get with HS384 JWT token', bearerTokens.hs384) &&
  ensureGetSuccess('get with HS256 JWT token', bearerTokens.hs256) &&
  ensureGetSuccess('get with RS512 JWT token', bearerTokens.rs512) &&
  ensureGetSuccess('get with RS384 JWT token', bearerTokens.rs384) &&
  ensureGetSuccess('get with RS256 JWT token', bearerTokens.rs256) &&

  describe('delete the user', (t) => {
    const resp = session.delete(`/users?email=${user.email.address}`, null, {
      headers: {
        'Authorization': 'Bearer ' + bearerTokens.correct,
        'password':      user.password
      } });

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(user.email.address);
  }) &&

  describe('check metrics', (t) => {
    if (!runMetricsTest) {
      t.expect(true).as('skipped').toEqual(true);

      return;
    }

    const resp = adminSession.get('/metrics');

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('counters')[metricNames.jwtFailureCounter].count).as('JWT failure count').toEqual(7)
        .and(resp.json('counters')[metricNames.jwtSuccessCounter].count).as('JWT success count').toEqual(8);
  });
}
