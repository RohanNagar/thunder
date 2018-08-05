const ArgumentParser  = require('argparse').ArgumentParser;
const responseHandler = require('../lib/response-handler');
const AWSClient       = require('../lib/aws-client');
const ThunderClient   = require('thunder-client');
const { spawn }       = require('child_process');
const localDynamo     = require('local-dynamo');
const YAML            = require('yamljs');
const async           = require('async');
const fs              = require('fs');

let parser = new ArgumentParser({
  version:     '1.0.0',
  addHelp:     true,
  description: 'Runs integration tests for Thunder'
});

// -- Add command line args --
parser.addArgument(['-f', '--filename'], {
  help:         'JSON file containing user details',
  defaultValue: __dirname + '/../resources/user_details.json' });

parser.addArgument(['-e', '--endpoint'], {
  help:         'The base endpoint to connect to',
  defaultValue: 'http://localhost:8080' });

parser.addArgument(['-a', '--auth'], {
  help:         'Authentication credentials to connect to the endpoint',
  defaultValue: 'application:secret' });

parser.addArgument(['-vb', '--verbose'], {
  help:   'Increase output verbosity',
  action: 'storeTrue' });

parser.addArgument(['-d', '--docker'], {
  help:   'Test against a Docker container with dind',
  action: 'storeTrue' });

parser.addArgument(['-n', '--nodeps'], {
  help:   'Do not start local dependencies',
  action: 'storeTrue' });

let args = parser.parseArgs();

// -- Separate auth --
let auth = {
  application: args.auth.split(':')[0],
  secret:      args.auth.split(':')[1]
};

// -- Read test config --
let tests = YAML.load(__dirname + '/tests.yaml');

// -- Read JSON file --
let file = fs.readFileSync(args.filename, 'utf8').toString();
let userDetails = JSON.parse(file);

// -- Create Thunder object --
let thunder = new ThunderClient(args.endpoint, auth.application, auth.secret);

// -- Launch required external services --
let dynamoProcess;
let sesProcess;

if (!args.nodeps) {
  console.log('Launching DynamoDB Local...');
  dynamoProcess = localDynamo.launch(null, 4567);

  console.log('Launching SES Local...');
  sesProcess = spawn('npm', ['run', 'ses'], {
    cwd: __dirname + '/../'
  });
}

// -- Hold verification token when generated --
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

// -- Run tests --
console.log('Running full Thunder test...\n');

async.series(testCases, (err, result) => {
  // Clean up
  if (!args.nodeps) {
    dynamoProcess.kill();
    sesProcess.kill();
  }

  if (err) {
    console.log('ERROR: %s', err.message);
    console.log('Attempting to clean up from failure by deleting user...');

    thunder.deleteUser(userDetails.email.address, userDetails.password, err => {
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
