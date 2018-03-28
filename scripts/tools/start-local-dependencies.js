var { spawn }      = require('child_process');
var localDynamo    = require('local-dynamo');
var AWSClient      = require('../src/aws-client');

console.log('Launching required dependencies for Thunder...');

console.log('Launching DynamoDB Local...');
var dynamoProcess = localDynamo.launch(null, 4567);

console.log('Launching SES Local...');
var sesProcess = spawn('npm', ['run', 'ses'], {
  cwd: __dirname + '/../'
});

// Define clean-up function
function cleanup() {
  dynamoProcess.kill();
  sesProcess.kill();
}

// Create the dynamo table
console.log('Creating pilot-users-test table in DynamoDB local...');
AWSClient.createPilotUserDynamoTable(err => {
  if (err) {
    console.log('An error occurred while creating the DynamoDB table.');
    cleanup();
    process.exit();
  }
});

// Make sure spawned processes get cleaned up on exit
process.on('exit', () => {
  cleanup();
});

console.log('All dependencies ready! Kill this process to shut them all down.');

