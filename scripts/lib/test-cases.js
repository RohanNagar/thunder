const AWSClient = require('../lib/aws-client');

class TestCases {
  /**
   * Constructs a new TestCases object.
   * @constructor
   *
   * @param {ThunderClient} thunder - The ThunderClient instance to use in the test cases.
   * @param {object} userDetails - The initial user information to use to create a user.
   * @param {boolean} verbose - Whether to increase output verbosity or not.
   * @param {boolean} docker - Whether we are testing against Docker or not.
   */
  constructor(thunder, userDetails, verbose, docker) {
    this.thunder = thunder;
    this.userDetails = userDetails;
    this.verbose = verbose;
    this.docker = docker;

    this.testPipeline = [
      this.begin.bind(this),

      // UPDATE
      // DISABLED this.updateNullPassword.bind(this),
      this.updateEmptyPassword.bind(this),
      this.updateNullUser.bind(this),
      // DISABLED this.updateNullEmail.bind(this),
      this.updateInvalidEmail.bind(this),
      this.updateInvalidProperties.bind(this),
      this.updateNonexistantUser.bind(this),
      this.updateWrongPassword.bind(this),
      this.updateField.bind(this),
      this.updateEmail.bind(this),

      // DELETE
      this.delNullEmail.bind(this),
      this.delEmptyEmail.bind(this),
      // DISABLED this.delNullPassword.bind(this),
      this.delEmptyPassword.bind(this),
      this.delNonexistantUser.bind(this),
      this.delWrongPassword.bind(this),
      this.del.bind(this)];
  }

  /* UPDATE TESTS */

  updateNullPassword(callback) {
    console.log('Checking for BAD REQUEST when updating a user with a null password...');

    return this.thunder.updateUser(null, null, this.userDetails, (err, statusCode, result) => {
        this.handleResponse(err, result, statusCode, 400, 'UPDATE NULL PASSWORD', callback);
      });
  }

  updateEmptyPassword(callback) {
    console.log('Checking for BAD REQUEST when updating a user with an empty password...');

    return this.thunder.updateUser(null, '', this.userDetails, (err, statusCode, result) => {
        this.handleResponse(err, result, statusCode, 400, 'UPDATE EMPTY PASSWORD', callback);
      });
  }

  updateNullUser(callback) {
    console.log('Checking for BAD REQUEST when updating a null user...');

    return this.thunder.updateUser(null, 'test', null,
      (err, statusCode, result) => {
        this.handleResponse(err, result, statusCode, 400, 'UPDATE NULL USER', callback);
      });
  }

  updateNullEmail(callback) {
    console.log('Checking for BAD REQUEST when updating a user with a null email...');

    let user = {
      email:      { address: null },
      password:   'test',
      properties: {}
    };

    return this.thunder.updateUser(null, 'test', user, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'UPDATE NULL EMAIL', callback);
    });
  }

  updateInvalidEmail(callback) {
    console.log('Checking for BAD REQUEST when updating a user with an invalid email...');

    let user = {
      email:      { address: 'bademail' },
      password:   'test',
      properties: {}
    };

    return this.thunder.updateUser(null, 'test', user, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'UPDATE INVALID EMAIL', callback);
    });
  }

  updateInvalidProperties(callback) {
    console.log('Checking for BAD REQUEST when updating a user with invalid properties...');

    let user = {
      email:      { address: 'test@test.com' },
      password:   'test',
      properties: {}
    };

    return this.thunder.updateUser(null, 'test', user, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'UPDATE INVALID PROPERTIES', callback);
    });
  }

  updateNonexistantUser(callback) {
    console.log('Checking for NOT FOUND when updating a nonexistant user...');

    return this.thunder.updateUser('test@test.com', this.userDetails.password, this.userDetails,
      (err, statusCode, result) => {
        this.handleResponse(err, result, statusCode, 404, 'UPDATE NONEXISTANT USER', callback);
      });
  }

  updateWrongPassword(callback) {
    console.log('Checking for UNAUTHORIZED when updating a user with the wrong password...');

    return this.thunder.updateUser(null, 'wrong-password', this.userDetails,
      (err, statusCode, result) => {
        this.handleResponse(err, result, statusCode, 401, 'UPDATE WRONG PASSWORD', callback);
      });
  }

  updateField(callback) {
    console.log('Attempting to update the user\'s unique ID property...');

    this.userDetails.properties.uniqueID = Date.now().toString();
    return this.thunder.updateUser(null, this.userDetails.password, this.userDetails,
      (err, statusCode, result) => {
        this.handleResponse(err, result, statusCode, 200, 'UPDATE', callback);
      });
  }

  updateEmail(callback) {
    console.log('Attempting to update the user\'s email address...');

    let existingEmail = this.userDetails.email.address;
    this.userDetails.email.address = 'newemail@gmail.com';
    return this.thunder.updateUser(existingEmail, this.userDetails.password, this.userDetails,
      (err, statusCode, result) => {
        this.handleResponse(err, result, statusCode, 200, 'UPDATE EMAIL', callback);
    });
  }

  /* DELETE TESTS */

  delNullEmail(callback) {
    console.log('Checking for BAD REQUEST when deleting a user with a null email...');

    return this.thunder.deleteUser(null, this.userDetails.password, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'DELETE NULL EMAIL', callback);
    });
  }

  delEmptyEmail(callback) {
    console.log('Checking for BAD REQUEST when deleting a user with an empty email...');

    return this.thunder.deleteUser('', this.userDetails.password, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'DELETE EMPTY EMAIL', callback);
    });
  }

  delNullPassword(callback) {
    console.log('Checking for BAD REQUEST when deleting a user with a null password...');

    return this.thunder.deleteUser(this.userDetails.email.address, null,
      (err, statusCode, result) => {
        this.handleResponse(err, result, statusCode, 400, 'DELETE NULL PASSWORD', callback);
      });
  }

  delEmptyPassword(callback) {
    console.log('Checking for BAD REQUEST when deleting a user with an empty password...');

    return this.thunder.deleteUser(this.userDetails.email.address, '',
      (err, statusCode, result) => {
        this.handleResponse(err, result, statusCode, 400, 'DELETE EMPTY PASSWORD', callback);
      });
  }

  delNonexistantUser(callback) {
    console.log('Checking for NOT FOUND when deleting a nonexistant user...');

    return this.thunder.deleteUser('test@test.com', 'test', (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 404, 'DELETE NULL PASSWORD', callback);
    });
  }

  delWrongPassword(callback) {
    console.log('Checking for UNAUTHORIZED when deleting a user with the wrong password...');

    return this.thunder.deleteUser(this.userDetails.email.address, 'wrong-password',
      (err, statusCode, result) => {
        this.handleResponse(err, result, statusCode, 401, 'DELETE NULL PASSWORD', callback);
      });
  }

  del(callback) {
    console.log('Attempting to delete the user...');

    return this.thunder.deleteUser(this.userDetails.email.address, this.userDetails.password,
      (err, statusCode, result) => {
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

    AWSClient.createDynamoTable('pilot-users-test', this.docker, (err) => {
      if (err) return callback(err);

      console.log('Done creating table\n');
      return callback(null);
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

      console.log();
      return callback(null);
    }

    console.log('An error occurred while performing method %s', methodName);
    console.log('Status Code: %d, Expected: %d', statusCode, expectedCode);

    if (this.verbose) {
      console.log('Details:');
      console.log(result);
    }

    console.log();

    if (err) callback(err);
    else {
      callback(new Error('Status codes do not match. Expected: ' + expectedCode
        + ', Actual: ' + statusCode));
    }
  }
}

module.exports = TestCases;
