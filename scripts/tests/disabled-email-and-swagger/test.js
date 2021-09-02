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

const credentials = 'application:secret';
const session = new Httpx({ baseURL: `http://${credentials}@localhost:8080` });

const user = {
  email:    { address: 'user@test.com' },
  password: 'password'
};

export default function testSuite() {
  describe('create the user', (t) => {
    const resp = session.post(`/users`, JSON.stringify(user),
        { headers: { 'Content-Type': 'application/json' } });

    t.expect(resp.status).as('status').toEqual(201)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(user.email.address);
  }) &&

  describe('get the user', (t) => {
    const resp = session.get(`/users?email=${user.email.address}`, null,
        { headers: { 'password': user.password } });

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(user.email.address);
  }) &&

  describe('send email should be not found', (t) => {
    const resp = session.post(`/verify?email=${user.email.address}`, null,
        { headers: { 'password': user.password } });

    t.expect(resp.status).as('status').toEqual(404)
        .and(resp).toHaveValidJson()
        .and(resp.json('message')).as('error message').toEqual('HTTP 404 Not Found');
  }) &&

  describe('verify user should be not found', (t) => {
    const resp = session.get(`/verify?email=${user.email.address}&token=testToken`);

    t.expect(resp.status).as('status').toEqual(404)
        .and(resp).toHaveValidJson()
        .and(resp.json('message')).as('error message').toEqual('HTTP 404 Not Found');
  }) &&

  describe('reset verification status should be not found', (t) => {
    const resp = session.get(`/verify/reset?email=${user.email.address}`, null,
        { headers: { 'password': user.password } });

    t.expect(resp.status).as('status').toEqual(404)
        .and(resp).toHaveValidJson()
        .and(resp.json('message')).as('error message').toEqual('HTTP 404 Not Found');
  }) &&

  describe('delete the user', (t) => {
    const resp = session.delete(`/users?email=${user.email.address}`, null,
        { headers: { 'password': user.password } });

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(user.email.address);
  }) &&

  describe('OpenAPI json is disabled', (t) => {
    const resp = session.get(`/openapi.json`);

    t.expect(resp.status).as('status').toEqual(404);
  }) &&

  describe('OpenAPI yaml is disabled', (t) => {
    const resp = session.get(`/openapi.yaml`);

    t.expect(resp.status).as('status').toEqual(404);
  }) &&

  describe('swagger UI is disabled', (t) => {
    const resp = session.get(`/swagger`);

    t.expect(resp.status).as('status').toEqual(404);
  });
}
