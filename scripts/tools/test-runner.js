const ArgumentParser  = require('argparse').ArgumentParser;
const responseHandler = require('../lib/response-handler');
const AWSClient       = require('../lib/aws-client');
const ThunderClient   = require('thunder-client');
const { spawn }       = require('child_process');
const localDynamo     = require('local-dynamo');
const YAML            = require('yamljs');
const async           = require('async');

let parser = new ArgumentParser({
  version:     '1.0.0',
  addHelp:     true,
  description: 'Runs integration tests for Thunder'
});

// -- Add command line args --
//parser.addArgument('testFile', {
//  help:     'The name of the file containing test cases'
//});

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
let tests = YAML.load(__dirname + '/tests.yaml');

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

// -- Hold information needed for later --
let createdEmail = 'success@simulator.amazonses.com'; // Assume default
let createdPassword = '5f4dcc3b5aa765d61d8327deb882cf99'; // Assume default
let generatedToken;

// -- Build tests --
let testCases = [
  function(callback) {
    console.log('Creating pilot-users-test table...');

    AWSClient.createDynamoTable('pilot-users-test', args.docker, err => {
      if (err) return callback(err);

      console.log('Done creating table\n');
      return callback(null);
    });
  }
];

tests.create.forEach(test => {
  if (!test.disabled) {
    testCases.push(function(callback) {
      console.log(test.log);

      thunder.createUser(test.body, (error, statusCode, result) => {
        if (statusCode === 201) {
          // A user was created, save this information for deletion later in case of failure
          createdEmail = result.email.address;
          createdPassword = result.password;
        }

        let err = responseHandler.handleResponse(error, statusCode, result,
          test.name, test.expectedCode, test.expectedResponse, args.verbose);

        if (err) return callback(err);
        else return callback(null);
      });
    });
  }
});

tests.get.forEach(test => {
  if (!test.disabled) {
    testCases.push(function(callback) {
      console.log(test.log);

      thunder.getUser(test.email, test.password, (error, statusCode, result) => {
        let err = responseHandler.handleResponse(error, statusCode, result,
          test.name, test.expectedCode, test.expectedResponse, args.verbose);

        if (err) return callback(err);
        else return callback(null);
      });
    });
  }
});

tests.email.forEach(test => {
  if (!test.disabled) {
    testCases.push(function(callback) {
      console.log(test.log);

      thunder.sendEmail(test.email, test.password, (error, statusCode, result) => {
        if (statusCode === 200) {
          // A verification token was generated, save to verify in future tests
          generatedToken = result.email.verificationToken;
        }

        if (test.expectedResponse.email
          && test.expectedResponse.email.verificationToken === 'GENERATED') {
          // If the test expects the generated token value, replace it
          test.expectedResponse.email.verificationToken = generatedToken;
        }

        let err = responseHandler.handleResponse(error, statusCode, result,
          test.name, test.expectedCode, test.expectedResponse, args.verbose);

        if (err) return callback(err);
        else return callback(null);
      });
    });
  }
});

tests.verify.forEach(test => {
  if (!test.disabled) {
    testCases.push(function(callback) {
      console.log(test.log);

      if (test.token === 'GENERATED') {
        // If the test uses the generated token value, replace it
        test.token = generatedToken;
      }

      thunder.verifyUser(test.email, test.token, (error, statusCode, result) => {
        if (test.expectedResponse.email
          && test.expectedResponse.email.verificationToken === 'GENERATED') {
          // If the test expects the generated token value, replace it
          test.expectedResponse.email.verificationToken = generatedToken;
        }

        let err = responseHandler.handleResponse(error, statusCode, result,
          test.name, test.expectedCode, test.expectedResponse, args.verbose);

        if (err) return callback(err);
        else return callback(null);
      });
    });
  }
});

tests.update.forEach(test => {
  if (!test.disabled) {
    testCases.push(function(callback) {
      console.log(test.log);

      if (test.body && test.body.email.verificationToken === 'GENERATED') {
        // If the test uses the generated token value, replace it
        test.body.email.verificationToken = generatedToken;
      }

      thunder.updateUser(test.existingEmail, test.password, test.body, (error, statusCode, result) => {
        if (statusCode === 200) {
          // Update email and password in case they changed
          createdEmail = result.email.address;
          createdPassword = result.password;
        }

        if (test.expectedResponse.email
          && test.expectedResponse.email.verificationToken === 'GENERATED') {
          // If the test expects the generated token value, replace it
          test.expectedResponse.email.verificationToken = generatedToken;
        }

        let err = responseHandler.handleResponse(error, statusCode, result,
          test.name, test.expectedCode, test.expectedResponse, args.verbose);

        if (err) return callback(err);
        else return callback(null);
      });
    });
  }
});

tests.delete.forEach(test => {
  if (!test.disabled) {
    testCases.push(function(callback) {
      console.log(test.log);

      thunder.deleteUser(test.email, test.password, (error, statusCode, result) => {
        if (test.expectedResponse.email
          && test.expectedResponse.email.verificationToken === 'GENERATED') {
          // If the test expects the generated token value, replace it
          test.expectedResponse.email.verificationToken = generatedToken;
        }

        let err = responseHandler.handleResponse(error, statusCode, result,
          test.name, test.expectedCode, test.expectedResponse, args.verbose);

        if (err) return callback(err);
        else return callback(null);
      });
    });
  }
});

// -- Run tests --
console.log('Running full Thunder test...\n');

async.series(testCases, (err, result) => {
  // Clean up local dependencies
  if (args.localDeps) {
    dynamoProcess.kill();
    sesProcess.kill();
  }

  if (err) {
    console.log('ERROR: %s', err.message);
    console.log('Attempting to clean up from failure by deleting user...');

    thunder.deleteUser(createdEmail, createdPassword, err => {
      if (err) {
        console.log('** NOTE: Deletion failure means this user is still in the DB.'
          + ' Delete manually. **');
      }

      console.log('Successfully deleted user. Aborting tests...')

      throw new Error('There are integration test failures');
    });
  } else {
    process.exit();
  }
});
