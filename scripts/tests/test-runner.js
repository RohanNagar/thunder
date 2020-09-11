const ArgumentParser  = require('argparse').ArgumentParser;
const responseHandler = require('../lib/response-handler');
const ThunderClient   = require('thunder-client');
const request         = require('request');
const YAML            = require('js-yaml');
const async           = require('async');
const path            = require('path');
const fs              = require('fs');

const parser = new ArgumentParser({
  add_help:    true,
  description: 'Runs integration tests for Thunder'
});

// -- Add command line args --
parser.add_argument('-v', '--version', {
  action:  'version',
  version: '1.0.0'
});

parser.add_argument('testFile', {
  help: 'The name of the file containing test cases'
});

parser.add_argument('-e', '--endpoint', {
  help:    'The base endpoint to connect to',
  default: 'http://localhost:8080' });

parser.add_argument('--admin-endpoint', {
  help:    'The admin endpoint to connect to',
  default: 'http://localhost:8081',
  dest:    'adminEndpoint' });

parser.add_argument('-a', '--auth', {
  help:    'Authentication credentials to connect to the endpoint',
  default: 'application:secret' });

parser.add_argument('-m', '--metrics', {
  help:   'Run any defined metrics tests',
  action: 'store_true',
  dest:   'runMetrics' });

parser.add_argument('-vb', '--verbose', {
  help:   'Increase output verbosity',
  action: 'store_true' });

const args = parser.parse_args();

// -- Read test config --
const tests = YAML.safeLoad(fs.readFileSync(path.join(process.cwd(), args.testFile)));

// -- Separate auth --
const auth = {
  application: args.auth.split(':')[0],
  secret:      args.auth.split(':')[1]
};

// -- Create Thunder object --
const thunder = new ThunderClient(args.endpoint, auth.application, auth.secret);

// -- Hold all created users --
const createdUsers = [];

// -- Be able to get the generated token of the user from the test --
function getTokenFromTest(test, skipExisting=false) {
  let address = null;

  if (test.email) {
    address = test.email;
  } else if (!skipExisting && test.existingEmail) {
    address = test.existingEmail;
  } else if (test.body) {
    address = test.body.email.address;
  }

  if (!address || !createdUsers[address]) {
    // If the user doesn't exist yet, there can be no token
    return 'GENERATED';
  }

  return createdUsers[address].email.verificationToken;
}

// -- Return a function that will handle a Thunder response --
function getCallback(test, callback) {
  return function(error, statusCode, result) {
    if (statusCode === 201) {
      // A user was created, save this information for deletion later in case of failure
      if (args.verbose) {
        console.log('A user was created, saving user %s for later deletion', result.email.address);
      }

      createdUsers[result.email.address] = result;
    }

    if (statusCode === 200 && test.responseType !== 'html' && test.type !== 'swagger') {
      // Update information in case they changed
      if (test.existingEmail && test.existingEmail !== result.email.address) {
        // Email was changed
        console.log('A user email address was changed. Marking %s as null', test.existingEmail);
        createdUsers[test.existingEmail] = null;
      }

      if (test.type === 'delete') {
        // The user was deleted
        console.log('A user was deleted. Marking %s as null', result.email.address);
        createdUsers[result.email.address] = null;
      } else {
        // Update the user information
        createdUsers[result.email.address] = result;
      }
    }

    if (test.type === 'swagger' && test.responseType === 'html') {
      // If the test is for Swagger UI, set the expected response to the actual response
      test.expectedResponse = result;
    }

    if (test.expectedResponse.email &&
        test.expectedResponse.email.verificationToken === 'GENERATED') {
      // If the test expects the generated token value, replace it
      test.expectedResponse.email.verificationToken = result.email.verificationToken;
    }

    if (test.expectedResponse.password &&
        test.expectedResponse.password === 'HASHED') {
      // If the test expects the hashed password value, replace it
      test.expectedResponse.password = result.password;
    }

    const err = responseHandler.handleResponse(statusCode, result, test.name,
        test.expectedCode, test.expectedResponse, args.verbose);

    if (err) return callback(err);
    else return callback(null);
  };
}

// -- Build tests (each endpoint has a section) --
const testCases = [];

