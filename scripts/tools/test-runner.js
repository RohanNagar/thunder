const ArgumentParser = require('argparse').ArgumentParser;
const TestCases      = require('../lib/test-cases');
const ThunderClient  = require('thunder-client');
const { spawn }      = require('child_process');
const localDynamo    = require('local-dynamo');
const async          = require('async');
const fs             = require('fs');

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

// -- Read JSON file --
let file = fs.readFileSync(args.filename, 'utf8').toString();
let userDetails = JSON.parse(file);

// -- Create Thunder object --
let thunder = new ThunderClient(args.endpoint, auth.application, auth.secret);
let testCases = new TestCases(thunder, userDetails, args.verbose, args.docker);

// -- Launch required external services --
if (!args.nodeps) {
  console.log('Launching DynamoDB Local...');
  let dynamoProcess = localDynamo.launch(null, 4567);

  console.log('Launching SES Local...');
  let sesProcess = spawn('npm', ['run', 'ses'], {
    cwd: __dirname + '/../'
  });
}

// -- Run tests --
console.log('Running full Thunder test...\n');
if (args.verbose) {
  console.log('Using user %s:', userDetails.email.address);
  console.log(userDetails);
  console.log('\n');
}

async.waterfall(testCases.testPipeline, (err, result) => {
  if (err) {
    console.log(err);
    console.log('Attempting to clean up from failure by deleting user...');

    testCases.del((err, res) => {
      if (err) {
        console.log('** NOTE: Deletion failure means this user is still in the DB.'
          + ' Delete manually. **');
      }

      console.log('Aborting...');

      // Clean up
      if (!args.nodeps) {
        dynamoProcess.kill();
        sesProcess.kill();
      }

      throw new Error('There are integration test failures');
    });
  } else {
    // Clean up
    if (!args.nodeps) {
      dynamoProcess.kill();
      sesProcess.kill();
    }

    process.exit();
  }
});
