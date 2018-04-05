const request = require('request');

const Method = {
  POST:   { name: 'POST', expected: 201 },
  GET:    { name: 'GET', expected: 200 },
  PUT:    { name: 'PUT', expected: 200 },
  DELETE: { name: 'DELETE', expected: 200 },
  VERIFY: { name: 'VERIFY', expected: 200 },
  EMAIL:  { name: 'EMAIL', expected: 200 }
};

/**
 * The ThunderClient class provides methods to communicate to Thunder.
 */
class ThunderClient {
  /**
   * Constructs a new ThunderClient.
   * @constructor
   *
   * @param {string} endpoint - The base endpoint to connect to Thunder with.
   * @param {string} application - The name of the application key used for basic auth.
   * @param {string} secret - The value of the application secret used for basic auth.
   */
  constructor(endpoint, application, secret) {
    this.baseRequest = request.defaults({
      baseUrl: endpoint,
      auth:    { username: application, password: secret }
    });
  }

  /**
   * POST /users
   * Creates a new Thunder user.
   *
   * @param {object} user - The data of the user object to create.
   * @param {function} callback - The function to call when the method completes.
   */
  createUser(user, callback) {
    this.baseRequest.post({
      url:  '/users',
      body: user,
      json: true
    }, (err, res, body) => {
      if (err) return callback(err);

      return checkResponse(res, body, Method.POST, callback);
    });
  }

  /**
   * GET /users
   * Gets a Thunder user.
   *
   * @param {string} email - The email address of the user to get.
   * @param {string} password - The password of the user to get.
   * @param {function} callback - The function to call when the method completes.
   */
  getUser(email, password, callback) {
    this.baseRequest.get({
      url:     '/users',
      headers: { password: password },
      qs:      { email: email }
    }, (err, res, body) => {
      if (err) return callback(err);

      return checkResponse(res, body, Method.GET, callback);
    });
  }

  /**
   * PUT /users
   * Updates a Thunder user.
   *
   * @param {string} email - The existing email address of the user to update.
   * @param {string} password - The password of the user to update.
   * @param {object} user - The user object to PUT as an update.
   * @param {function} callback - The function to call when the method completes.
   */
  updateUser(email, password, user, callback) {
    this.baseRequest.put({
      url:     '/users',
      headers: { password: password },
      qs:      (email !== null && email !== '') ? { email: email } : {},
      body:    user,
      json:    true
    }, (err, res, body) => {
      if (err) return callback(err);

      return checkResponse(res, body, Method.PUT, callback);
    });
  }

  /**
   * DELETE /users
   * Deletes a Thunder user.
   *
   * @param {string} email - The email address of the user to delete.
   * @param {string} password - The password of the user to delete.
   * @param {function} callback - The function to call when the method completes.
   */
  deleteUser(email, password, callback) {
    this.baseRequest.delete({
      url:     '/users',
      headers: { password: password },
      qs:      { email: email }
    }, (err, res, body) => {
      if (err) return callback(err);

      return checkResponse(res, body, Method.DELETE, callback);
    });
  }

  /**
   * POST /verify
   * Creates and sends a new verification email.
   *
   * @param {string} email - The email address of the user to send the email to.
   * @param {string} password - The password of the user to send the email to.
   * @param {function} callback - The function to call when the method completes.
   */
  sendEmail(email, password, callback) {
    this.baseRequest.post({
      url:     '/verify',
      headers: { password: password },
      qs:      { email: email }
    }, (err, res, body) => {
      if (err) return callback(err);

      return checkResponse(res, body, Method.EMAIL, callback);
    });
  }

  /**
   * GET /verify
   * Verifies a user. Simulates the user clicking the URL in the email.
   *
   * @param {string} email - The email address of the user to verify.
   * @param {string} token - The verification token that should match the generated token.
   * @param {function} callback - The function to call when the method completes.
   */
  verifyUser(email, token, callback) {
    this.baseRequest.get({
      url: '/verify',
      qs:  { email: email, token: token }
    }, (err, res, body) => {
      if (err) return callback(err);

      return checkResponse(res, body, Method.VERIFY, callback);
    });
  }
}

/**
 * Checks if a Thunder response was successful.
 *
 * @param {object} res - The response object to verify.
 * @param {object} body - The reponse body.
 * @param {Method} method - The method that was called to produce the response.
 * @param {function} callback - The function to call when the method completes.
 * @return When the response check is complete.
 */
function checkResponse(res, body, method, callback) {
  if (res.statusCode !== method.expected) {
    return callback(
      new Error('The status code ' + res.statusCode
        + ' does not match expected ' + method.expected), body);
  }

  let result;

  try {
    result = JSON.parse(body);
  } catch (e) {
    result = body;
  }

  return callback(null, result);
}

module.exports = ThunderClient;

