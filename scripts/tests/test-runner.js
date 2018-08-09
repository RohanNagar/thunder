const ArgumentParser  = require('argparse').ArgumentParser;
const responseHandler = require('../lib/response-handler');
const AWSClient       = require('../lib/aws-client');
const ThunderClient   = require('thunder-client');
const { spawn }       = require('child_process');
const localDynamo     = require('local-dynamo');
const YAML            = require('yamljs');
const async           = require('async');
const path            = require('path');

let parser = new ArgumentParser({
  version:     '1.0.0',
  addHelp:     true,
  description: 'Runs integration tests for Thunder'
});

// -- Add command line args --
parser.addArgument('testFile', {
  help: 'The name of the file containing test cases'
});

parser.addArgument(['-e', '--endpoint'], {
  help:         'The base endpoint to connect to',
  defaultValue: 'http://localhost:8080' });

parser.addArgument(['-a', '--auth'], {
  help:         'Authentication credentials to connect to the endpoint',
  defaultValue: 'application:secret' });

parser.addArgument(['-d', '--docker'], {
  help:   'Test against a Docker container with Docker-in-Docker',
  action: 'storeTrue' });

parser.addArgument(['-l', '--local-dependencies'], {
  help:   'Start local dependencies before running tests',
  action: 'storeTrue',
  dest:   'localDeps' });

parser.addArgument(['-vb', '--verbose'], {
  help:   'Increase output verbosity',
  action: 'storeTrue' });

let args = parser.parseArgs();

// -- Read test config --
let tests = YAML.load(path.join(process.cwd(), args.testFile));

// -- Separate auth --
let auth = {
  application: args.auth.split(':')[0],
  secret:      args.auth.split(':')[1]
};

// -- Create Thunder object --
let thunder = new ThunderClient(args.endpoint, auth.application, auth.secret);

// -- Launch required external services --
let dynamoProcess;
let sesProcess;

if (args.localDeps) {
  console.log('Launching DynamoDB Local...');
  dynamoProcess = localDynamo.launch(null, 4567);

  console.log('Launching SES Local...');
  sesProcess = spawn('npm', ['run', 'ses'], {
    cwd: __dirname + '/../'
  });
}

// -- Hold all created users --
let createdUsers = [];

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

    if (statusCode === 200) {
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

    if (test.expectedResponse.email
      && test.expectedResponse.email.verificationToken === 'GENERATED') {
      // If the test expects the generated token value, replace it
      test.expectedResponse.email.verificationToken = result.email.verificationToken;
    }

    let err = responseHandler.handleResponse(error, statusCode, result,
      test.name, test.expectedCode, test.expectedResponse, args.verbose);

    if (err) return callback(err);
    else return callback(null);
  };
}

// -- Build tests (each endpoint has a section) --
let testCases = [
  function(callback) {
    console.log('Creating pilot-users-test table...');

    AWSClient.createDynamoTable('pilot-users-test', args.docker, (err) => {
      if (err) return callback(err);

      console.log('Done creating table\n');
      return callback(null);
    });
  }
];

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

          thunder.verifyUser(test.email, test.token, getCallback(test, callback));
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

      default:
        console.log('Unknown test type "%s". This test will be skipped.');
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
    let deleteCalls = [];

    for (email in createdUsers) {
      if (!createdUsers[email]) continue;

      let user = createdUsers[email];

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
      // Clean up local dependencies
      if (args.localDeps) {
        dynamoProcess.kill();
        sesProcess.kill();
      }

      console.log('Aborting tests...');

      throw new Error('There are integration test failures');
    });
  } else {
    // Notify of any persisting users
    for (email in createdUsers) {
      if (!createdUsers[email]) continue;

      console.log('INFO: User %s still exists in the database after test completion.', email);
    }

    // Clean up local dependencies
    if (args.localDeps) {
      dynamoProcess.kill();
      sesProcess.kill();
    }

    process.exit();
  }
});
