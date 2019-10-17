# Thunder Scripts

This directory contains a number of scripts to ease development and enable integration testing.

- [Getting Started](#getting-started)
- [Explanation of Directories](#explanation-of-directories)
- [Available Scripts](#available-scripts)
- [Testing with Docker](#testing-with-docker)
- [Writing New Integration Tests](#writing-new-integration-tests)

## Getting Started

First, make sure you have Node.js and NPM installed.

Second, install the required NPM packages.

```bash
$ npm install
```

Now you're set to run any of the available scripts.

If you want to run tests using docker-compose, make sure to install `docker` and `docker-compose`.

## Explanation of Directories

* `ci` - This holds scripts used by Travis CI, such as pushing a Docker image or running multiple integration tests.
* `deploy` - This holds scripts and templates that can be used to deploy Thunder and related resources.
* `lib` - This is source code that is used in the `tools` scripts. All code is written in Node.js.
* `tests` - This holds the integration test runner script along with a directory for each integration test.
To add a new test, create a new directory with that test name and add the relevant files: `config.yaml`,
`docker-compose.yml`, and `tests.yaml`
* `tools` - This holds scripts that improve development life, such as updating specific dependencies,
running local dependencies, or running integration tests.

## Available Scripts

- [Local Dependencies](#local-dependencies)
- [Single Operations](#single-operations)
- [Test Runner](#test-runner)
- [Managed Integration Tests](#managed-integration-tests)

### Local Dependencies

```bash
$ node tools/run-local-dependencies.js
```

Use this script to start the local dependencies (DynamoDB and SES)
so that testing with Thunder locally is easy.
This is convenient when running Thunder repeatedly from IntelliJ or the command line.
Just keep this script running in between Thunder starts and stops,
and you won't have to worry about communication with AWS.

### Single Operations

The single operation script was removed in favor of using Swagger UI.
To run an individual Thunder command, start Thunder and go to `/swagger` to use Swagger UI.

### Test Runner

```bash
$ node tests/test-runner.js
```

Use this script to run integration tests if Thunder is already running. This is the main testing file.
You will need to supply a test case file for the script to run. Additionally, there are optional arguments
as described in the table below.

|Flag|Description|Default Value|
|:---:|:---:|:---:|
|-h|Display a help message and exit.|----|
|-v|Display the version of the script and exit.|----|
|-e|The endpoint to connect to the running instance of Thunder.|`http://localhost:8080`|
|-a|The basic authentication credentials in the form `{app_name}:{app_secret}`.|`application:secret`|
|-d|Use this flag if testing against a DynamoDB instance running in the Docker host.|`false`|
|-l|Use this flag to start local dependencies before running tests.|`false`|
|-vb|Increase the output verbosity.|`false`|

Run the script using the following command (supplying the optional arguments if needed):

```bash
$ node tests/test-runner.js -e <endpoint> -a <auth> -l -vb path/to/test-cases.yaml
```

### Managed Integration Tests

```bash
$ ./tools/integration-tests.sh
```

Use this script to run all integration tests against the current locally packaged source code.
This script will start dependencies and Thunder locally, and then run the integration test suite.

## Testing with Docker

An easy way to run integration tests is by using Docker. Essentially, each directory
inside `tests/` contains three files:

1. `config.yaml` - The config file that defines how Thunder should behave.
2. `tests.yaml` - The test cases to run against Thunder.
3. `docker-compose.yml` - A file that sets up Docker containers using docker-compose.

You can run any of the tests by first starting Thunder locally and then running the
`tests/test-runner.js` script with the correct test case as the argument. However, you can also run these
tests with Docker and ensure the correct configuration is used so that you don't have to manually manage the config.

In order to run these tests using Docker, you need docker and docker-compose installed.
Then run the following by replacing `TEST_NAME` with the name of your test.

```bash
$ sudo docker-compose -f tests/TEST_NAME/docker-compose.yml up -d

$ node tests/test-runner.js tests/TEST_NAME/tests.yaml
```

## Writing New Integration Tests

Integration tests are defined in a YAML file. To add a new integration test suite, use the following steps.

1. Create a new directory inside `tests/` with the name of your new test.

2. Inside that directory, create a new file called `tests.yaml` and fill out your test cases.
Look at `tests/general/tests.yaml` for an example of possible test cases. In general,
the format for a test case will look like the following:

```yaml
- name: NAME OF TEST
  type: [create|get|update|delete|email|verify|swagger|metrics]
  disabled: [true|false] # Optional, default is false
  log: 'Log line to output before running the test'
  body: # Only used for create or update
  existingEmail: # Only used for update
  email: # Used for get, delete, email, verify
  token: # Only used for verify
  password: # Used for get, update, delete, email
  responseType: # Used for verify, swagger
  expectedMetrics: # Used for metrics
    - name:
      value:
  expectedCode: 400
  expectedResponse: 'Expected response message or body'
```

3. Create a new file called `config.yaml` inside the same directory. This should be the config used
by Thunder when it starts up. See `tests/general/config.yaml` for an example.

4. Finally, create a new file called `docker-compose.yml` to define how Thunder should come up. See
`tests/general/docker-compose.yml` for an example.

5. You can run your test suite with `tests/test-runner.js`. For example:

```bash
$ node tests/test-runner.js tests/my-test/tests.yaml
```

6. Make sure to add the test suite to the Travis build. Open `../.travis.yml` and in stage 3, add a new job:

```yaml
- stage: test
  name: Integration Test - My Test
  sudo: required
  services:
    - docker
  env: CACHE_NAME=INTEGRATION#
  install:
    - npm --prefix scripts/ install
  script:
    - mvn clean package -Dmaven.test.skip=true
    - ./scripts/tests/docker-integration-tests.sh my-test
```
