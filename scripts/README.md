# Thunder Scripts

This directory contains a number of scripts to ease development and enable integration testing.

- [Getting Started](#getting-started)
- [Explanation of Directories](#explanation-of-directories)
- [Available Scripts](#available-scripts)
- [Writing New Integration Tests](#writing-new-integration-tests)

## Getting Started

First, make sure you have Node.js and NPM installed. If you have run `tools/bootstrap.sh`, you should
be good to go.

Second, install the required NPM packages.

```bash
$ npm install
```

Now you're set to run any of the available scripts.

## Explanation of Directories

* `aws` - This holds templates and scripts that can be used to deploy AWS resources such as
DynamoDB tables.
* `ci` - This holds scripts used by Travis CI, such as pushing a Docker image or running Docker integration tests.
* `kubernetes` - This holds Kubernetes templates that can be used with few modifications to deploy
Thunder on a Kubernetes cluster.
* `lib` - This is source code that is used in the `tools` scripts. All code is written in Node.js.
* `logo` - This holds image files for the Thunder logo.
* `tests` - This holds the integration test runner script along with a directory for each integration test.
To add a new test, create a new directory with that test name and add the relevant files: `config.yaml`,
`docker-compose.yml`, and `tests.yaml`
* `tools` - This holds scripts that improve development life, such as bootstrapping a new machine,
running local dependencies, or running individual Thunder commands.

## Available Scripts

- [Bootstrap](#bootstrap)
- [Local Dependencies](#local-dependencies)
- [Single Operations](#single-operations)
  - [Create User](#create-user)
  - [Get User](#get-user)
  - [Update User](#update-user)
  - [Delete User](#delete-user)
  - [Send Email](#send-email)
  - [Verify User](#verify-user)
- [Test Runner](#test-runner)
- [Managed Integration Tests](#managed-integration-tests)

### Bootstrap

```bash
$ ./tools/bootstrap.sh
```

Use this script when pulling down the Thunder repo for the first time on a new machine.
This script will install all necessary dependencies to get you up and running quickly.

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

```bash
$ node tools/run-thunder-command.js
```

Use this script to run an individual Thunder command against a running instance of Thunder. This
provides more convenience when needing to do a single operation, either during testing or in production.

Each command has the following optional arguments.

|Flag|Description|Default Value|
|:---:|:---:|:---:|
|-h|Display a help message and exit.|----|
|-e|The endpoint to connect to the running instance of Thunder.|`http://localhost:8080`|
|-a|The basic authentication credentials in the form `{app_name}:{app_secret}`.|`application:secret`|
|-vb|Increase the output verbosity.|`False`|

Additionally, each command has their own set of arguments, both required and optional.
Read below for more information on the individual commands.

#### Create User

To create a user, you can optionally supply the filename of the JSON file containing the details
of the user to create. By default, the filename value is `scripts/tools/default_user.json`.

```bash
$ node tools/run-thunder-command.js -e <endpoint> -a <auth> -vb create -f <filename>
```

#### Get User

To get a user, you must supply the email and password of the user to get as positional arguments.

```bash
$ node tools/run-thunder-command.js -e <endpoint> -a <auth> -vb get <email> <password>
```

#### Update User

To update a user, you must supply the email and password of the user to update as positional arguments.
You can also optionally supply the filename of the JSON file containing the details of the state to update the user to.
By default, the filename value is `scripts/tools/default_user.json`.

```bash
$ node tools/run-thunder-command.js -e <endpoint> -a <auth> -vb update <email> <password> -f <filename>
```

#### Delete User

To delete a user, you must supply the email and password of the user to delete as positional arguments.

```bash
$ node tools/run-thunder-command.js -e <endpoint> -a <auth> -vb delete <email> <password>
```

#### Send Email

To send a verification email, you must supply the email and password of the user as positional arguments.

```bash
$ node tools/run-thunder-command.js -e <endpoint> -a <auth> -vb email <email> <password>
```

#### Verify User

To verify a user, you must supply the email and verification token of the user as positional arguments.

```bash
$ node tools/run-thunder-command.js -e <endpoint> -a <auth> -vb verify <email> <token>
```

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

## Writing New Integration Tests

Integration tests are defined in a YAML file. To add a new integration test suite, use the following steps.

1. Create a new directory inside `tests/` with the name of your new test.

2. Inside that directory, create a new file called `tests.yaml` and fill out your test cases.
Look at `tests/general/tests.yaml` for an example of possible test cases. In general,
the format for a test case will look like the following:

```yaml
- name: NAME OF TEST
  type: [create|get|update|delete|email|verify]
  disabled: [true|false] # Optional, default is false
  log: 'Log line to output before running the test'
  body: # Only used for create or update
  existingEmail: # Only used for update
  email: # Used for get, delete, email, verify
  token: # Only used for verify
  password: # Used for get, update, delete, email
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
