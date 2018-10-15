const { spawn }      = require('child_process');
const localDynamo    = require('local-dynamo');
const AWSClient      = require('../lib/aws-client');

// -- Start DynamoDB and SES --
console.log('Launching required dependencies for Thunder...');

console.log('Launching DynamoDB Local...');
const dynamoProcess = localDynamo.launch(null, 4567);

console.log('Launching SES Local...');
const sesProcess = spawn('npm', ['run', 'ses'], {
  cwd: __dirname + '/../'
});

/**
 * Cleans up the started child processes.
 */
function cleanup() {
  dynamoProcess.kill();
  sesProcess.kill();
}

// -- Create the DynamoDB table --
console.log('Creating pilot-users-test table in DynamoDB local...');
AWSClient.createDynamoTable('pilot-users-test', false, (err) => {
  if (err) {
    console.log('An error occurred while creating the DynamoDB table.');
    console.log(err);
    cleanup();
    process.exit();
  }

  // -- Ready to go --
  console.log('All dependencies ready! Kill this process to shut them all down.');
});

// -- Make sure spawned processes get cleaned up on exit --
process.on('exit', () => {
  cleanup();
});
