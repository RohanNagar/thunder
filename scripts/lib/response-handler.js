const deepEqual = require('deep-equal');

/**
 * Handles a response from Thunder by checking expected results and logging information.
 *
 * @param {int} statusCode - The status code of the Thunder call.
 * @param {object} result - The result that was returned from the Thunder call.
 * @param {string} name - The name of the test method.
 * @param {int} expectedCode - The expected status code.
 * @param (object} expectedResult - The expected response from Thunder.
 * @param {boolean} verbose - True to log more information, false to log less.
 * @return {Error} An error if the response was not as expected, null otherwise.
 */
function handleResponse(statusCode, result, name, expectedCode, expectedResult, verbose) {
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

/**
 * Checks the metrics response to ensure the expected metrics and values are contained in the
 * response.
 *
 * @param {int} statusCode - The status code of the Thunder metrics call.
 * @param {object} result - The result that was returned from the Thunder call.
 * @param {string} name - The name of the test method.
 * @param (object} expectedMetrics - The expected metrics and their values.
 * @param {boolean} verbose - True to log more information, false to log less.
 * @return {Error} An error if the response was not as expected, null otherwise.
 */
function checkMetrics(statusCode, result, name, expectedMetrics, verbose) {
  if (statusCode !== 200) {
    console.log('The status code from the metrics was not 200. Status Code: %d', statusCode);

    return new Error('Status code was not 200 for metrics.');
  }

  let failure = false;

  expectedMetrics.forEach((expected) => {
    let value = 0;

    if (expected.name in result.meters) {
      console.log('The metrics name %s was found in the response as a meter.', expected.name);

      value = result.meters[expected.name].count;
    } else if (expected.name in result.counters) {
      console.log('The metrics name %s was found in the response as a counter.', expected.name);

      value = result.counters[expected.name].count;
    } else {
      console.log('The metrics name %s was not found in the metrics response.', expected.name);
      failure = true;

      return;
    }

    if (value !== expected.value) {
      console.log('The metric value for metric %s did not match. Value: %d, Expected: %d',
          expected.name, value, expected.value);
      failure = true;

      return;
    }
  });

  if (failure) {
    console.log('There were failures when checking metrics.');

    return new Error('There were failures when checking metrics.');
  }

  console.log('Successfully completed method %s.', name);

  if (verbose) {
    console.log('Response:');
    console.log(result);
  }

  console.log();
  return null;
}

/**
 * Checks the health check response to ensure the application is healthy.
 *
 * @param {int} statusCode - The status code of the Thunder health check call.
 * @param {object} result - The result that was returned from the Thunder call.
 * @param {string} name - The name of the test method.
 * @param {boolean} verbose - True to log more information, false to log less.
 * @return {Error} An error if the response was not as expected, null otherwise.
 */
function checkHealth(statusCode, result, name, verbose) {
  if (statusCode !== 200) {
    console.log('The status code from the health check was not 200. Status Code: %d', statusCode);

    return new Error('Status code was not 200 for healthcheck.');
  }

  let failure = false;

  Object.keys(result).forEach((key) => {
    if (!result[key].healthy) {
      console.log('The %s health check is unhealthy.', key);

      failure = true;
    }

    console.log('Found health check: %s. Healthy: %s', key, result[key].healthy);
  });

  if (failure) {
    console.log('One or more health checks were unhealthy.');

    return new Error('One or more health checks were unhealthy.');
  }

  console.log('Successfully completed method %s.', name);

  if (verbose) {
    console.log('Response:');
    console.log(result);
  }
}

module.exports = {
  handleResponse,
  checkMetrics,
  checkHealth
};
