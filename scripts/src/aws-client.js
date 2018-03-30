const AWS = require('aws-sdk');

/**
 * Create a new DynamoDB table in DynamoDB Local.
 * The table will be created with the name `pilot-users-test`.
 *
 * @param {function} callback - The function to call on method completion.
 */
function createPilotUserDynamoTable(callback) {
  let dynamodb = new AWS.DynamoDB({ endpoint: 'http://localhost:4567', region: 'us-east-1' });
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
    if (err) return callback(err);

    callback(null);
  });
}

module.exports = {
  createPilotUserDynamoTable
};

