var request = require('request');

var Method = {
  POST:   {name: 'POST', expected: 201},
  GET:    {name: 'GET', expected: 200},
  PUT:    {name: 'PUT', expected: 200},
  DELETE: {name: 'DELETE', expected: 200},
  VERIFY: {name: 'VERIFY', expected: 200},
  EMAIL:  {name: 'EMAIL', expected: 200}
};

class ThunderClient {
  constructor(endpoint, auth) {
    this.baseRequest = request.defaults({
      baseUrl: endpoint,
      auth: {
        username: auth.application,
        password: auth.secret
      }
    });
  }

  /* POST /users */
  createUser(body, callback, verbose=false) {
    this.baseRequest.post({
      url: '/users',
      body: body,
      json: true
    }, (err, res, body) => {
      if (err) return callback(err);

      return checkResponse(res, body, Method.POST, callback, verbose);
    });
  }

  /* GET /users */
  getUser(params, headers, callback, verbose=false) {
    this.baseRequest.get({
      url: '/users',
      headers: headers,
      qs: params
    }, (err, res, body) => {
      if (err) return callback(err);

      return checkResponse(res, body, Method.GET, callback, verbose);
    });
  }

  /* PUT /users */
  updateUser(params, body, headers, callback, verbose=false) {
    this.baseRequest.put({
      url: '/users',
      headers: headers,
      qs: params,
      body: body,
      json: true
    }, (err, res, body) => {
      if (err) return callback(err);

      return checkResponse(res, body, Method.PUT, callback, verbose);
    });
  }

  /* DELETE /users */
  deleteUser(params, headers, callback, verbose=false) {
    this.baseRequest.delete({
      url: '/users',
      headers: headers,
      qs: params
    }, (err, res, body) => {
      if (err) return callback(err);

      return checkResponse(res, body, Method.DELETE, callback, verbose);
    });
  }

  /* POST /verify */
  sendEmail(params, headers, callback, verbose=false) {
    this.baseRequest.post({
      url: '/verify',
      headers: headers,
      qs: params
    }, (err, res, body) => {
      if (err) return callback(err);

      return checkResponse(res, body, Method.EMAIL, callback, verbose);
    });
  }

  /* GET /verify */
  verifyUser(params, headers, callback, verbose=false) {
    this.baseRequest.get({
      url: '/verify',
      headers: headers,
      qs: params
    }, (err, res, body) => {
      if (err) return callback(err);

      return checkResponse(res, body, Method.VERIFY, callback, verbose);
    });
  }
}

function checkResponse(res, body, method, callback, verbose=false) {
  if (res.statusCode !== method.expected) {
    console.log('An error occurred while performing method %s', method.name);

    if (verbose) {
      console.log('Details:');
      console.log(body);
    }

    console.log('\n');
    return callback(new Error('The status code ' + res.statusCode + ' does not match expected ' + method.expected));
  }

  console.log('Successfully completed method %s', method.name);
  try {
    var result = JSON.parse(body);
  } catch (e) {
    var result = body;
  }

  if (verbose) {
    console.log('Response:');
    console.log(result);
  }

  console.log('\n');
  return callback(null, result);
}

module.exports = ThunderClient;

