# Thunder Scripts

This directory contains a number of scripts to ease development and testing.

## Explanation of Directories

* `aws` - This holds templates and scripts that can be used to deploy AWS resources such as
DynamoDB tables.
* `kubernetes` - This holds Kubernetes templates that can be used with few modifications to deploy
Thunder on a Kubernetes cluster.
* `logo` - This holds image files for the Thunder logo.
* `lib` - This is source code that is used in the `tools` scripts. All code is written in Node.js.
* `tools` - This holds scripts that improve development life, such as bootstrapping a new machine,
running local dependencies, or running a full integration test.

## Available Scripts

```bash
$ ./tools/bootstrap.sh
```

Use this script when pulling down the Thunder repo for the first time on a new machine.
This script will install all necessary dependencies to get you up and running quickly.

```bash
$ ./tools/integration-tests.sh
```

Use this script to run all integration tests against the current locally packaged source code.
This script will start dependencies and Thunder locally, and then run the integration test suite.

```bash
$ node tools/run-local-dependencies.js
```

Use this script to start the local dependencies (DynamoDB and SES)
so that testing with Thunder locally is easy.
This is convienent when running Thunder repeatedly from IntelliJ or the command line.
Just keep this script running in between Thunder starts and stops,
and you won't have to worry about communication with AWS.

```bash
$ node tools/test-runner.js
```

Use this script to run integration tests if Thunder is already running.
Typically, you will want to use `tools/integration-tests.sh` to run integration tests
so that you do not have to start Thunder separately.

```bash
$ node tools/run-thunder-command.js
```

Use this script to run an individual Thunder command such as `GET` or `DELETE`.
More information can be found in the
[wiki](https://github.com/RohanNagar/thunder/wiki/Running-Node.js-Scripts#single-operations).
