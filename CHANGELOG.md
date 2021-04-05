# v3.0.1
## ⚛ Fixed
* Updated the client Javadoc return descriptions.

# v3.0.0
## ✳️ Added
* OpenAPI (Swagger) specifications are now available at `/openapi.yaml` and `/openapi.json`
* Swagger UI is available at `/swagger`
* New configuration option: Allow common password mistakes
* Ability to use different database providers
* Added MongoDB database provider
* Added Healthcheck for email providers
* Added more metrics for better observability
* New password hashing algorithm: `sha256`
* DynamoDB table will be created on application startup if it does not exist
* A Helm chart is available for deploying Thunder to a Kubernetes cluster

## ✴️ Changed
* Required `type` option on the `database` configuration.
* The `md5` password hashing algorithm is no longer available. You should use `sha256` instead.
* Property validation configuration has changed, and allows for more flexible validation. See the docs for more details:

```yaml
properties:
  allowSubset: [true|false]
  allowSuperset: [true|false]
  allowed:
    - name:
      type:
    - name:
      type:
```

* Email verification is now disabled by default. There is a new `type` option in the configuration to specify your email provider:

```yaml
email:
  type: [none|ses]
```

* Additional `User` properties are no longer contained in a JSON object. They should be included directly in the `User` object:

```json
{
  "email" : {
    "address" : "test@test.com",
    "verified" : true,
    "verificationToken" : "hashToken"
  },
  "password" : "12345",
  "customBoolean" : true,
  "customDouble" : 1.2,
  "customInt" : 1,
  "customList" : ["hello", "world"],
  "customMap" : {
    "key" : "value"
  },
  "customString" : "value"
}
```

## ⚛ Fixed
* A potential bug that would cause all of a user's data to be lost when updating a user's email address has been addressed.

## ☕ Client
* The endpoint used to build `ThunderClient` is no longer required to end in `/`.
* All methods in `ThunderClient` now return a `CompletableFuture<User>` instead of a retrofit `Call<User>`.

## ➡️ DevOps
* Migrated the CI build from `Travis CI` to `GitHub Actions CI`.
* Migrated Dependabot updates from `dependabot.com` to Github-Native.
* Added GitHub Action to automatically check for updates to the Bootstrap CSS version.
* Added Github Action to automatically approve pull requests from Dependabot.

## Notable Dependency Upgrades
* AWS Java SDK upgraded from `1.11.x` to `2.x`
* `async` in `/scripts` upgraded from `2.6.2` to `3.x`

# v2.1.0
## ✳️ Added
* New endpoint to reset a user's verification status (`POST /verify/reset`).
* Server-side hashing is now available. In the new `passwordHash` configuration, set `serverSideHash` to `true` in order to enable it. Server-side hashing will use the algorithm defined in the `algorithm` option. By default, the algorithm is `simple`, which does not actually perform a hash. You can also disable the header check for passwords. By default, most endpoints will require the `password` header to be set to the user's password. To disable this, set `headerCheck` to `false`.

  ```yaml
  passwordHash:
    algorithm: [simple|md5|bcrypt]
    serverSideHash: true
    headerCheck: true
  ```

