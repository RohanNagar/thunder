import { describe } from 'https://jslib.k6.io/expect/0.0.5/index.js';
import { Httpx } from 'https://jslib.k6.io/httpx/0.0.5/index.js';

export const options = {
  thresholds: {
    checks: [{
      threshold:   'rate == 1.00',
      abortOnFail: true
    }],
    http_req_duration: ['p(95)<100'] // 99% of requests should be below 100ms
  },
  // 10 virtual users running the test suite 10 times each
  vus:        10,
  iterations: 10
};

const credentials = 'application:secret';
const session = new Httpx({ baseURL: `http://${credentials}@localhost:8080` });

const validUserForUpdate = {
  email: {
    address: 'test@test.com'
  },
  password:   'password',
  uniqueID:   'ABC123',
  attributes: ['hello', 'world']
};

const missingEmailUser = {
  email:      null,
  password:   'password',
  uniqueID:   'ABC123',
  attributes: ['hello', 'world']
};

const invalidEmailUser = {
  email: {
    address: 'invalid'
  },
  password:   'password',
  uniqueID:   'ABC123',
  attributes: ['hello', 'world']
};

const invalidPropertiesUser = {
  email: {
    address: 'test@test.com'
  },
  password: 'password',
  uniqueID: 'ABC123'
};

