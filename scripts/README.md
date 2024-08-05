# Thunder Scripts

This directory contains a number of scripts to ease development and enable integration testing.

- [Getting Started](#getting-started)
- [Explanation of Directories](#explanation-of-directories)
- [Available Scripts](#available-scripts)
- [Testing with Docker](#testing-with-docker)
- [Writing New Integration Tests](#writing-new-integration-tests)

## Getting Started

- To run integration tests, you need to have Node.js and NPM installed. Then, install the required NPM packages:

```bash
$ npm install
```

- To run tests using docker-compose, install `docker` and `docker-compose`.


- To run Helm chart validation tests, install Go.

## Explanation of Directories

* `ci` - This holds scripts used by Github Actions during CI runs.
* `deploy` - This holds scripts and templates that can be used to deploy Thunder and related resources.
* `tests` - This consists of a directory for each integration test that is run using `docker-compose`
  and `artillery`. To add a new test, create a new directory with that test name and add the relevant files:
  `config.yaml`, `docker-compose.yml`, and `tests.yml`.
* `tools` - This holds scripts that improve development life, such as updating specific dependencies,
  running local dependencies, or running integration tests.

## Available Scripts

- [Local Dependencies](#local-dependencies)
- [Single Operations](#single-operations)
- [Integration Tests with Artillery](#integration-tests-with-artillery)
- [Managed Integration Tests](#managed-integration-tests)

### Local Dependencies

```bash
$ node tools/run-local-dependencies.js
```

Use this script to start the local dependencies (DynamoDB and SES)
so that testing with Thunder locally is easy.
This is convenient when running Thunder repeatedly from IntelliJ or the command line.
Just keep this script running in between Thunder starts and stops,
and you won't have to worry about communication with dependencies.

### Single Operations

To run an individual Thunder command, start Thunder and go to `/swagger` to use Swagger UI.

### Integration Tests with Artillery

```bash
$ ./node_modules/.bin/artillery run "tests/general/tests.yml"
```

To run integration tests, we use [artillery](https://artillery.io).
You will need to supply an artillery test case file for the script to run.
Make sure Thunder is running, either locally or with the `docker-compose.yml` file
located in each testcase under `tests/`. Then, run the artillery test.

### Managed Integration Tests

```bash
$ ./tools/integration-tests.sh
```

Use this script to run a set of integration tests against the current locally packaged source code.
This script will start dependencies and Thunder locally, and then run the integration test suite.

## Testing with Docker

An easy way to run integration tests is by using Docker. Essentially, each directory
inside `tests/` contains three files:

1. `config.yaml` - The config file that defines how Thunder should behave.
2. `tests.yml` - The test cases to run against Thunder.
3. `docker-compose.yml` - A file that sets up Docker containers using docker-compose.

You can run any of the tests by first starting Thunder locally with the correct config and then running
`./node_modules/.bin/artillery run ...` with the correct test case as the argument. However, you can also run these
tests with Docker and ensure the correct configuration is used so that you don't have to manually manage the config.

In order to run these tests using Docker, you need docker and docker-compose installed.
Then run the following by replacing `TEST_NAME` with the name of your test.

```bash
$ sudo docker compose -f tests/TEST_NAME/docker-compose.yml up -d

$ ./node_modules/.bin/artillery run tests/TEST_NAME/tests.yml
```

## Writing New Integration Tests

Integration tests are defined in a YAML file. To add a new integration test suite, use the following steps.

1. Create a new directory inside `tests/` with the name of your new test.

2. Inside that directory, create a new file called `tests.yml` and fill out your test cases.
   Look at `tests/general/tests.yaml` for an example of test cases, or read the
   [artillery documentation](https://artillery.io/docs/guides/overview/welcome.html).

3. Create a new file called `config.yaml` inside the same directory. This should be the config used
   by Thunder when it starts up. See `tests/general/config.yaml` for an example.

4. Finally, create a new file called `docker-compose.yml` to define how Thunder should come up. See
   `tests/general/docker-compose.yml` for an example.

5. You can run your test suite with artillery as described above. For example:

```bash
$ ./node_modules/.bin/artillery run tests/my-test/tests.yaml
```

6. Make sure to add the test suite to the Github Actions workflow.
   Open `../.github/workflows/ci.yml` and in the `integration-test` job, add the test to the matrix.

```yaml
integration-test:
  runs-on: ubuntu-latest
  needs: [build]
  strategy:
    matrix:
      testname:
        - bad-request                # Load tests bad requests to all endpoints
        - general                    # Functional DynamoDB test
        - mongodb                    # Functional MongoDB test
        - update-existing-email      # Functional test to user data is not deleted on email update
        - bcrypt                     # Functional test to ensure bcrypt server-side hash works
        - sha256                     # Functional test to ensure sha256 server-side hash works
        - disabled-email-and-swagger # Functional test to ensure disabling endpoints returns 404
        - disabled-password-header   # Functional test to ensure passwords are not required on disable
        # Put your new test here
        - my-new-test                # Description of test
```
