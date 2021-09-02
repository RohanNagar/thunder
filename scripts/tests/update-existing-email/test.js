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

const firstUser = {
  email:    { address: 'first@test.com' },
  password: 'password.one'
};

const secondUser = {
  email:    { address: 'second@test.com' },
  password: 'password.two'
};

export default function testSuite() {
  describe('create the first user', (t) => {
    const resp = session.post(`/users`, JSON.stringify(firstUser),
        { headers: { 'Content-Type': 'application/json' } });

    t.expect(resp.status).as('status').toEqual(201)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(firstUser.email.address);
  }) &&

  describe('create the second user', (t) => {
    const resp = session.post(`/users`, JSON.stringify(secondUser),
        { headers: { 'Content-Type': 'application/json' } });

    t.expect(resp.status).as('status').toEqual(201)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(secondUser.email.address);
  }) &&

  describe('update the first user email to be the same as the second user', (t) => {
    const resp = session.put(`/users?email=${firstUser.email.address}`, JSON.stringify(secondUser),
        { headers: { 'Content-Type': 'application/json', 'password': firstUser.password } });

    t.expect(resp.status).as('status').toEqual(409)
        .and(resp.body).as('error message')
        .toEqual('A user with the new email address already exists. (User: first@test.com)');
  }) &&

  describe('get the first user to ensure it exists', (t) => {
    const resp = session.get(`/users?email=${firstUser.email.address}`, null,
        { headers: { 'password': firstUser.password } });

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(firstUser.email.address);
  });
}
