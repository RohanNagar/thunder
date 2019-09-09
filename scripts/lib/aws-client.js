const AWS = require('aws-sdk');

/**
 * Create a new DynamoDB table in DynamoDB Local.
 *
 * @param {string} tableName - The name of the user table to create.
 * @param {boolean} docker - Whether DynamoDB is running in the Docker host or not.
 * @param {function} callback - The function to call on method completion.
 */
function createDynamoTable(tableName, docker, callback) {
  const endpoint = docker ? 'http://docker:4567' : 'http://localhost:4567';

  const dynamodb = new AWS.DynamoDB({
    endpoint: endpoint,
    region:   'us-east-1'
  });

  dynamodb.createTable({
    AttributeDefinitions: [{
      AttributeName: 'email',
      AttributeType: 'S' }],
    KeySchema: [{
      AttributeName: 'email',
      KeyType:       'HASH' }],
    ProvisionedThroughput: {
      ReadCapacityUnits:  2,
      WriteCapacityUnits: 2 },
    TableName: 'pilot-users-test'
  }, (err, data) => {
    if (err) {
      if (err.code === 'ResourceInUseException' &&
          err.message === 'Cannot create preexisting table') {
        // If the table already exists, we can return without an error
        return callback(null);
      }

      return callback(err);
    }

    callback(null);
  });
}

module.exports = {
  createDynamoTable
};
