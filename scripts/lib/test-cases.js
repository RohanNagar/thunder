const AWSClient = require('../lib/aws-client');

class TestCases {
  /**
   * Constructs a new TestCases object.
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

    this.testPipeline = [this.begin.bind(this),
      // CREATE
      this.createNullUser.bind(this),
      // DISABLED this.createNullEmail.bind(this),
      this.createInvalidEmail.bind(this),
      this.createInvalidProperties.bind(this),
      this.create.bind(this),

      // GET
      this.getNullEmail.bind(this),
      this.getEmptyEmail.bind(this),
      // DISABLED this.getNullPassword.bind(this),
      this.getEmptyPassword.bind(this),
      this.getIncorrectPassword.bind(this),
      this.get.bind(this),

      // SEND EMAIL
      this.emailNullEmail.bind(this),
      this.emailEmptyEmail.bind(this),
      // DISABLED this.emailNullPassword.bind(this),
      this.emailEmptyPassword.bind(this),
      this.email.bind(this),

      // VERIFY EMAIL
      this.verify.bind(this),

      // UPDATE FAILURES
      // UPDATE SUCCESSES
      this.updateField.bind(this),
      this.get.bind(this),
      this.updateEmail.bind(this),
      this.get.bind(this),

      // DELETE
      this.del.bind(this)];
  }

  /* CREATE TESTS */

  createNullUser(data, callback) {
    console.log('Checking for BAD REQUEST when creating a null user...');

    return this.thunder.createUser(null, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'CREATE NULL USER', callback);
    });
  }

  createNullEmail(data, callback) {
    console.log('Checking for BAD REQUEST when creating a user with a null email...');

    let user = {
      email:      { address: null },
      password:   'test',
      properties: {}
    };

    return this.thunder.createUser(user, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'CREATE NULL EMAIL', callback);
    });
  }

  createInvalidEmail(data, callback) {
    console.log('Checking for BAD REQUEST when creating a user with an invalid email...');

    let user = {
      email:      { address: 'bademail' },
      password:   'test',
      properties: {}
    };

    return this.thunder.createUser(user, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'CREATE INVALID EMAIL', callback);
    });
  }

  createInvalidProperties(data, callback) {
    console.log('Checking for BAD REQUEST when creating a user with invalid properties...');

    let user = {
      email:      { address: 'test@test.com' },
      password:   'test',
      properties: {}
    };

    return this.thunder.createUser(user, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'CREATE INVALID PROPERTIES', callback);
    });
  }

  create(data, callback) {
    console.log('Attempting to create a new user...');

    return this.thunder.createUser(data, (err, statusCode, result) => {
      if (!err) this.userDetails = result;

      this.handleResponse(err, result, statusCode, 201, 'CREATE', callback);
    });
  }

  /* GET TESTS */

  getNullEmail(data, callback) {
    console.log('Checking for BAD REQUEST when getting a user using a null email...');

    return this.thunder.getUser(null, data.password, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'GET NULL EMAIL', callback);
    });
  }

  getEmptyEmail(data, callback) {
    console.log('Checking for BAD REQUEST when getting a user using an empty email...');

    return this.thunder.getUser('', data.password, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'GET EMPTY EMAIL', callback);
    });
  }

  getEmptyPassword(data, callback) {
    console.log('Checking for BAD REQUEST when getting a user using an empty password...');

    return this.thunder.getUser(data.email.address, '', (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'GET EMPTY PASSWORD', callback);
    });
  }

  getIncorrectPassword(data, callback) {
    console.log('Checking for UNAUTHORIZED when getting a user using an incorrect password...');

    return this.thunder.getUser(data.email.address, 'ERROR', (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 401, 'GET INCORRECT PASSWORD', callback);
    });
  }

  get(data, callback) {
    console.log('Attempting to get the user...');

    return this.thunder.getUser(data.email.address, data.password, (err, statusCode, result) => {
      if (!err) this.userDetails = result;

      this.handleResponse(err, result, statusCode, 200, 'GET', callback);
    });
  }

  /* EMAIL TESTS */

  emailNullEmail(data, callback) {
    console.log('Checking for BAD REQUEST when sending a verification email to a null email...');

    return this.thunder.sendEmail(null, data.password, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'EMAIL NULL EMAIL', callback);
    });
  }

  emailEmptyEmail(data, callback) {
    console.log('Checking for BAD REQUEST when sending a verification email to an empty email...');

    return this.thunder.sendEmail('', data.password, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'EMAIL EMPTY EMAIL', callback);
    });
  }

  emailNullPassword(data, callback) {
    console.log('Checking for BAD REQUEST when sending an email with a null password...');

    return this.thunder.sendEmail(data.email.address, null, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'EMAIL NULL PASSWORD', callback);
    });
  }

  emailEmptyPassword(data, callback) {
    console.log('Checking for BAD REQUEST when sending an email with an empty password...');

    return this.thunder.sendEmail(data.email.address, '', (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'EMAIL EMPTY PASSWORD', callback);
    });
  }

  email(data, callback) {
    console.log('Attempting to send a verification email...');

    return this.thunder.sendEmail(data.email.address, data.password, (err, statusCode, result) => {
      if (!err) this.userDetails = result;

      this.handleResponse(err, result, statusCode, 200, 'EMAIL', callback);
    });
  }

  /* VERIFY TESTS */

  verify(data, callback) {
    console.log('Attempting to verify the created user...');

    return this.thunder.verifyUser(data.email.address, data.email.verificationToken,
      (err, statusCode, result) => {
        if (!err) this.userDetails = result;

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
      if (!err) this.userDetails = result;

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
    return this.thunder.updateUser(existingEmail, data.password, data,
      (err, statusCode, result) => {
        if (!err) this.userDetails = result;

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
      if (!err) this.userDetails = result;

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

    AWSClient.createDynamoTable('pilot-users-test', (err) => {
      if (err) return callback(err);

      console.log('Done creating table\n');
      return callback(null, this.userDetails);
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
    if (statusCode === expectedCode) {
      console.log('Successfully completed method %s (Status Code: %d, Expected: %d)',
        methodName, statusCode, expectedCode);

      if (this.verbose) {
        console.log('Response:');
        console.log(result);
      }

      console.log('\n');
      return callback(null, this.userDetails);
    }

    console.log('An error occurred while performing method %s', methodName);
    console.log('Status Code: %d, Expected: %d', statusCode, expectedCode);

    if (this.verbose) {
      console.log('Details:');
      console.log(result);
    }

    console.log('\n');

    if (err) callback(err);
    else callback(new Error('Status codes do not match. Expected: '
      + expectedCode + ', Actual: ' + statusCode));
  }

  /**
   * Deletes the user in the database after a failure.
   *
   * @param {function} callback - The function to call on completion.
   * @return When the deletion event has begun.
   */
  deleteAfterFailure(callback) {
    console.log('Attempting to delete the user...');

    return this.thunder.deleteUser(this.userDetails.email.address, this.userDetails.password,
      (err, statusCode, result) => {
        if (!err) this.userDetails = result;

        this.handleResponse(err, result, statusCode, 200, 'DELETE', callback);
    });
  }
}

module.exports = TestCases;
