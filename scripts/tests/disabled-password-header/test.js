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

export default function testSuite() {
  const user = {
    email:    { address: 'test@test.com' },
    password: 'password'
  };

  const updatedUser = {
    email:    { address: 'test@test.com' },
    password: 'newpassword'
  };

  let verificationToken = '';

  describe('create a user', (t) => {
    const resp = session.post(`/users`, JSON.stringify(user),
        { headers: { 'Content-Type': 'application/json' } });

    t.expect(resp.status).as('status').toEqual(201)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(user.email.address);
  }) &&

  describe('get the user without the password header', (t) => {
    const resp = session.get(`/users?email=${user.email.address}`);

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(user.email.address);
  }) &&

  describe('send a verification email with an incorrect password header', (t) => {
    const resp = session.post(`/verify?email=${user.email.address}`, null,
        { headers: { 'password': 'incorrect' } });

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(user.email.address);

    verificationToken = resp.json('email.verificationToken');
  }) &&

  describe('verify the user', (t) => {
    const resp = session.get(`/verify?email=${user.email.address}&token=${verificationToken}`);

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(user.email.address)
        .and(resp.json('email.verified')).as('verified').toEqual(true);
  }) &&

  describe('update the user without a password header', (t) => {
    const resp = session.put(`/users`, JSON.stringify(updatedUser),
        { headers: { 'Content-Type': 'application/json' } });

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(updatedUser.email.address);
  }) &&

  describe('get the updated user with the old password header', (t) => {
    const resp = session.get(`/users?email=${updatedUser.email.address}`, null,
        { headers: { 'password': user.password } });

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(updatedUser.email.address);
  }) &&

  describe('reset verification status without the password header', (t) => {
    const resp = session.post(`/verify/reset?email=${updatedUser.email.address}`);

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(updatedUser.email.address)
        .and(resp.json('email.verified')).as('verified').toEqual(false);
  }) &&

  describe('delete the user without the password header', (t) => {
    const resp = session.delete(`/users?email=${updatedUser.email.address}`);

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(updatedUser.email.address);
  });
}
