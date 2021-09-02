import { describe } from 'https://jslib.k6.io/expect/0.0.5/index.js';
import { Httpx, Request, Get, Post } from 'https://jslib.k6.io/httpx/0.0.5/index.js';

export let options = {
  thresholds: {
    checks: [{threshold: 'rate == 1.00', abortOnFail: true}],
  },
  vus: 1,
  iterations: 1
};

const credentials = 'application:secret';
let session = new Httpx({ baseURL: `http://${credentials}@localhost:8080` });

export default function testSuite() {

  let user = {
    email: { address: 'success@simulator.amazonses.com' },
    password: '5f4dcc3b5aa765d61d8327deb882cf99',
    uniqueID: 'ABC123',
    attributes: ['hello', 'world']
  };

  describe('create a new user', (t) => {
    let resp = session.post(`/users`, JSON.stringify(user),
        { headers: { 'Content-Type': 'application/json' } });

    t.expect(resp.status).as("status").toEqual(201)
      .and(resp).toHaveValidJson()
      .and(resp.json('email.address')).as('email').toEqual(user.email.address);
  })

  &&

  describe('create a conflicting user', (t) => {
    let resp = session.post(`/users`, JSON.stringify(user),
        { headers: { 'Content-Type': 'application/json' } });

    t.expect(resp.status).as("status").toEqual(409)
      .and(resp.body).as('body')
      .toEqual('ConditionalCheck failed for insert/update. If this is an update, try again. If this is a new user, a user with the same email address already exists. (User: success@simulator.amazonses.com)');
  })

  &&

  describe('attempt to get a nonexistent user', (t) => {
    let resp = session.get(`/users?email=test@test.com`, null,
        { headers: { 'password': 'password' }});

    t.expect(resp.status).as("status").toEqual(404)
      .and(resp.body).as('body')
      .toEqual('User not found in the database. (User: test@test.com)');
  })

  &&

  describe('attempt to get a user with an incorrect password', (t) => {
    let resp = session.get(`/users?email=${user.email.address}`, null,
        { headers: { 'password': 'password' }});

    t.expect(resp.status).as("status").toEqual(401)
      .and(resp.body).as('body')
      .toEqual(`Unable to validate user with provided credentials. (User: ${user.email.address})`);
  })

  &&

  describe('get the user', (t) => {
    let resp = session.get(`/users?email=${user.email.address}`, null,
        { headers: { 'password': user.password }});

    t.expect(resp.status).as("status").toEqual(200)
      .and(resp).toHaveValidJson()
      .and(resp.json('email.address')).as('email').toEqual(user.email.address);
  })

  &&

  describe('get the user with a password mistake', (t) => {
    let resp = session.get(`/users?email=${user.email.address}`, null,
        { headers: { 'password': '-' + user.password }});

    t.expect(resp.status).as("status").toEqual(200)
      .and(resp).toHaveValidJson()
      .and(resp.json('email.address')).as('email').toEqual(user.email.address);
  })

  &&

  describe('delete the user', (t) => {
    let resp = session.delete(`/users?email=${user.email.address}`, null,
        { headers: { 'password': user.password }});

    t.expect(resp.status).as("status").toEqual(200)
      .and(resp).toHaveValidJson()
      .and(resp.json('email.address')).as('email').toEqual(user.email.address);
  });

}
