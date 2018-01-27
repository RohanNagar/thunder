var ArgumentParser = require('argparse').ArgumentParser;
var ThunderClient  = require('./thunder-client');
var fs             = require('fs');
var crypto         = require('crypto');

var parser = new ArgumentParser({
  version: '1.0.0',
  addHelp: true,
  description: 'Runs a single Thunder command'
});

// Arguments for all commands
parser.addArgument(['-e', '--endpoint'], {
  help: 'The base endpoint to connect to Thunder.',
  defaultValue: 'http://localhost:8080'});

parser.addArgument(['-a', '--auth'], {
  help: 'Authentication credentials to connect to the endpoint.',
  defaultValue: 'application:secret'});

parser.addArgument(['-vb', '--verbose'], {
  help: 'Increase output verbosity.',
  action: 'storeTrue'});

// Arguments for individual commands
var subparsers = parser.addSubparsers({
  help: 'The name of the command to run.',
  title: 'Commands',
  dest: 'command',
  options: ['create', 'get', 'update', 'delete'],
  required: true
});

var createCommand = subparsers.addParser('create', { addHelp: true });
createCommand.addArgument(['-f', '--filename'], {
  help: 'JSON file containing user details',
  defaultValue: __dirname + '/../resources/user_details.json'});

var getCommand = subparsers.addParser('get', { addHelp: true });
getCommand.addArgument('email', {
  help: 'The email of the user'});
getCommand.addArgument('password', {
  help: 'The current password of the user'});

var updateCommand = subparsers.addParser('update', { addHelp: true });
updateCommand.addArgument(['-f', '--filename'], {
  help: 'JSON file containing user details',
  defaultValue: __dirname + '/../resources/user_details.json'});
updateCommand.addArgument('email', {
  help: 'The email of the user'});
updateCommand.addArgument('password', {
  help: 'The current password of the user'});

var deleteCommand = subparsers.addParser('delete', { addHelp: true });
deleteCommand.addArgument('email', {
  help: 'The email of the user'});
deleteCommand.addArgument('password', {
  help: 'The current password of the user'});

var args = parser.parseArgs();

// Seperate auth
var auth = {
  application: args.auth.split(':')[0],
  secret: args.auth.split(':')[1]
};

// Create Thunder object
var thunder = new ThunderClient(args.endpoint, auth);

switch (args.command) {
  case 'create':
    var file = fs.readFileSync(args.filename, 'utf8').toString();
    var userDetails = JSON.parse(file);

    console.log('Creating user...');
    thunder.createUser(userDetails, (err, result) => {
      if (err) {
        console.log(err);
        throw new Error('A failure occured while creating.');
      }
    }, args.verbose);
    
    break;
  case 'get':
    var hashedPassword = crypto.createHash('md5').update(args.password).digest("hex");

    console.log('Getting user %s...', args.email);
    thunder.getUser({ email: args.email },
                    { password: hashedPassword },
                    (err, result) => {
      if (err) {
        console.log(err);
        throw new Error('A failure occured while creating.');
      }
    }, args.verbose);

    break;
  case 'update':
    var file = fs.readFileSync(args.filename, 'utf8').toString();
    var userDetails = JSON.parse(file);

    var hashedPassword = crypto.createHash('md5').update(args.password).digest("hex");

    console.log('Updating user %s...', args.email);
    thunder.updateUser({ email: args.email },
                       userDetails,
                       { password: hashedPassword },
                       (err, result) => {
      if (err) {
        console.log(err);
        throw new Error('A failure occured while creating.');
      }
    }, args.verbose);

    break;
  case 'delete':
    var hashedPassword = crypto.createHash('md5').update(args.password).digest("hex");

    console.log('Deleting user %s...', args.email);
    thunder.deleteUser({ email: args.email },
                       { password: hashedPassword },
                       (err, result) => {
      if (err) {
        console.log(err);
        throw new Error('A failure occured while creating.');
      }
    }, args.verbose);

    break;
}

