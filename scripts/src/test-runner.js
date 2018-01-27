var ArgumentParser = require('argparse').ArgumentParser;
var ThunderClient  = require('./thunder-client');
var { spawn }      = require('child_process');
var localDynamo    = require('local-dynamo');
var AWS            = require('aws-sdk');
var async          = require('async');
var fs             = require('fs');

var parser = new ArgumentParser({
  version: '1.0.0',
  addHelp: true,
  description: 'Runs integration tests for Thunder'
});

// Add command line args
parser.addArgument(['-f', '--filename'], {
  help: 'JSON file containing user details',
  defaultValue: __dirname + '/../resources/user_details.json'});

parser.addArgument(['-e', '--endpoint'], {
  help: 'The base endpoint to connect to',
  defaultValue: 'http://localhost:8080'});

parser.addArgument(['-a', '--auth'], {
  help: 'Authentication credentials to connect to the endpoint',
  defaultValue: 'application:secret'});

parser.addArgument(['-vb', '--verbose'], {
  help: 'Increase output verbosity',
  action: 'storeTrue'});

parser.addArgument(['-t', '--thunder'], {
  help: 'Start the Thunder jar before running tests',
  action: 'storeTrue'});

var args = parser.parseArgs();

// Separate auth
var auth = {
  application: args.auth.split(':')[0],
  secret: args.auth.split(':')[1]
};

// Read JSON file
var file = fs.readFileSync(args.filename, 'utf8').toString();
var userDetails = JSON.parse(file);

// Create Thunder object
var thunder = new ThunderClient(args.endpoint, auth);

// -- Define Tests --
function create(data, callback) {
  console.log('Attempting to create a new user...');

  return thunder.createUser(data, callback, args.verbose);
}

function get(data, callback) {
  console.log('Attempting to get the user...');

  return thunder.getUser({email: data.email.address},
                         {password: data.password},
                         callback,
                         args.verbose);
}

function email(data, callback) {
  console.log('Attempting to send a verification email...');
  
  return thunder.sendEmail({email: data.email.address},
                           {password: data.password},
                           callback,
                           args.verbose);
}

function verify(data, callback) {
  console.log('Attempting to verify the created user...');
  
  return thunder.verifyUser({email: data.email.address,
                             token: data.email.verificationToken},
                            {password: data.password},
                            callback,
                            args.verbose);
}

function updateField(data, callback) {
  console.log('Attempting to update the user\'s Facebook access token...');
  
  data.facebookAccessToken = Date.now();
  return thunder.updateUser({},
                            data,
                            {password: data.password},
                            callback,
                            args.verbose);
}

function updateEmail(data, callback) {
  console.log('Attempting to update the user\'s email address...');
  
  var existingEmail = data.email.address;
  data.email.address = 'newemail@gmail.com';
  return thunder.updateUser({email: existingEmail},
                            data,
                            {password: data.password},
                            callback,
                            args.verbose);
}

function del(data, callback) {
  console.log('Attempting to delete the user...');

  return thunder.deleteUser({email: data.email.address},
                            {password: data.password},
                            callback,
                            args.verbose);
}

function begin(callback) {
  console.log('Creating pilot-users-test table...');
  var dynamodb = new AWS.DynamoDB({endpoint: 'http://localhost:4567', region: 'us-east-1'});
	dynamodb.createTable({
	  AttributeDefinitions: [{
	    AttributeName: "email", 
	    AttributeType: "S"}], 
	  KeySchema: [{
	    AttributeName: "email", 
	    KeyType: "HASH"}],
		ProvisionedThroughput: {
	    ReadCapacityUnits: 2, 
	    WriteCapacityUnits: 2}, 
	  TableName: "pilot-users-test"
	}, (err, data) => {
	  if (err) return callback(err);

  console.log('Done creating table\n');
    callback(null, userDetails);
  });
}

// Define the order of the tests to run
var testPipeline = [begin, create, get, email, verify, updateField, get, updateEmail, get, del];

// Launch required external services
console.log('Launching DynamoDB Local...');
var dynamoProcess = localDynamo.launch(null, 4567);

console.log('Launching SES Local...');
var sesProcess = spawn('npm', ['run', 'ses'], {
  cwd: __dirname + '/../'
});

process.on('exit', () => {
  dynamoProcess.kill();
  sesProcess.kill();
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
        console.log('** NOTE: Deletion failure means this user is still in the DB. Delete manually. **');
      }

      console.log('Aborting...');
      throw new Error('There are integration test failures');
    });
  }
});

