const deepEqual = require('deep-equal');

/**
 * Handles a response from Thunder by checking expected results and logging information.
 *
 * @param {Error} err - The error object that was returned from the Thunder call.
 * @param {int} statusCode - The status code of the Thunder call.
 * @param {object} result - The result that was returned from the Thunder call.
 * @param {string} name - The name of the test method.
 * @param {int} expectedCode - The expected status code.
 * @param (object} expectedResult - The expected response from Thunder.
 * @param {boolean} verbose - True to log more information, false to log less.
 * @return {Error} An error if the response was not as expected, null otherwise.
 */
function handleResponse(err, statusCode, result, name, expectedCode, expectedResult, verbose) {
  if (statusCode === expectedCode) {
    if (deepEqual(result, expectedResult) ||
          (name.toLowerCase().includes('swagger') &&
          result.info.title === expectedResult.info.title &&
          result.info.description === expectedResult.info.description)) {
      console.log('Successfully completed method %s (Status Code: %d, Expected: %d)',
          name, statusCode, expectedCode);

      if (verbose) {
        console.log('Response:');
        console.log(result);
      }

      console.log();
      return null;
    }
  }

  console.log('An error occurred while performing method %s', name);
  console.log('Status Code: %d, Expected: %d', statusCode, expectedCode);
  console.log('Response:');
  console.log(result);
  console.log('Expected:');
  console.log(expectedResult);
  console.log();

  return new Error('Expected status code or response does not match.');
}

module.exports = {
  handleResponse
};
