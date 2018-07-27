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

    this.testPipeline = [
      this.begin.bind(this),

      // CREATE
      this.createNullUser.bind(this),
      // DISABLED this.createNullEmail.bind(this),
      this.createInvalidEmail.bind(this),
      this.createInvalidProperties.bind(this),
      this.create.bind(this),
      this.createDuplicateUser.bind(this),

      // GET
      this.getNullEmail.bind(this),
      this.getEmptyEmail.bind(this),
      // DISABLED this.getNullPassword.bind(this),
      this.getEmptyPassword.bind(this),
      this.getNonexistantUser.bind(this),
      this.getIncorrectPassword.bind(this),
      this.get.bind(this),

      // EMAIL
      this.emailNullEmail.bind(this),
      this.emailEmptyEmail.bind(this),
      // DISABLED this.emailNullPassword.bind(this),
      this.emailEmptyPassword.bind(this),
      this.emailNonexistantUser.bind(this),
      this.email.bind(this),

      // VERIFY
      this.verifyNullEmail.bind(this),
      this.verifyEmptyEmail.bind(this),
      this.verifyNullToken.bind(this),
      this.verifyEmptyToken.bind(this),
      this.verifyBadToken.bind(this),
      this.verify.bind(this),
      // DISABLED this.verifyHtml.bind(this),

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
      this.del.bind(this)];
  }

  /* CREATE TESTS */

  createNullUser(callback) {
    console.log('Checking for BAD REQUEST when creating a null user...');

    return this.thunder.createUser(null, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'CREATE NULL USER', callback);
    });
  }

  createNullEmail(callback) {
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

  createInvalidEmail(callback) {
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

  createInvalidProperties(callback) {
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

  create(callback) {
    console.log('Attempting to create a new user...');

    return this.thunder.createUser(this.userDetails, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 201, 'CREATE', callback);
    });
  }

  createDuplicateUser(callback) {
    console.log('Checking for CONFLICT when creating a duplicate user...');

    return this.thunder.createUser(this.userDetails, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 409, 'CREATE DUPLICATE USER', callback);
    });
  }

  /* GET TESTS */

  getNullEmail(callback) {
    console.log('Checking for BAD REQUEST when getting a user using a null email...');

    return this.thunder.getUser(null, this.userDetails.password, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'GET NULL EMAIL', callback);
    });
  }

  getEmptyEmail(callback) {
    console.log('Checking for BAD REQUEST when getting a user using an empty email...');

    return this.thunder.getUser('', this.userDetails.password, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'GET EMPTY EMAIL', callback);
    });
  }

  getEmptyPassword(callback) {
    console.log('Checking for BAD REQUEST when getting a user using an empty password...');

    return this.thunder.getUser(this.userDetails.email.address, '', (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'GET EMPTY PASSWORD', callback);
    });
  }

  getNonexistantUser(callback) {
    console.log('Checking for NOT FOUND when getting a nonexistant user...');

    return this.thunder.getUser('test@test.com', 'password', (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 404, 'GET NONEXISTANT USER', callback);
    });
  }

  getIncorrectPassword(callback) {
    console.log('Checking for UNAUTHORIZED when getting a user using an incorrect password...');

    return this.thunder.getUser(this.userDetails.email.address, 'ERROR',
      (err, statusCode, result) => {
        this.handleResponse(err, result, statusCode, 401, 'GET INCORRECT PASSWORD', callback);
      });
  }

  get(callback) {
    console.log('Attempting to get the user...');

    return this.thunder.getUser(this.userDetails.email.address, this.userDetails.password,
      (err, statusCode, result) => {
        this.handleResponse(err, result, statusCode, 200, 'GET', callback);
      });
  }

  /* EMAIL TESTS */

  emailNullEmail(callback) {
    console.log('Checking for BAD REQUEST when sending a verification email to a null email...');

    return this.thunder.sendEmail(null, this.userDetails.password, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'EMAIL NULL EMAIL', callback);
    });
  }

  emailEmptyEmail(callback) {
    console.log('Checking for BAD REQUEST when sending a verification email to an empty email...');

    return this.thunder.sendEmail('', this.userDetails.password, (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'EMAIL EMPTY EMAIL', callback);
    });
  }

  emailNullPassword(callback) {
    console.log('Checking for BAD REQUEST when sending an email with a null password...');

    return this.thunder.sendEmail(this.userDetails.email.address, null,
      (err, statusCode, result) => {
        this.handleResponse(err, result, statusCode, 400, 'EMAIL NULL PASSWORD', callback);
      });
  }

  emailEmptyPassword(callback) {
    console.log('Checking for BAD REQUEST when sending an email with an empty password...');

    return this.thunder.sendEmail(this.userDetails.email.address, '', (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 400, 'EMAIL EMPTY PASSWORD', callback);
    });
  }

  emailNonexistantUser(callback) {
    console.log('Checking for NOT FOUND when sending an email to a nonexistant user...');

    return this.thunder.sendEmail('test@test.com', 'password', (err, statusCode, result) => {
      this.handleResponse(err, result, statusCode, 404, 'EMAIL NONEXISTANT USER', callback);
    });
  }

  email(callback) {
    console.log('Attempting to send a verification email...');

    return this.thunder.sendEmail(this.userDetails.email.address, this.userDetails.password,
      (err, statusCode, result) => {
        if (!err) this.userDetails = result; // Update userDetails with given token

        this.handleResponse(err, result, statusCode, 200, 'EMAIL', callback);
      });
  }

  /* VERIFY TESTS */

  verifyNullEmail(callback) {
    console.log('Checking for BAD REQUEST when verifying with a null email...');

    return this.thunder.verifyUser(null, this.userDetails.email.verificationToken,
      (err, statusCode, result) => {
        this.handleResponse(err, result, statusCode, 400, 'VERIFY NULL EMAIL', callback);
      });
  }

  verifyEmptyEmail(callback) {
    console.log('Checking for BAD REQUEST when verifying with an empty email...');

    return this.thunder.verifyUser('', this.userDetails.email.verificationToken,
      (err, statusCode, result) => {
        this.handleResponse(err, result, statusCode, 400, 'VERIFY EMPTY EMAIL', callback);
      });
  }

  verifyNullToken(callback) {
    console.log('Checking for BAD REQUEST when verifying with a null token...');

    return this.thunder.verifyUser(this.userDetails.email.address, null,
      (err, statusCode, result) => {
        this.handleResponse(err, result, statusCode, 400, 'VERIFY NULL TOKEN', callback);
      });
  }

  verifyEmptyToken(callback) {
    console.log('Checking for BAD REQUEST when verifying with an empty token...');

    return this.thunder.verifyUser(this.userDetails.email.address, '',
      (err, statusCode, result) => {
        this.handleResponse(err, result, statusCode, 400, 'VERIFY EMPTY TOKEN', callback);
      });
  }

  verifyBadToken(callback) {
    console.log('Checking for BAD REQUEST when verifying with an incorrect password...');

    return this.thunder.verifyUser(this.userDetails.email.address, 'wrong-token',
      (err, statusCode, result) => {
        this.handleResponse(err, result, statusCode, 400, 'VERIFY BAD TOKEN', callback);
      });
  }

  verify(callback) {
    console.log('Attempting to verify the created user...');

    return this.thunder.verifyUser(
      this.userDetails.email.address,
      this.userDetails.email.verificationToken,
      (err, statusCode, result) => {
        if (!err) this.userDetails = result; // Update userDetails with verified status

        this.handleResponse(err, result, statusCode, 200, 'VERIFY', callback);
      });
  }

  verifyHtml(callback) {
    console.log('Attempting to verify the created user and check for HTML...');

    return this.thunder.verifyUser(
      this.userDetails.email.address,
      this.userDetails.email.verificationToken,
      (err, statusCode, result) => {
        this.handleResponse(err, result, statusCode, 200, 'VERIFY', callback);
      }, 'html');
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

  /**
   * Deletes the user from Thunder.
   *
   * @param {object} data - The user data to delete.
   * @param {function} callback - The function to call on completion.
   * @return When the deletion event has begun.
   */
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

    AWSClient.createDynamoTable('pilot-users-test', (err) => {
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
