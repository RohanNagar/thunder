# Thunder Scripts

This directory contains a number of scripts to ease development and testing.

## Explanation of Directories

* `resources` - This holds resource files that are used within the source code, such as images, configuration, or data files.
* `src` - This is the source code for running integration tests against Thunder. All code is written in Node.js.
* `tools` - This holds scripts that improve development life, such as bootstrapping a new machine, running local dependencies, or running a full integration test.

## Available Scripts

* `tools/bootstrap.sh`

```bash
$ ./tools/bootstrap.sh
```

Use this script when pulling down the Thunder repo for the first time on a new machine.
This script will install all necessary dependencies to get you up and running quickly.

* `tools/integration-tests.sh`

```bash
$ ./tools/integration-tests.sh
```

Use this script to run all integration tests against the current locally packaged source code.
This script will start dependencies and Thunder locally, and then run the integration test suite.

* `tools/run-local-dependencies.js`

```bash
$ node tools/run-local-dependencies.js
```

Use this script to start the local dependencies (DynamoDB and SES) so that testing with Thunder locally is easy.
This is convienent when running Thunder repeatedly from IntelliJ or the command line.
Just keep this script running in between Thunder starts and stops, and you won't have to worry about communication with AWS.

