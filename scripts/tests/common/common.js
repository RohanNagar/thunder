import { describe } from 'https://jslib.k6.io/expect/0.0.5/index.js';
import { Httpx } from 'https://jslib.k6.io/httpx/0.0.5/index.js';

const credentials = 'application:secret';
const session = new Httpx({ baseURL: `http://${credentials}@localhost:8080` });
const adminSession = new Httpx({ baseURL: `http://localhost:8081` });

const metricNames = {
  deleteRequestMeter:
    'com.sanctionco.thunder.resources.UserResource.delete-requests',
  getRequestMeter:
    'com.sanctionco.thunder.resources.UserResource.get-requests',
  createRequestMeter:
    'com.sanctionco.thunder.resources.UserResource.post-requests',
  updateRequestMeter:
    'com.sanctionco.thunder.resources.UserResource.update-requests',
  resetVerificationRequestMeter:
    'com.sanctionco.thunder.resources.VerificationResource.reset-verification-requests',
  sendEmailRequestMeter:
    'com.sanctionco.thunder.resources.VerificationResource.send-email-requests',
  verifyEmailRequestMeter:
    'com.sanctionco.thunder.resources.VerificationResource.verify-email-requests',
  swaggerUIRequestMeter:
    'com.sanctionco.thunder.openapi.SwaggerResource.swagger-ui-requests',
  emailSendSuccessCounter:
    'com.sanctionco.thunder.email.EmailService.email-send-success'
};

// TODO: make these the same message
const conflictMessagePerTest = {
  dynamodb: 'ConditionalCheck failed for insert/update. If this is an update, try again. If ' +
      'this is a new user, a user with the same email address already exists. ' +
      '(User: success@simulator.amazonses.com)',
  mongodb: 'A user with the same email address already exists. (User: success@simulator.amazonses.com)',
  inmemorydb: 'A user with the same email address already exists. (User: success@simulator.amazonses.com)'
};

