var AWS = require('aws-sdk');

function createPilotUserDynamoTable(callback) {
  var dynamodb = new AWS.DynamoDB({endpoint: 'http://localhost:4567', region: 'us-east-1'});
  dynamodb.createTable({
    AttributeDefinitions: [{
      AttributeName: "email",
      AttributeType: "S" }],
    KeySchema: [{
      AttributeName: "email",
      KeyType: "HASH" }],
    ProvisionedThroughput: {
      ReadCapacityUnits: 2,
      WriteCapacityUnits: 2 },
    TableName: "pilot-users-test"
  }, (err, data) => {
    if (err) return callback(err);

    callback(null);
  });
}

module.exports = {
  createPilotUserDynamoTable
};

if (!module.parent) {
  createPilotUserDynamoTable(err => {
    if (err) {
      console.log('There was an error creating the table. Is DynamoDB running on port 4567?');
      console.log(err);
    } else {
      console.log('Successfully created pilot-users-test DynamoDB table.');
    }
  });
}

