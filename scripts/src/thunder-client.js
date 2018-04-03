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
 * The ThunderClient class provides methods to talk to Thunder with.
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
   * @param {object} body - The request body to POST.
   * @param {function} callback - The function to call when the method completes.
   * @param {boolean} verbose - Whether to print verbose logs or not. Defaults to false.
   */
  createUser(body, callback, verbose=false) {
    this.baseRequest.post({
      url:  '/users',
      body: body,
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
   * @param {object} params - The query parameters to attach to the URL.
   * @param {object} headers - The request headers to add to the request.
   * @param {function} callback - The function to call when the method completes.
   * @param {boolean} verbose - Whether to print verbose logs or not. Defaults to false.
   */
  getUser(params, headers, callback, verbose=false) {
    this.baseRequest.get({
      url:     '/users',
      headers: headers,
      qs:      params
    }, (err, res, body) => {
      if (err) return callback(err);

      return checkResponse(res, body, Method.GET, callback, verbose);
    });
  }

  /**
   * PUT /users
   * Updates a Thunder user.
   *
   * @param {object} params - The query parameters to attach to the URL.
   * @param {object} body - The request body to PUT.
   * @param {object} headers - The request headers to add to the request.
   * @param {function} callback - The function to call when the method completes.
   * @param {boolean} verbose - Whether to print verbose logs or not. Defaults to false.
   */
  updateUser(params, body, headers, callback, verbose=false) {
    this.baseRequest.put({
      url:     '/users',
      headers: headers,
      qs:      params,
      body:    body,
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
   * @param {object} params - The query parameters to attach to the URL.
   * @param {object} headers - The request headers to add to the request.
   * @param {function} callback - The function to call when the method completes.
   * @param {boolean} verbose - Whether to print verbose logs or not. Defaults to false.
   */
  deleteUser(params, headers, callback, verbose=false) {
    this.baseRequest.delete({
      url:     '/users',
      headers: headers,
      qs:      params
    }, (err, res, body) => {
      if (err) return callback(err);

      return checkResponse(res, body, Method.DELETE, callback, verbose);
    });
  }

  /**
   * POST /verify
   * Creates a new verification email.
   *
   * @param {object} params - The query parameters to attach to the URL.
   * @param {object} headers - The request headers to add to the request.
   * @param {function} callback - The function to call when the method completes.
   * @param {boolean} verbose - Whether to print verbose logs or not. Defaults to false.
   */
  sendEmail(params, headers, callback, verbose=false) {
    this.baseRequest.post({
      url:     '/verify',
      headers: headers,
      qs:      params
    }, (err, res, body) => {
      if (err) return callback(err);

      return checkResponse(res, body, Method.EMAIL, callback, verbose);
    });
  }

  /**
   * GET /verify
   * Verifies a user. Simulates the user clicking the URL in the email.
   *
   * @param {object} params - The query parameters to attach to the URL.
   * @param {object} headers - The request headers to add to the request.
   * @param {function} callback - The function to call when the method completes.
   * @param {boolean} verbose - Whether to print verbose logs or not. Defaults to false.
   */
  verifyUser(params, headers, callback, verbose=false) {
    this.baseRequest.get({
      url:     '/verify',
      headers: headers,
      qs:      params
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