export function fullTest(testName, runMetricsTest = true) {
  const user = {
    email:      { address: 'success@simulator.amazonses.com' },
    password:   '5f4dcc3b5aa765d61d8327deb882cf99',
    uniqueID:   'ABC123',
    attributes: ['hello', 'world']
  };

  const userWithUpdatedProperties = {
    email:      { address: 'success@simulator.amazonses.com', verificationToken: 'something-else' },
    password:   '5f4dcc3b5aa765d61d8327deb882cf99',
    uniqueID:   'NEW_ID',
    attributes: ['hello', 'world']
  };

  const userWithUpdatedEmail = {
    email: {
      address:           'newemail@gmail.com',
      verificationToken: 'something-else',
      verified:          true
    },
    password:   '5f4dcc3b5aa765d61d8327deb882cf99',
    uniqueID:   'NEW_ID',
    attributes: ['hello', 'world']
  };

  let initialCreationTime = 0;
  let verificationToken = '';

  describe('create a new user', (t) => {
    const resp = session.post(`/users`, JSON.stringify(user),
        { headers: { 'Content-Type': 'application/json' } });

    t.expect(resp.status).as('status').toEqual(201)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(user.email.address)
        .and(resp.json('creationTime')).as('creation time').toBeGreaterThan(0)
        .and(resp.json('lastUpdateTime')).as('last update time').toBeGreaterThan(0);

    initialCreationTime = resp.json('creationTime');
  }) &&

  describe('create a conflicting user', (t) => {
    const resp = session.post(`/users`, JSON.stringify(user),
        { headers: { 'Content-Type': 'application/json' } });

    t.expect(resp.status).as('status').toEqual(409)
        .and(resp.body).as('body').toEqual(conflictMessagePerTest[testName]);
  }) &&

  describe('attempt to get a nonexistent user', (t) => {
    const resp = session.get(`/users?email=test@test.com`, null,
        { headers: { 'password': 'password' } });

    t.expect(resp.status).as('status').toEqual(404)
        .and(resp.body).as('body')
        .toEqual('User not found in the database. (User: test@test.com)');
  }) &&

  describe('attempt to get a user with an incorrect password', (t) => {
    const resp = session.get(`/users?email=${user.email.address}`, null,
        { headers: { 'password': 'password' } });

    t.expect(resp.status).as('status').toEqual(401)
        .and(resp.body).as('body')
        .toEqual(`Unable to validate user with provided credentials. (User: ${user.email.address})`);
  }) &&

  describe('get the user', (t) => {
    const resp = session.get(`/users?email=${user.email.address}`, null,
        { headers: { 'password': user.password } });

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(user.email.address)
        .and(resp.json('creationTime')).as('creation time').toEqual(initialCreationTime)
        .and(resp.json('lastUpdateTime')).as('last update time').toBeGreaterThan(0);
  }) &&

  describe('get the user with a password mistake', (t) => {
    const resp = session.get(`/users?email=${user.email.address}`, null,
        { headers: { 'password': '-' + user.password } });

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(user.email.address);
  }) &&

  describe('send a verification email to a nonexistent user', (t) => {
    const resp = session.post(`/verify?email=test@test.com`, null,
        { headers: { 'password': 'password' } });

    t.expect(resp.status).as('status').toEqual(404)
        .and(resp.body).as('body')
        .toEqual('User not found in the database. (User: test@test.com)');
  }) &&

  describe('send a verification email with an incorrect password', (t) => {
    const resp = session.post(`/verify?email=${user.email.address}`, null,
        { headers: { 'password': 'password' } });

    t.expect(resp.status).as('status').toEqual(401)
        .and(resp.body).as('body')
        .toEqual(`Unable to validate user with provided credentials. (User: ${user.email.address})`);
  }) &&

  describe('send a verification email', (t) => {
    const resp = session.post(`/verify?email=${user.email.address}`, null,
        { headers: { 'password': user.password } });

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(user.email.address)
        .and(resp.json('creationTime')).as('creation time').toEqual(initialCreationTime)
        .and(resp.json('lastUpdateTime')).as('last update time').toBeGreaterThan(initialCreationTime);

    verificationToken = resp.json('email.verificationToken');
  }) &&

  describe('verification with an incorrect token', (t) => {
    const resp = session.get(`/verify?email=${user.email.address}&token=incorrect`, null);

    t.expect(resp.status).as('status').toEqual(400)
        .and(resp.body).as('body')
        .toEqual(`Incorrect verification token. (User: ${user.email.address})`);
  }) &&

  describe('verify the user', (t) => {
    const resp = session.get(`/verify?email=${user.email.address}&token=${verificationToken}`, null);

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(user.email.address)
        .and(resp.json('creationTime')).as('creation time').toEqual(initialCreationTime)
        .and(resp.json('lastUpdateTime')).as('last update time').toBeGreaterThan(initialCreationTime)
        .and(resp.json('email.verified')).as('verified status').toBeTruthy();
  }) &&

  describe('verify the user and retrieve HTML', (t) => {
    const resp = session.get(`/verify?email=${user.email.address}&token=${verificationToken}&response_type=html`, null);

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp.html().find('div > div').text()).as('success html text')
        .toEqual('Success! Your account has been verified.');
  }) &&

  describe('reset verification status of a nonexistent user', (t) => {
    const resp = session.post(`/verify/reset?email=test@test.com`, null,
        { headers: { 'password': 'password' } });

    t.expect(resp.status).as('status').toEqual(404)
        .and(resp.body).as('body')
        .toEqual('User not found in the database. (User: test@test.com)');
  }) &&

  describe('reset verification status with an incorrect password', (t) => {
    const resp = session.post(`/verify/reset?email=${user.email.address}`, null,
        { headers: { 'password': 'password' } });

    t.expect(resp.status).as('status').toEqual(401)
        .and(resp.body).as('body')
        .toEqual(`Unable to validate user with provided credentials. (User: ${user.email.address})`);
  }) &&

  describe('reset verification status of the user', (t) => {
    const resp = session.post(`/verify/reset?email=${user.email.address}`, null,
        { headers: { 'password': user.password } });

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(user.email.address)
        .and(resp.json('creationTime')).as('creation time').toEqual(initialCreationTime)
        .and(resp.json('lastUpdateTime')).as('last update time').toBeGreaterThan(initialCreationTime)
        .and(resp.json('email.verified')).as('verified status').toEqual(false);
  }) &&

  describe('send another verification email', (t) => {
    const resp = session.post(`/verify?email=${user.email.address}`, null,
        { headers: { 'password': user.password } });

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(user.email.address)
        .and(resp.json('creationTime')).as('creation time').toEqual(initialCreationTime)
        .and(resp.json('lastUpdateTime')).as('last update time').toBeGreaterThan(initialCreationTime)
        .and(resp.json('email.verified')).as('verified status').toEqual(false);

    verificationToken = resp.json('email.verificationToken');
  }) &&

  describe('re-verify the user', (t) => {
    const resp = session.get(`/verify?email=${user.email.address}&token=${verificationToken}`, null);

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(user.email.address)
        .and(resp.json('creationTime')).as('creation time').toEqual(initialCreationTime)
        .and(resp.json('lastUpdateTime')).as('last update time').toBeGreaterThan(initialCreationTime)
        .and(resp.json('email.verified')).as('verified status').toBeTruthy();
  }) &&

  describe('update a nonexistent user', (t) => {
    const resp = session.put(`/users?email=test@test.com`, JSON.stringify(userWithUpdatedProperties),
        { headers: { 'Content-Type': 'application/json', 'password': 'password' } });

    t.expect(resp.status).as('status').toEqual(404)
        .and(resp.body).as('body')
        .toEqual('User not found in the database. (User: test@test.com)');
  }) &&

  describe('update a user with an incorrect password', (t) => {
    const resp = session.put(`/users`, JSON.stringify(userWithUpdatedProperties),
        { headers: { 'Content-Type': 'application/json', 'password': 'password' } });

    t.expect(resp.status).as('status').toEqual(401)
        .and(resp.body).as('body')
        .toEqual(`Unable to validate user with provided credentials. (User: ${user.email.address})`);
  }) &&

  describe('update the user properties', (t) => {
    const resp = session.put(`/users`, JSON.stringify(userWithUpdatedProperties),
        { headers: { 'Content-Type': 'application/json', 'password': user.password } });

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(user.email.address)
        .and(resp.json('creationTime')).as('creation time').toEqual(initialCreationTime)
        .and(resp.json('lastUpdateTime')).as('last update time').toBeGreaterThan(initialCreationTime)
        .and(resp.json('email.verificationToken')).as('updated response verification token').toEqual(verificationToken)
        .and(resp.json('email.verified')).as('verified status').toBeTruthy();
  }) &&

  describe('update the user email', (t) => {
    const resp = session.put(`/users?email=${user.email.address}`, JSON.stringify(userWithUpdatedEmail),
        { headers: { 'Content-Type': 'application/json', 'password': user.password } });

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(userWithUpdatedEmail.email.address)
        .and(resp.json('creationTime')).as('creation time').toBeGreaterThan(initialCreationTime)
        .and(resp.json('lastUpdateTime')).as('last update time').toBeGreaterThan(initialCreationTime)
        .and(resp.json('email.verificationToken')).as('new email verification token').toEqual(null)
        .and(resp.json('email.verified')).as('verified status').toEqual(false);

    // TODO should creationTime stay the same when updating email?
    // TODO right now we are updating it
    initialCreationTime = resp.json('creationTime');
  }) &&

  describe('delete a nonexistent user', (t) => {
    const resp = session.delete(`/users?email=test@test.com`, null,
        { headers: { 'password': 'password' } });

    t.expect(resp.status).as('status').toEqual(404)
        .and(resp.body).as('body')
        .toEqual('User not found in the database. (User: test@test.com)');
  }) &&

  describe('delete a user with an incorrect password', (t) => {
    const resp = session.delete(`/users?email=${userWithUpdatedEmail.email.address}`, null,
        { headers: { 'password': 'password' } });

    t.expect(resp.status).as('status').toEqual(401)
        .and(resp.body).as('body')
        .toEqual(`Unable to validate user with provided credentials. (User: ${userWithUpdatedEmail.email.address})`);
  }) &&

  describe('delete the user', (t) => {
    const resp = session.delete(`/users?email=${userWithUpdatedEmail.email.address}`, null,
        { headers: { 'password': userWithUpdatedEmail.password } });

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(userWithUpdatedEmail.email.address)
        .and(resp.json('creationTime')).as('creation time').toEqual(initialCreationTime)
        .and(resp.json('lastUpdateTime')).as('last update time').toBeGreaterThan(0);
  }) &&

  describe('get the OpenAPI json', (t) => {
    const resp = session.get(`/openapi.json`);

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('info.title')).as('OpenAPI title').toEqual('Thunder API')
        .and(resp.json('info.description')).as('OpenAPI descripion')
        .toEqual('A fully customizable user management REST API');
  }) &&

  describe('get the OpenAPI yaml', (t) => {
    const resp = session.get(`/openapi.yaml`);

    t.expect(resp.status).as('status').toEqual(200);
  }) &&

  describe('get the Swagger UI HTML', (t) => {
    const resp = session.get(`/swagger`);

    t.expect(resp.status).as('status').toEqual(200);
  }) &&

  describe('verifying application health', (t) => {
    const resp = adminSession.get(`/healthcheck`);

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('Database.healthy')).as('database health').toEqual(true)
        .and(resp.json('Email.healthy')).as('email provider health').toEqual(true)
        .and(resp.json('deadlocks.healthy')).as('deadlock health').toEqual(true);
  }) &&

  describe('check metrics', (t) => {
    if (!runMetricsTest) {
      t.expect(true).as('skipped').toEqual(true);

      return;
    }

    const resp = adminSession.get(`/metrics`);

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('meters')[metricNames.deleteRequestMeter].count).as('delete request count').toEqual(3)
        .and(resp.json('meters')[metricNames.getRequestMeter].count).as('get request count').toEqual(4)
        .and(resp.json('meters')[metricNames.createRequestMeter].count).as('create request count').toEqual(2)
        .and(resp.json('meters')[metricNames.updateRequestMeter].count).as('update request count').toEqual(4)
        .and(resp.json('meters')[metricNames.resetVerificationRequestMeter].count).as('reset request count').toEqual(3)
        .and(resp.json('meters')[metricNames.sendEmailRequestMeter].count).as('send email request count').toEqual(4)
        .and(resp.json('meters')[metricNames.verifyEmailRequestMeter].count).as('verify email request count').toEqual(4)
        .and(resp.json('meters')[metricNames.swaggerUIRequestMeter].count).as('swagger UI request count').toEqual(1)
        .and(resp.json('counters')[metricNames.emailSendSuccessCounter].count).as('email success count').toEqual(2);
  });
}

