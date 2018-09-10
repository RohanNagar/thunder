const ArgumentParser = require('argparse').ArgumentParser;
const ThunderClient  = require('thunder-client');
const fs             = require('fs');
const crypto         = require('crypto');

let parser = new ArgumentParser({
  version:     '1.0.0',
  addHelp:     true,
  description: 'Runs a single Thunder command'
});

// Arguments for all commands
parser.addArgument(['-e', '--endpoint'], {
  help:         'The base endpoint to connect to Thunder.',
  defaultValue: 'http://localhost:8080' });

parser.addArgument(['-a', '--auth'], {
  help:         'Authentication credentials to connect to the endpoint.',
  defaultValue: 'application:secret' });

parser.addArgument(['-vb', '--verbose'], {
  help:   'Increase output verbosity.',
  action: 'storeTrue' });

// Arguments for individual commands
let subparsers = parser.addSubparsers({
  help:     'The name of the command to run.',
  title:    'Commands',
  dest:     'command',
  options:  ['create', 'get', 'update', 'delete', 'email', 'verify'],
  required: true
});

let createCommand = subparsers.addParser('create', { addHelp: true });
createCommand.addArgument(['-f', '--filename'], {
  help:         'JSON file containing user details',
  defaultValue: __dirname + '/default_user.json' });

let getCommand = subparsers.addParser('get', { addHelp: true });
getCommand.addArgument('email', {
  help: 'The email of the user' });
getCommand.addArgument('password', {
  help: 'The current password of the user' });

let updateCommand = subparsers.addParser('update', { addHelp: true });
updateCommand.addArgument(['-f', '--filename'], {
  help:         'JSON file containing user details',
  defaultValue: __dirname + '/default_user.json' });
updateCommand.addArgument('email', {
  help: 'The email of the user' });
updateCommand.addArgument('password', {
  help: 'The current password of the user' });

let deleteCommand = subparsers.addParser('delete', { addHelp: true });
deleteCommand.addArgument('email', {
  help: 'The email of the user' });
deleteCommand.addArgument('password', {
  help: 'The current password of the user' });

let emailCommand = subparsers.addParser('email', { addHelp: true });
emailCommand.addArgument('email', {
  help: 'The email of the user' });
emailCommand.addArgument('password', {
  help: 'The current password of the user' });

let verifyCommand = subparsers.addParser('verify', { addHelp: true });
verifyCommand.addArgument('email', {
  help: 'The email of the user' });
verifyCommand.addArgument('token', {
  help: 'The verification token that will successfully verify the user' });

let args = parser.parseArgs();

// Separate auth
let auth = {
  application: args.auth.split(':')[0],
  secret:      args.auth.split(':')[1]
};

// Create Thunder object
let thunder = new ThunderClient(args.endpoint, auth.application, auth.secret);

// Variables to be used
let file;
let userDetails;
let hashedPassword;

// -- Define response handler --
/**
 * Handles a response from Thunder and prints out necessary information.
 *
 * @param {Error} err - The error object that was returned from the Thunder call.
 * @param {object} result - The result that was returned from the Thunder call.
 * @param {string} methodName - The name of the method that was called.
 */
function handleResponse(err, result, methodName) {
  if (err) {
    console.log('An error occurred while performing method %s', methodName);

    if (args.verbose) {
      console.log('Details:');
      console.log(result);
    }

    console.log('\n');
    throw err;
  }

  console.log('Successfully completed method %s', methodName);

  if (args.verbose) {
    console.log('Response:');
    console.log(result);
  }

  console.log('\n');
}

switch (args.command) {
  case 'create':
    file = fs.readFileSync(args.filename, 'utf8').toString();
    userDetails = JSON.parse(file);

    console.log('Creating user...');
    thunder.createUser(userDetails, (err, statusCode, result) => {
      handleResponse(err, result, 'CREATE');
    });

    break;
  case 'get':
    hashedPassword = crypto.createHash('md5')
        .update(args.password).digest('hex');

    console.log('Getting user %s...', args.email);
    thunder.getUser(args.email, hashedPassword, (err, statusCode, result) => {
      handleResponse(err, result, 'GET');
    });

    break;
  case 'update':
    file = fs.readFileSync(args.filename, 'utf8').toString();
    userDetails = JSON.parse(file);

    hashedPassword = crypto.createHash('md5')
        .update(args.password).digest('hex');

    console.log('Updating user %s...', args.email);
    thunder.updateUser(args.email, hashedPassword, userDetails,
        (err, statusCode, result) => {
          handleResponse(err, result, 'UPDATE');
        });

    break;
  case 'delete':
    hashedPassword = crypto.createHash('md5')
        .update(args.password).digest('hex');

    console.log('Deleting user %s...', args.email);
    thunder.deleteUser(args.email, hashedPassword, (err, statusCode, result) => {
      handleResponse(err, result, 'DELETE');
    });

    break;
  case 'email':
    hashedPassword = crypto.createHash('md5')
        .update(args.password).digest('hex');

    console.log('Sending email to user %s...', args.email);
    thunder.sendEmail(args.email, hashedPassword, (err, statusCode, result) => {
      handleResponse(err, result, 'EMAIL');
    });

    break;
  case 'verify':
    console.log('Verifying user %s...', args.email);
    thunder.verifyUser(args.email, args.token, (err, statusCode, result) => {
      handleResponse(err, result, 'EMAIL');
    });

    break;
}