export default function testSuite() {
  describe('create a null user', (t) => {
    const resp = session.post(`/users`, null,
        { headers: { 'Content-Type': 'application/json' } });

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Cannot post a null user. (User: null)');
  }) &&

  describe('create a user without an email address', (t) => {
    const resp = session.post(`/users`, JSON.stringify({ email: null }),
        { headers: { 'Content-Type': 'application/json' } });

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Cannot post a user without an email address. (User: null)');
  }) &&

  describe('create a user with an invalid email address', (t) => {
    const resp = session.post(`/users`, JSON.stringify({ email: { address: 'invalid' } }),
        { headers: { 'Content-Type': 'application/json' } });

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Invalid email address format. Please try again. (User: invalid)');
  }) &&

  describe('create a user with invalid properties', (t) => {
    const resp = session.post(`/users`, JSON.stringify({ email: { address: 'test@test.com' }, password: 'test' }),
        { headers: { 'Content-Type': 'application/json' } });

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Cannot post a user with invalid properties. (User: test@test.com)');
  }) &&

  describe('get a user without an email address', (t) => {
    const resp = session.get(`/users`, null,
        { headers: { 'password': 'password' } });

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Incorrect or missing email query parameter. (User: null)');
  }) &&

  describe('get a user with an empty email address', (t) => {
    const resp = session.get(`/users?email=`, null,
        { headers: { 'password': 'password' } });

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Incorrect or missing email query parameter. (User: )');
  }) &&

  describe('get a user without a password header', (t) => {
    const resp = session.get(`/users?email=success@simulator.amazonses.com`);

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Credentials are required to access this resource. (User: success@simulator.amazonses.com)');
  }) &&

  describe('get a user with an empty password header', (t) => {
    const resp = session.get(`/users?email=success@simulator.amazonses.com`, null,
        { headers: { 'password': '' } });

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Credentials are required to access this resource. (User: success@simulator.amazonses.com)');
  }) &&

  describe('send a verification email to a null email address', (t) => {
    const resp = session.post(`/verify`, null,
        { headers: { 'password': 'password' } });

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Incorrect or missing email query parameter. (User: null)');
  }) &&

  describe('send a verification email to an empty email address', (t) => {
    const resp = session.post(`/verify?email=`, null,
        { headers: { 'password': 'password' } });

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Incorrect or missing email query parameter. (User: )');
  }) &&

  describe('send a verification email without a password header', (t) => {
    const resp = session.post(`/verify?email=success@simulator.amazonses.com`);

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Credentials are required to access this resource. (User: success@simulator.amazonses.com)');
  }) &&

  describe('send a verification email with an empty password header', (t) => {
    const resp = session.post(`/verify?email=success@simulator.amazonses.com`, null,
        { headers: { 'password': '' } });

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Credentials are required to access this resource. (User: success@simulator.amazonses.com)');
  }) &&

  describe('verify a null email address', (t) => {
    const resp = session.get(`/verify?token=testToken`);

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Incorrect or missing email query parameter. (User: null)');
  }) &&

  describe('verify an empty email address', (t) => {
    const resp = session.get(`/verify?email=&token=testToken`);

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Incorrect or missing email query parameter. (User: )');
  }) &&

  describe('verify with a null token', (t) => {
    const resp = session.get(`/verify?email=success@simulator.amazonses.com`);

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Incorrect or missing verification token query parameter. (User: success@simulator.amazonses.com)');
  }) &&

  describe('verify with an empty token', (t) => {
    const resp = session.get(`/verify?email=success@simulator.amazonses.com&token=`);

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Incorrect or missing verification token query parameter. (User: success@simulator.amazonses.com)');
  }) &&

  describe('reset verification with a null email', (t) => {
    const resp = session.post(`/verify/reset`, null,
        { headers: { 'password': 'password' } });

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Incorrect or missing email query parameter. (User: null)');
  }) &&

  describe('reset verification with an empty email', (t) => {
    const resp = session.post(`/verify/reset?email=`, null,
        { headers: { 'password': 'password' } });

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Incorrect or missing email query parameter. (User: )');
  }) &&

  describe('reset verification without the password header', (t) => {
    const resp = session.post(`/verify/reset?email=success@simulator.amazonses.com`);

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Credentials are required to access this resource. (User: success@simulator.amazonses.com)');
  }) &&

  describe('reset verification with an empty password header', (t) => {
    const resp = session.post(`/verify/reset?email=success@simulator.amazonses.com`, null,
        { headers: { 'password': '' } });

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Credentials are required to access this resource. (User: success@simulator.amazonses.com)');
  }) &&

  describe('update a user without the password header', (t) => {
    const resp = session.put(`/users`, JSON.stringify(validUserForUpdate),
        { headers: { 'Content-Type': 'application/json' } });

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Credentials are required to access this resource. (User: test@test.com)');
  }) &&

  describe('update a user with an empty password header', (t) => {
    const resp = session.put(`/users`, JSON.stringify(validUserForUpdate),
        { headers: { 'Content-Type': 'application/json', 'password': '' } });

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Credentials are required to access this resource. (User: test@test.com)');
  }) &&

  describe('update with a null user object', (t) => {
    const resp = session.put(`/users`, null,
        { headers: { 'Content-Type': 'application/json', 'password': 'password' } });

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Cannot post a null user. (User: null)');
  }) &&

  describe('update with a user object that does not have an email address', (t) => {
    const resp = session.put(`/users`, JSON.stringify(missingEmailUser),
        { headers: { 'Content-Type': 'application/json', 'password': 'password' } });

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Cannot post a user without an email address. (User: null)');
  }) &&

  describe('update with a user object that has an invalid email address', (t) => {
    const resp = session.put(`/users`, JSON.stringify(invalidEmailUser),
        { headers: { 'Content-Type': 'application/json', 'password': 'password' } });

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Invalid email address format. Please try again. (User: invalid)');
  }) &&

  describe('update with a user object that has invalid properties', (t) => {
    const resp = session.put(`/users`, JSON.stringify(invalidPropertiesUser),
        { headers: { 'Content-Type': 'application/json', 'password': 'password' } });

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Cannot post a user with invalid properties. (User: test@test.com)');
  }) &&

  describe('delete a null email address', (t) => {
    const resp = session.delete(`/users`, null, { headers: { 'password': 'password' } });

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Incorrect or missing email query parameter. (User: null)');
  }) &&

  describe('delete an empty email address', (t) => {
    const resp = session.delete(`/users?email=`, null, { headers: { 'password': 'password' } });

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Incorrect or missing email query parameter. (User: )');
  }) &&

  describe('delete a user without a password header', (t) => {
    const resp = session.delete(`/users?email=test@test.com`);

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Credentials are required to access this resource. (User: test@test.com)');
  }) &&

  describe('delete a user with an empty password header', (t) => {
    const resp = session.delete(`/users?email=test@test.com`, null, { headers: { 'password': '' } });

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('error message')
        .toEqual('Credentials are required to access this resource. (User: test@test.com)');
  });
}
