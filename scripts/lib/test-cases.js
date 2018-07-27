const AWSClient = require('../lib/aws-client');

class TestCases {
  /**
   * Constructs new TestCases.
   * @constructor
   *
   * @param {ThunderClient} thunder - The ThunderClient instance to use in the test cases.
   * @param {object} userDetails - The initial user information to use to create a user.
   * @param {boolean} verbose - Whether to increase output verbosity or not.
   */
  constructor(thunder, userDetails, verbose) {
    this.thunder = thunder;
    this.userDetails = userDetails;
    this.verbose = verbose;

    this.currentEmail = userDetails.email.address;
    this.currentPassword = userDetails.password;

    this.testPipeline
      = [this.begin.bind(this), this.create.bind(this), this.get.bind(this), this.email.bind(this), this.verify.bind(this), this.updateField.bind(this), this.get.bind(this), this.updateEmail.bind(this), this.get.bind(this), this.del.bind(this)];
  }

  /**
   * Create the new user in Thunder.
   *
   * @param {object} data - The user data to create.
   * @param {function} callback - The function to call on completion.
   * @return When the create event has begun.
   */
  create(data, callback) {
    console.log('Attempting to create a new user...');

    return this.thunder.createUser(data, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 201, 'CREATE', callback);
    });
  }

  /**
   * Gets the user from Thunder.
   *
   * @param {object} data - The data of the user to get
   * @param {function} callback - The function to call on completion.
   * @return When the get event has begun.
   */
  get(data, callback) {
    console.log('Attempting to get the user...');

    return this.thunder.getUser(data.email.address, data.password, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 200, 'GET', callback);
    });
  }

  /**
   * Sends a verification email to the Thunder user.
   *
   * @param {object} data - The user data of the user to email.
   * @param {function} callback - The function to call on completion.
   * @return When the send email event has begun.
   */
  email(data, callback) {
    console.log('Attempting to send a verification email...');

    return this.thunder.sendEmail(data.email.address, data.password, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 200, 'EMAIL', callback);
    });
  }

  /**
   * Verifies the user in Thunder. Simulates the user clicking on the link
   * in the email.
   *
   * @param {object} data - The user data of the user to verify.
   * @param {function} callback - The function to call on completion.
   * @return When the verification event has begun.
   */
  verify(data, callback) {
    console.log('Attempting to verify the created user...');

    return this.thunder.verifyUser(data.email.address, data.email.verificationToken,
      (err, statusCode, result) => {
        this.handleResponse(err, result, statusCode, 200, 'VERIFY', callback);
      });
  }

  /**
   * Updates the `uniqueID` field in the Thunder user.
   *
   * @param {object} data - The user data to perform an update on.
   * @param {function} callback - The function to call on completion.
   * @return When the update event has begun.
   */
  updateField(data, callback) {
    console.log('Attempting to update the user\'s unique ID property...');

    data.properties.uniqueID = Date.now().toString();
    return this.thunder.updateUser(null, data.password, data, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 200, 'UPDATE', callback);
    });
  }

  /**
   * Updates the user's email address.
   *
   * @param {object} data - The user data of the user to update.
   * @param {function} callback - The function to call on completion.
   * @return When the update event has begun.
   */
  updateEmail(data, callback) {
    console.log('Attempting to update the user\'s email address...');

    let existingEmail = data.email.address;
    data.email.address = 'newemail@gmail.com';
    return this.thunder.updateUser(existingEmail, data.password, data, (err, statusCode, result) => {
      if (!err) this.currentEmail = data.email.address;
      
      this.handleResponse(err, result, statusCode, 200, 'UPDATE EMAIL', callback);
    });
  }

  /**
   * Deletes the user from Thunder.
   *
   * @param {object} data - The user data to delete.
   * @param {function} callback - The function to call on completion.
   * @return When the deletion event has begun.
   */
  del(data, callback) {
    console.log('Attempting to delete the user...');

    return this.thunder.deleteUser(data.email.address, data.password, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 200, 'DELETE', callback);
    });
  }

  /**
   * Begins the test suite by first creating the database table.
   *
   * @param {function} callback - The function to call on completion.
   */
  begin(callback) {
    console.log('Creating pilot-users-test table...');

    let userDetails = this.userDetails;

    AWSClient.createDynamoTable('pilot-users-test', (err) => {
      if (err) return callback(err);

      console.log('Done creating table\n');
      return callback(null, userDetails);
    });
  }

  /**
   * Handles a response from Thunder and prints out necessary information.
   *
   * @param {Error} err - The error object that was returned from the Thunder call.
   * @param {object} result - The result that was returned from the Thunder call.
   * @param {int} statusCode - The status code of the Thunder call.
   * @param {int} expectedCode - The expected status code.
   * @param {string} methodName - The name of the method that was called.
   * @param {function} callback - The function to call after validation and logging.
   * @return When the validation is complete.
   */
  handleResponse(err, result, statusCode, expectedCode, methodName, callback) {
    if (err && statusCode !== expectedCode) {
      console.log('An error occurred while performing method %s', methodName);

      if (this.verbose) {
        console.log('Details:');
        console.log(result);
      }

      console.log('\n');
      return callback(err);
    }

    console.log('Successfully completed method %s (Status Code: %d, Expected: %d)',
      methodName, statusCode, expectedCode);

    if (this.verbose) {
      console.log('Response:');
      console.log(result);
    }

    console.log('\n');
    callback(null, result);
  }

  deleteAfterFailure() {

  }
}

module.exports = TestCases;
