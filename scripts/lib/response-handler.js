const deepEqual = require('deep-equal')

function handleResponse(err, statusCode, result, name, expectedCode, expectedResult, verbose) {
  if (statusCode === expectedCode && deepEqual(result, expectedResult)) {
    console.log('Successfully completed method %s (Status Code: %d, Expected: %d)',
      name, statusCode, expectedCode);

    if (verbose) {
      console.log('Response:');
      console.log(result);
    }

    console.log();
    return null;
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
