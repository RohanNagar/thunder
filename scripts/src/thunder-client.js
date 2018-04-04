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
   * @param {boolean} verbose - Whether to print verbose logs or not. Defaults to false.
   */
  createUser(user, callback, verbose=false) {
    this.baseRequest.post({
      url:  '/users',
      body: user,
      json: true
    }, (err, res, body) => {
      if (err) return callback(err);

      return checkResponse(res, body, Method.POST, callback, verbose);
    });
  }

  /**
   * GET /users
   * Gets a Thunder user.
   *
   * @param {string} email - The email address of the user to get.
   * @param {string} password - The password of the user to get.
   * @param {function} callback - The function to call when the method completes.
   * @param {boolean} verbose - Whether to print verbose logs or not. Defaults to false.
   */
  getUser(email, password, callback, verbose=false) {
    this.baseRequest.get({
      url:     '/users',
      headers: { password: password },
      qs:      { email: email }
    }, (err, res, body) => {
      if (err) return callback(err);

      return checkResponse(res, body, Method.GET, callback, verbose);
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
   * @param {boolean} verbose - Whether to print verbose logs or not. Defaults to false.
   */
  updateUser(email, password, user, callback, verbose=false) {
    this.baseRequest.put({
      url:     '/users',
      headers: { password: password },
      qs:      (email !== null && email !== '') ? { email: email } : {},
      body:    user,
      json:    true
    }, (err, res, body) => {
      if (err) return callback(err);

      return checkResponse(res, body, Method.PUT, callback, verbose);
    });
  }

  /**
   * DELETE /users
   * Deletes a Thunder user.
   *
   * @param {string} email - The email address of the user to delete.
   * @param {string} password - The password of the user to delete.
   * @param {function} callback - The function to call when the method completes.
   * @param {boolean} verbose - Whether to print verbose logs or not. Defaults to false.
   */
  deleteUser(email, password, callback, verbose=false) {
    this.baseRequest.delete({
      url:     '/users',
      headers: { password: password },
      qs:      { email: email }
    }, (err, res, body) => {
      if (err) return callback(err);

      return checkResponse(res, body, Method.DELETE, callback, verbose);
    });
  }

  /**
   * POST /verify
   * Creates and sends a new verification email.
   *
   * @param {string} email - The email address of the user to send the email to.
   * @param {string} password - The password of the user to send the email to.
   * @param {function} callback - The function to call when the method completes.
   * @param {boolean} verbose - Whether to print verbose logs or not. Defaults to false.
   */
  sendEmail(email, password, callback, verbose=false) {
    this.baseRequest.post({
      url:     '/verify',
      headers: { password: password },
      qs:      { email: email }
    }, (err, res, body) => {
      if (err) return callback(err);

      return checkResponse(res, body, Method.EMAIL, callback, verbose);
    });
  }

  /**
   * GET /verify
   * Verifies a user. Simulates the user clicking the URL in the email.
   *
   * @param {string} email - The email address of the user to verify.
   * @param {string} token - The verification token that should match the generated token.
   * @param {string} password - The password of the user to verify.
   * @param {function} callback - The function to call when the method completes.
   * @param {boolean} verbose - Whether to print verbose logs or not. Defaults to false.
   */
  verifyUser(email, token, password, callback, verbose=false) {
    this.baseRequest.get({
      url:     '/verify',
      headers: { password: password },
      qs:      { email: email, token: token }
    }, (err, res, body) => {
      if (err) return callback(err);

      return checkResponse(res, body, Method.VERIFY, callback, verbose);
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
 * @param {boolean} verbose - Whether to print verbose logs or not. Defaults to false.
 * @return When the response check is complete.
 */
function checkResponse(res, body, method, callback, verbose=false) {
  if (res.statusCode !== method.expected) {
    console.log('An error occurred while performing method %s', method.name);

    if (verbose) {
      console.log('Details:');
      console.log(body);
    }

    console.log('\n');
    return callback(
      new Error('The status code ' + res.statusCode
        + ' does not match expected ' + method.expected));
  }

  console.log('Successfully completed method %s', method.name);
  let result;

  try {
    result = JSON.parse(body);
  } catch (e) {
    result = body;
  }

  if (verbose) {
    console.log('Response:');
    console.log(result);
  }

  console.log('\n');
  return callback(null, result);
}

module.exports = ThunderClient;