## ✴️ Changed
* When updating a user (`PUT /users`), email verification information can no longer be overwritten. Existing verification status will remain the same, or if the email has been updated, the verification status will be reset.
* _**(docs)**_ Moved user documentation from the Github Wiki to [ReadTheDocs](https://thunder-api.readthedocs.io/en/latest/).
* _**(docs)**_ Updated all Javadoc to match new Thunder Javadoc guidelines.

## ⚛ Fixed
* _**(docs)**_ Javadoc for generated Dagger source files is no longer generated.

## ☕ Client
* Support for the new `/verify/reset` endpoint.

## ➡️ DevOps
* Run all CI tasks on Travis, and run multiple integration tests in CI.
* Update GitHub Issue templates to set default labels and assignees.

## Dependency Upgrades
* `aws-java-sdk` 1.11.385 -> 1.11.486
* `checkstyle` 8.12 -> 8.16
* `dagger` 2.17 -> 2.21
* `dropwizard` 1.3.5 -> 1.3.8
* `jackson-api` 2.9.6 -> 2.9.8
* `jacoco-maven-plugin` 0.8.1 -> 0.8.2
* `junit-jupiter` 5.2.0 -> 5.3.2
* `junit-platform` 1.2.0 -> 1.3.2
* `maven-shade-plugin` 3.1.1 -> 3.2.1
* `maven-surefire-plugin` 2.22.0 -> 2.22.1
* `mockito` 2.21.0 -> 2.23.4
* `nexus-staging-maven-plugin` 1.6.7 -> 1.6.8
* `retrofit` 2.4.0 -> 2.5.0
* _Python (Documentation) Dependencies:_
  * _`sphinx` 1.7.7 -> 1.8.2_
* _Node.js (DevOps) Dependencies:_
  * _`aws-sdk` 2.291.0 -> 2.391.0_
  * _`eslint` 5.3.0 -> 5.12.1_
  * _`eslint-config-google` 0.9.1 -> 0.11.0_
  * _`thunder-client` 0.3.0 -> 0.4.1_

# v2.0.0
## ✳️ Added

* Property Validation

  Additional properties defined in the `User` JSON can be validated on `POST` and `PUT`. Simply define the `properties` in the configuration, and they will be automatically validated. To disable validation, do not include `properties` in the configuration.

    Example:
    ```yaml
    properties:
      - name: myFirstProperty
        type: string
      - name: mySecondProperty
        type: list
    ```

  Supported property types are: `string`, `integer`, `double`, `boolean`, `list`, and `map`.

* Optionally Disable Email Verification

  You can now disable email verification if you don't want the endpoints to be active. Simply set the `enabled` option to `false`:
  ```yaml
  email:
    enabled: false
  ```

* More Email Configuration Options

  Use your own HTML pages or email message bodies, or use a custom subject line! Default ones are provided, but you can specify your own:
    ```yaml
    email:
      messageOptions:
        subject: Welcome to My App
        bodyHtmlFilePath: /path/to/verification.html
        bodyTextFilePath: /path/to/verification.txt
        urlPlaceholderString: PLACEHOLDER
        successHtmlFilePath: /path/to/success.html
    ```

  On each `POST` `/verify` request, a verification URL will be generated for the specific user and a String replacement will replace the `urlPlaceholderString` with the correct URL before sending the message. The default placeholder string is `CODEGEN-URL`.

* Documentation on [How to Get Started with HTTPS](/RohanNagar/thunder/wiki/Quick-Start)

## ✴️ Changed

* ⚠️ `PilotUser` has been renamed to `User`
* ⚠️ The user object now has an expandable map of properties, so the user JSON is no longer confined to what is defined in the code.

    Example of new User object:
    ```json
    {
      "email" : {
        "address" : "test@test.com",
        "verified" : "true",
        "verificationToken" : "hashToken"
      },
      "password" : "12345",
      "properties" : {
        "stringProperty" : "myUserObject",
        "integerProperty": 1000,
        "listsWorkToo": ["hello", "world"]
      }
    }
    ```
  This applies to **ALL** `/user` methods: `GET`, `POST`, `PUT`, `DELETE` and **ALL** `/verify` methods: `GET`, `POST`
* ⚠️ All configuration options that used hyphens now are camel-case. For example, `table-name` has become `tableName`
* ⚠️ The `ses` configuration object has been renamed to `email`
* ⚠️ The `dynamo` configuration object has been renamed to `database`
* ⚠️ The package name for the `api`, `application`, and `client` have changed from `com.sanction.thunder` to `com.sanctionco.thunder`

## ⚛ Fixed
* `POST` `/verify` now correctly checks for a matching user password in the request header before sending the email.

## ☕ Client
* ⚠️ `PilotUser` renamed to `User` and object definition changed. See the `Features` section above for more information.

## ➡️ DevOps
* New [logo](https://github.com/RohanNagar/thunder/blob/master/scripts/resources/vertical.png)!
* Multiple custom issue templates added for creating new Github issues
* Enforce Javadoc on class definitions
* Switch to [Codecov](https://codecov.io/gh/RohanNagar/thunder) for coverage reports
* A new integration test format, more thorough tests, and integration tests against the Docker image
* Maven release build adds Javadoc, sources, and GPG signatures

## Dependency Upgrades
* `aws-java-sdk` 1.11.311 -> 1.11.385
* `checkstyle` 8.9 -> 8.12
* `dagger` 2.15 -> 2.17
* `dropwizard` 1.3.1 -> 1.3.5
* `jackson-api` 2.9.5 -> 2.9.6
* `junit` 4.12 -> 5.2.0
* `maven-compiler-plugin` 3.7.0 -> 3.8.0
* `maven-shade-plugin` 2.3 -> 3.1.1
* `maven-surefire-plugin` 2.21.0 -> 2.22.0
* `mockito` 2.18.0 -> 2.21.0
* _Node.js (DevOps) Dependencies:_
  * _`async` 2.6.0 -> 2.6.1_
  * _`aws-sdk` 2.238.1 -> 2.291.0_
  * _`eslint` 4.19.1 -> 5.3.0_
  * _`thunder-client` 0.1.0 -> 0.2.0_

# v1.2.0

## Features
* Add support for returning HTML in the response for `/verify`

## Client
* ⚠️ Upgraded Retrofit from v1.9 to v2.4
  * All endpoint URLs passed into `ThunderBuilder` must end in a slash `/`
  * `ThunderClient` now returns a `Call<PilotUser>` instead of `PilotUser`. Example:

     1.1.2 (old):
     ```
     PilotUser user = thunderClient.getUser("USERNAME", "PASSWORD");
     ```

     1.2.0 (new):
     ```
     PilotUser user = thunderClient.getUser("USERNAME", "PASSWORD").execute().body();
     ```
* Added `sendVerificationEmail()` method that calls `POST /verify`
* Added `verifyUser()` overload that provides an option for the `ResponseType` (either HTML or JSON)

## DevOps
* Added `bootstrap.sh` script to easily bootstrap a new development machine with dependencies
* All Node.js code is now being checked for code style using ESLint
* The `thunder-client` code has been moved into its own package [here](https://github.com/RohanNagar/thunder-client-js)
* Introduce a build on GitLab for Docker builds. See the mirror [here](https://gitlab.com/RohanNagar/thunder)
* Thunder is now available as a Docker image! [Click here](https://hub.docker.com/r/rohannagar/thunder/)
* Added Kubernetes deployment files to easily deploy Thunder on a K8s cluster

## Miscellaneous

* Code coverage is now at 99% 🎉

## Dependency Upgrades
* `aws-java-sdk` 1.11.275 -> 1.11.311
* `checkstyle` 8.2 -> 8.9 [Change `ImportOrder` check to `CustomImportOrder`]
* `dagger` 2.14.1 -> 2.15
* `dropwizard` 1.2.3 -> 1.3.1
* `jackson` 2.9.4 -> 2.9.5
* `jacoco-plugin` 0.8.0 -> 0.8.1
* `mockito` 2.13.0 -> 2.18.0
* `retrofit` 1.9.0 -> 2.4.0 [⚠️ Breaking change - see above]
* _Node.js (DevOps) Dependencies:_
  * _`argparse` 1.0.9 -> 1.0.10_
  * _`aws-sdk` 2.192.0 -> 2.224.1_


# v1.1.2

## DevOps Improvements
* Travis now runs integration tests on PR checks and commits to master

## Dependency Upgrades
* `aws-java-sdk` 1.11.273 -> 1.11.275
* _Node.js (DevOps) Dependencies:_
  * _`aws-ses-local` 1.1.1 -> 1.3.0_
  * _`aws-sdk` 2.152.0 -> 2.192.0_


# v1.1.1

## DevOps Improvements
* Travis now deploys release jars to the Github Releases Page!
* Bug fix for compiling project on earlier versions of Maven (fixes JitPack build)

## Dependency Upgrades
* `aws-java-sdk` 1.11.272 -> 1.11.273


# v1.1.0

## API
* Removed basic auth from `GET /verify`
* Fixed bug with the link in sent emails

## Application Layer
* Moved SES Configuration to the `config.yaml`, including: `endpoint`, `region`, and `fromAddress`
* Added `endpoint` and `region` for DynamoDB to the `config.yaml`

## DevOps Improvements
* Replaced Python scripts with improved Node.js scripts

## Dependency Upgrades
* `aws-java-sdk` 1.11.x -> 1.11.272
* `dagger` 2.9 -> 2.14.1
* `dropwizard` 1.0.6 -> 1.2.3
* `jackson` 2.7.8 -> 2.9.4
* `mockito` 1.10.19 -> 2.13.0 [Replaced deprecated `Matchers` with `ArgumentMatchers`]


# v1.0.0
* New resource `VerificationResource`. Provides two endpoints:
  * `POST /verify` - sends an email to a user providing them the ability to verify their email address. Uses Amazon SES to send the email.
  * `GET /verify` - the user is sent to this endpoint in the email, which will handle validating the verification token and marking the user as verified.
* New query parameter `email` for `PUT /users`.
  * This allows for updates to the email address. Put the existing email address as the query parameter, and the new email in the body of the Pilot user.
  * Corresponding updates to `ThunderClient` to include the new `email` parameter on the `updateUser()` method.
* Removed Guava and Commons-Codec dependencies, replaced with pure Java 8
* Much improved logging
* Better handling of AWS errors
* Minor code quality improvements


# v0.5.0
* Replaced the `username` PilotUser field with an `email` field
* Reordered the parameters on `getUser()`, `updateUser()`, and `deleteUser()` methods in `ThunderClient`
* Moved DynamoDB table name to configuration file `config.yaml`
* Upgraded AWS DynamoDB SDK to 1.11.91
  * Removed DynamoDB endpoint from configuration file and coded the region in `DynamoDbModule`
* Upgraded Checkstyle to 7.5.1
* Upgraded Dagger to 2.9
* Upgraded Dropwizard to 1.0.6
* Upgraded Guava to 21.0
  * Changed deprecated `Throwables.propagate(e)` to `throw new RuntimeException(e)`
* Upgraded Jackson to 2.7.8
* Improved unit testing
* Improved endpoint testing
* Updated documentation


# v0.4.1
* Fix bug where when DynamoDB was down, the response returned would be an Internal Server Error
* Improve Client unit tests


# v0.4.0
* Upgrade Dropwizard to 0.9.2
* Upgrade AWS DynamoDB SDK to 1.10.68
* Upgrade Guava to 19.0
* Introduce HeaderParam for user's password for improved security
* Corresponding updates to client to include the password in method calls
* Improved testing
* Better documentation


# v0.3.0
* Refactored StormUser to PilotUser in light of project change


# v0.2.0
* Added an ID field to objects in database
* Added authentication to all endpoints
* Added a healthcheck to determine if DynamoDB is available
* Included Dropwizard metrics to count endpoint requests


# v0.1.0
* Initial pre-release