export function serverSideHashTest() {
  const user = {
    email:    { address: 'test@test.com' },
    password: 'password'
  };

  const userWithNewPassword = {
    email:    { address: 'test@test.com' },
    password: 'newpassword'
  };

  let hashedPassword = '';
  let verificationToken = '';

  describe('create a user', (t) => {
    const resp = session.post(`/users`, JSON.stringify(user),
        { headers: { 'Content-Type': 'application/json' } });

    t.expect(resp.status).as('status').toEqual(201)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(user.email.address);

    hashedPassword = resp.json('password');
  }) &&

  describe('get the user', (t) => {
    const resp = session.get(`/users?email=${user.email.address}`, null,
        { headers: { 'password': user.password + '2' } }); // Test with a common mistake password

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(user.email.address)
        .and(resp.json('password')).as('password').toEqual(hashedPassword);
  }) &&

  describe('send a verification email', (t) => {
    const resp = session.post(`/verify?email=${user.email.address}`, null,
        { headers: { 'password': 'Password' } }); // Test with a common mistake password

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

  describe('update the password', (t) => {
    const resp = session.put(`/users`, JSON.stringify(userWithNewPassword),
        { headers: { 'Content-Type': 'application/json', 'password': user.password } });

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(user.email.address)
        .and(resp.json('email.verified')).as('verified').toEqual(true)
        .and(resp.json('email.verificationToken')).as('verificationToken').toEqual(verificationToken)
        .and(resp.json('password') !== hashedPassword).as('new hashed password is different').toEqual(true);

    hashedPassword = resp.json('password');
  }) &&

  describe('get the updated user', (t) => {
    const resp = session.get(`/users?email=${userWithNewPassword.email.address}`, null,
        { headers: { 'password': userWithNewPassword.password } });

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(userWithNewPassword.email.address)
        .and(resp.json('password')).as('password').toEqual(hashedPassword);
  }) &&

  describe('delete the user', (t) => {
    const resp = session.delete(`/users?email=${userWithNewPassword.email.address}`, null,
        { headers: { 'password': userWithNewPassword.password } });

    t.expect(resp.status).as('status').toEqual(200)
        .and(resp).toHaveValidJson()
        .and(resp.json('email.address')).as('email').toEqual(userWithNewPassword.email.address);
  });
}