tests.forEach((test) => {
  if (!test.disabled) {
    switch (test.type) {
      case 'create':
        testCases.push(function(callback) {
          console.log(test.log);

          thunder.createUser(test.body, getCallback(test, callback));
        });

        break;

      case 'get':
        testCases.push(function(callback) {
          console.log(test.log);

          thunder.getUser(test.email, test.password, getCallback(test, callback));
        });

        break;

      case 'email':
        testCases.push(function(callback) {
          console.log(test.log);

          thunder.sendEmail(test.email, test.password, getCallback(test, callback));
        });

        break;

      case 'verify':
        testCases.push(function(callback) {
          console.log(test.log);

          if (test.token === 'GENERATED') {
            // If the test uses the generated token value, replace it
            test.token = getTokenFromTest(test);
          }

          thunder.verifyUser(test.email, test.token, getCallback(test, callback),
              test.responseType);
        });

        break;

      case 'reset':
        testCases.push(function(callback) {
          console.log(test.log);

          thunder.resetVerificationStatus(test.email, test.password,
              getCallback(test, callback));
        });

        break;

      case 'update':
        testCases.push(function(callback) {
          console.log(test.log);

          if (test.body && test.body.email.verificationToken === 'GENERATED') {
            // If the test uses the generated token value, replace it
            test.body.email.verificationToken = getTokenFromTest(test);
          }

          thunder.updateUser(test.existingEmail, test.password, test.body,
              getCallback(test, callback));
        });

        break;

      case 'delete':
        testCases.push(function(callback) {
          console.log(test.log);

          thunder.deleteUser(test.email, test.password, getCallback(test, callback));
        });

        break;

      case 'swagger':
        testCases.push(function(callback) {
          console.log(test.log);

          const cb = getCallback(test, callback);

          let url = null;

          // Determine what URL the test is accessing
          if (test.responseType === 'json') {
            url = '/openapi.json';
          } else if (test.responseType === 'yaml') {
            url = '/openapi.yaml';
          } else if (test.responseType === 'html') {
            url = '/swagger';
          } else {
            console.log('Unknown responseType. Failing test.');
            cb(new Error('Unknown responseType for swagger test.'));
          }

          // Make the request and parse the result
          request(args.endpoint + url, null, (err, res, body) => {
            let result;

            try {
              if (test.responseType === 'json') {
                result = JSON.parse(body);
              } else if (test.responseType === 'yaml') {
                result = YAML.safeLoad(body);
              } else {
                result = body;
              }
            } catch (e) {
              result = body;
            }

            return cb(err, res.statusCode, result);
          });
        });

        break;

      case 'metrics':
        if (!args.runMetrics) {
          console.log('Metrics tests are disabled for this test run. Skipping test %s', test.name);
          break;
        }

        testCases.push(function(callback) {
          console.log(test.log);

          request(args.adminEndpoint + '/metrics', null, (err, res, body) => {
            const error = responseHandler.checkMetrics(res.statusCode, JSON.parse(body),
                test.name, test.expectedMetrics, args.verbose);

            if (error) return callback(error);
            else return callback(null);
          });
        });

        break;

      case 'healthcheck':
        testCases.push(function(callback) {
          console.log(test.log);

          request(args.adminEndpoint + '/healthcheck', null, (err, res, body) => {
            const error = responseHandler.checkHealth(res.statusCode, JSON.parse(body),
                test.name, args.verbose);

            if (error) return callback(error);
            else return callback(null);
          });
        });

        break;

      default:
        console.log('Unknown test type "%s". This test will be skipped.', test.type);
    }
  }
});

// -- Run tests --
console.log('Running full Thunder test...\n');

async.series(testCases, (err, result) => {
  if (err) {
    console.log('ERROR: %s', err.message);
    console.log('Attempting to clean up from failure by deleting users...');

    // Set up all the delete calls necessary
    const deleteCalls = [];

    for (email in createdUsers) {
      if (!createdUsers[email]) continue;

      const user = createdUsers[email];

      deleteCalls.push(function(callback) {
        console.log('Deleting user %s...', user.email.address);

        thunder.deleteUser(user.email.address, user.password, (err) => {
          if (err) {
            console.log('WARN: Failed to delete user %s', user.email.address);
            callback(err);
          }

          callback(null);
        });
      });
    }

    // Perform the deletes
    async.parallel(deleteCalls, (err, result) => {
      console.log('Aborting tests...');

      throw new Error('There are integration test failures');
    });
  } else {
    // Notify of any persisting users
    for (email in createdUsers) {
      if (!createdUsers[email]) continue;

      console.log('INFO: User %s still exists in the database after test completion.', email);
    }

    process.exit();
  }
});
