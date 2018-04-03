const ArgumentParser = require('argparse').ArgumentParser;
const ThunderClient  = require('./thunder-client');
const { spawn }      = require('child_process');
const localDynamo    = require('local-dynamo');
const AWSClient      = require('./aws-client');
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
let thunder = new ThunderClient(args.endpoint, auth);

// -- Define Tests --
/**
 * Create the new user in Thunder.
 *
 * @param {object} data - The user data to create.
 * @param {function} callback - The function to call on completion.
 * @return When the create event has begun.
 */
function create(data, callback) {
  console.log('Attempting to create a new user...');

  return thunder.createUser(data, callback, args.verbose);
}

/**
 * Gets the user from Thunder.
 *
 * @param {object} data - The data of the user to get
 * @param {function} callback - The function to call on completion.
 * @return When the get event has begun.
 */
function get(data, callback) {
  console.log('Attempting to get the user...');

  return thunder.getUser({ email: data.email.address },
                         { password: data.password },
                         callback,
                         args.verbose);
}

/**
 * Sends a verification email to the Thunder user.
 *
 * @param {object} data - The user data of the user to email.
 * @param {function} callback - The function to call on completion.
 * @return When the send email event has begun.
 */
function email(data, callback) {
  console.log('Attempting to send a verification email...');

  return thunder.sendEmail({ email: data.email.address },
                           { password: data.password },
                           callback,
                           args.verbose);
}

/**
 * Verifies the user in Thunder. Simulates the user clicking on the link
 * in the email.
 *
 * @param {object} data - The user data of the user to verify.
 * @param {function} callback - The function to call on completion.
 * @return When the verification event has begun.
 */
function verify(data, callback) {
  console.log('Attempting to verify the created user...');

  return thunder.verifyUser({ email: data.email.address,
                              token: data.email.verificationToken },
                            { password: data.password },
                            callback,
                            args.verbose);
}

/**
 * Updates the `facebookAccessToken` field in the Thunder user.
 *
 * @param {object} data - The user data to perform an update on.
 * @param {function} callback - The function to call on completion.
 * @return When the update event has begun.
 */
function updateField(data, callback) {
  console.log('Attempting to update the user\'s Facebook access token...');

  data.facebookAccessToken = Date.now();
  return thunder.updateUser({},
                            data,
                            { password: data.password },
                            callback,
                            args.verbose);
}

/**
 * Updates the user's email address.
 *
 * @param {object} data - The user data of the user to update.
 * @param {function} callback - The function to call on completion.
 * @return When the update event has begun.
 */
function updateEmail(data, callback) {
  console.log('Attempting to update the user\'s email address...');

  let existingEmail = data.email.address;
  data.email.address = 'newemail@gmail.com';
  return thunder.updateUser({ email: existingEmail },
                            data,
                            { password: data.password },
                            callback,
                            args.verbose);
}

/**
 * Deletes the user from Thunder.
 *
 * @param {object} data - The user data to delete.
 * @param {function} callback - The function to call on completion.
 * @return When the deletion event has begun.
 */
function del(data, callback) {
  console.log('Attempting to delete the user...');

  return thunder.deleteUser({ email: data.email.address },
                            { password: data.password },
                            callback,
                            args.verbose);
}

/**
 * Begins the test suite by first creating the database table.
 *
 * @param {function} callback - The function to call on completion.
 */
function begin(callback) {
  console.log('Creating pilot-users-test table...');

  AWSClient.createDynamoTable('pilot-users-test', (err) => {
    if (err) return callback(err);

    console.log('Done creating table\n');
    return callback(null, userDetails);
  });
}

// -- Define the order of the tests to run --
let testPipeline
  = [begin, create, get, email, verify, updateField, get, updateEmail, get, del];

// -- Launch required external services --
console.log('Launching DynamoDB Local...');
let dynamoProcess = localDynamo.launch(null, 4567);

console.log('Launching SES Local...');
let sesProcess = spawn('npm', ['run', 'ses'], {
  cwd: __dirname + '/../'
});

// -- Run tests --
console.log('Running full Thunder test...\n');
if (args.verbose) {
  console.log('Using user %s:', userDetails.email.address);
  console.log(userDetails);
  console.log('\n');
}

async.waterfall(testPipeline, (err, result) => {
  if (err) {
    console.log(err);
    console.log('Attempting to clean up from failure by deleting user...');

    del(userDetails, (err, res) => {
      if (err) {
        console.log('** NOTE: Deletion failure means this user is still in the DB.'
          + ' Delete manually. **');
      }

      console.log('Aborting...');
      throw new Error('There are integration test failures');
    });
  }

  // Clean up
  dynamoProcess.kill();
  sesProcess.kill();

  process.exit();
});

