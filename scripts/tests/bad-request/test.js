import { describe, expect } from 'https://jslib.k6.io/k6chaijs/4.5.0.1/index.js';
import { Httpx } from 'https://jslib.k6.io/httpx/0.1.0/index.js';

export const options = {
  thresholds: {
    checks: [{
      threshold:   'rate == 1.00',
      abortOnFail: true
    }],
    http_req_duration: ['p(95)<100'] // 95% of requests should be below 100ms
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
  describe('create a null user', () => {
    const resp = session.post(`/users`, null,
        { headers: { 'Content-Type': 'application/json' } });

    expect(resp.status, 'status').to.equal(400);
    expect(resp.body, 'response body').to.equal('Cannot post a null user. (User: null)');
  });

  describe('create a user without an email address', () => {
    const resp = session.post(`/users`, JSON.stringify({ email: null }),
        { headers: { 'Content-Type': 'application/json' } });

    expect(resp.status).to.equal(400);
    expect(resp.body).to.equal('Cannot post a user without an email address. (User: null)');
  });

  describe('create a user with an invalid email address', () => {
    const resp = session.post(`/users`, JSON.stringify({ email: { address: 'invalid' } }),
        { headers: { 'Content-Type': 'application/json' } });

    expect(resp.status).to.equal(400);
    expect(resp.body).to.equal('Invalid email address format. Please try again. (User: invalid)');
  });

  describe('create a user with invalid properties', () => {
    const resp = session.post(`/users`, JSON.stringify({ email: { address: 'test@test.com' }, password: 'test' }),
        { headers: { 'Content-Type': 'application/json' } });

    expect(resp.status).to.equal(400);
    expect(resp.body).to.equal('Cannot post a user with invalid properties. (User: test@test.com)');
  });

  describe('get a user without an email address', () => {
    const resp = session.get(`/users`, null,
        { headers: { 'password': 'password' } });

    expect(resp.status).to.equal(400);
    expect(resp.body).to.equal('Incorrect or missing email query parameter. (User: null)');
  });

  describe('get a user with an empty email address', () => {
    const resp = session.get(`/users?email=`, null,
        { headers: { 'password': 'password' } });

    expect(resp.status).to.equal(400);
    expect(resp.body).to.equal('Incorrect or missing email query parameter. (User: )');
  });

  describe('get a user without a password header', () => {
    const resp = session.get(`/users?email=success@simulator.amazonses.com`);

    expect(resp.status).to.equal(400);
    expect(resp.body)
        .to.equal('Credentials are required to access this resource. (User: success@simulator.amazonses.com)');
  });

  describe('get a user with an empty password header', () => {
    const resp = session.get(`/users?email=success@simulator.amazonses.com`, null,
        { headers: { 'password': '' } });

    expect(resp.status).to.equal(400);
    expect(resp.body)
        .to.equal('Credentials are required to access this resource. (User: success@simulator.amazonses.com)');
  });

  describe('send a verification email to a null email address', () => {
    const resp = session.post(`/verify`, null,
        { headers: { 'password': 'password' } });

    expect(resp.status).to.equal(400);
    expect(resp.body).to.equal('Incorrect or missing email query parameter. (User: null)');
  });

  describe('send a verification email to an empty email address', () => {
    const resp = session.post(`/verify?email=`, null,
        { headers: { 'password': 'password' } });

    expect(resp.status).to.equal(400);
    expect(resp.body).to.equal('Incorrect or missing email query parameter. (User: )');
  });

  describe('send a verification email without a password header', () => {
    const resp = session.post(`/verify?email=success@simulator.amazonses.com`);

    expect(resp.status).to.equal(400);
    expect(resp.body)
        .to.equal('Credentials are required to access this resource. (User: success@simulator.amazonses.com)');
  });

  describe('send a verification email with an empty password header', () => {
    const resp = session.post(`/verify?email=success@simulator.amazonses.com`, null,
        { headers: { 'password': '' } });

    expect(resp.status).to.equal(400);
    expect(resp.body)
        .to.equal('Credentials are required to access this resource. (User: success@simulator.amazonses.com)');
  });

  describe('verify a null email address', () => {
    const resp = session.get(`/verify?token=testToken`);

    expect(resp.status).to.equal(400);
    expect(resp.body).to.equal('Incorrect or missing email query parameter. (User: null)');
  });

  describe('verify an empty email address', () => {
    const resp = session.get(`/verify?email=&token=testToken`);

    expect(resp.status).to.equal(400);
    expect(resp.body).to.equal('Incorrect or missing email query parameter. (User: )');
  });

  describe('verify with a null token', () => {
    const resp = session.get(`/verify?email=success@simulator.amazonses.com`);

    expect(resp.status).to.equal(400);
    expect(resp.body)
        .to.equal('Incorrect or missing verification token query parameter. (User: success@simulator.amazonses.com)');
  });

  describe('verify with an empty token', () => {
    const resp = session.get(`/verify?email=success@simulator.amazonses.com&token=`);

    expect(resp.status).to.equal(400);
    expect(resp.body)
        .to.equal('Incorrect or missing verification token query parameter. (User: success@simulator.amazonses.com)');
  });

  describe('reset verification with a null email', () => {
    const resp = session.post(`/verify/reset`, null,
        { headers: { 'password': 'password' } });

    expect(resp.status).to.equal(400);
    expect(resp.body).to.equal('Incorrect or missing email query parameter. (User: null)');
  });

  describe('reset verification with an empty email', () => {
    const resp = session.post(`/verify/reset?email=`, null,
        { headers: { 'password': 'password' } });

    expect(resp.status).to.equal(400);
    expect(resp.body).to.equal('Incorrect or missing email query parameter. (User: )');
  });

  describe('reset verification without the password header', () => {
    const resp = session.post(`/verify/reset?email=success@simulator.amazonses.com`);

    expect(resp.status).to.equal(400);
    expect(resp.body)
        .to.equal('Credentials are required to access this resource. (User: success@simulator.amazonses.com)');
  });

  describe('reset verification with an empty password header', () => {
    const resp = session.post(`/verify/reset?email=success@simulator.amazonses.com`, null,
        { headers: { 'password': '' } });

    expect(resp.status).to.equal(400);
    expect(resp.body)
        .to.equal('Credentials are required to access this resource. (User: success@simulator.amazonses.com)');
  });

  describe('update a user without the password header', () => {
    const resp = session.put(`/users`, JSON.stringify(validUserForUpdate),
        { headers: { 'Content-Type': 'application/json' } });

    expect(resp.status).to.equal(400);
    expect(resp.body).to.equal('Credentials are required to access this resource. (User: test@test.com)');
  });

  describe('update a user with an empty password header', () => {
    const resp = session.put(`/users`, JSON.stringify(validUserForUpdate),
        { headers: { 'Content-Type': 'application/json', 'password': '' } });

    expect(resp.status).to.equal(400);
    expect(resp.body).to.equal('Credentials are required to access this resource. (User: test@test.com)');
  });

  describe('update with a null user object', () => {
    const resp = session.put(`/users`, null,
        { headers: { 'Content-Type': 'application/json', 'password': 'password' } });

    expect(resp.status).to.equal(400);
    expect(resp.body).to.equal('Cannot post a null user. (User: null)');
  });

  describe('update with a user object that does not have an email address', () => {
    const resp = session.put(`/users`, JSON.stringify(missingEmailUser),
        { headers: { 'Content-Type': 'application/json', 'password': 'password' } });

    expect(resp.status).to.equal(400);
    expect(resp.body).to.equal('Cannot post a user without an email address. (User: null)');
  });

  describe('update with a user object that has an invalid email address', () => {
    const resp = session.put(`/users`, JSON.stringify(invalidEmailUser),
        { headers: { 'Content-Type': 'application/json', 'password': 'password' } });

    expect(resp.status).to.equal(400);
    expect(resp.body).to.equal('Invalid email address format. Please try again. (User: invalid)');
  });

  describe('update with a user object that has invalid properties', () => {
    const resp = session.put(`/users`, JSON.stringify(invalidPropertiesUser),
        { headers: { 'Content-Type': 'application/json', 'password': 'password' } });

    expect(resp.status).to.equal(400);
    expect(resp.body).to.equal('Cannot post a user with invalid properties. (User: test@test.com)');
  });

  describe('delete a null email address', () => {
    const resp = session.delete(`/users`, null, { headers: { 'password': 'password' } });

    expect(resp.status).to.equal(400);
    expect(resp.body).to.equal('Incorrect or missing email query parameter. (User: null)');
  });

  describe('delete an empty email address', () => {
    const resp = session.delete(`/users?email=`, null, { headers: { 'password': 'password' } });

    expect(resp.status).to.equal(400);
    expect(resp.body).to.equal('Incorrect or missing email query parameter. (User: )');
  });

  describe('delete a user without a password header', () => {
    const resp = session.delete(`/users?email=test@test.com`);

    expect(resp.status).to.equal(400);
    expect(resp.body).to.equal('Credentials are required to access this resource. (User: test@test.com)');
  });

  describe('delete a user with an empty password header', () => {
    const resp = session.delete(`/users?email=test@test.com`, null, { headers: { 'password': '' } });

    expect(resp.status).to.equal(400);
    expect(resp.body).to.equal('Credentials are required to access this resource. (User: test@test.com)');
  });
}
