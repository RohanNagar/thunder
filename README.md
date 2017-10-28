<img align="left" src="application/src/main/resources/logo.png">

# Thunder
[![Build Status](https://travis-ci.org/RohanNagar/thunder.svg?branch=master)](https://travis-ci.org/RohanNagar/thunder)
[![Coverage Status](https://coveralls.io/repos/github/RohanNagar/thunder/badge.svg?branch=master&maxAge=3600)](https://coveralls.io/github/RohanNagar/thunder?branch=master)
[![Version](https://img.shields.io/github/tag/RohanNagar/thunder.svg?label=version&colorB=7f8c8d)](https://github.com/RohanNagar/thunder/releases)
[![License](https://img.shields.io/badge/license-MIT-FF7178.svg)](https://github.com/RohanNagar/thunder/blob/master/LICENSE.md)
[![Twitter](https://img.shields.io/badge/twitter-%40RohanNagar22-00aced.svg)](http://twitter.com/RohanNagar22)

Thunder is a REST API that interfaces with a DynamoDB database.
Thunder is part of the backend for [Pilot](https://github.com/RohanNagar/pilot-osx), the social media publishing application.

* [Endpoints](#endpoints)
* [Client Library Usage](#client-library-usage)
* [Running Locally](#running-locally)
* [Testing](#testing)
* [Modifying for Personal Use](#modifying-for-personal-use)
* [Changelog](https://github.com/RohanNagar/thunder/wiki/Changelog)
* [Further Documentation](#further-documentation)

## Endpoints
- `POST` `/users`
  
  The POST endpoint is for adding a new user to the database.
  Must post with a JSON body that defines a PilotUser.
  The body should look similar to the following.

  ```json
  {
    "email" : {
      "address": "sampleuser@sanctionco.com"
    },
    "password" : "12345",
    "facebookAccessToken" : "facebookAccessToken",
    "twitterAccessToken" : "twitterAccessToken",
    "twitterAccessSecret" : "twitterAccessSecret"
  }
  ```
  
- `PUT` `/users`

  The PUT endpoint is for updating a specific user.
  The body of the request must be JSON that defines the PilotUser that is being updated.
  All fields must be present in the JSON, or they will be overridden in the database as `null`.
  Additionally, the email of the user must be the same in order for the PUT to be successful.
  
- `GET` `/users?email=sampleuser@sanctionco.com`
  
  The GET request must set the email query parameter. Additionally, the password of the user must be
  included as a header parameter for security reasons.
  The response will contain the PilotUser JSON object.

- `DELETE` `/users?email=sampleuser@sanctionco.com`

  The DELETE request must set the email query parameter. Additionally, the password of the user must
  be included as a header parameter for security reasons.
  The user will be deleted in the database,
  and the response will contain the PilotUser object that was just deleted.

- `GET` `/verify?email=sampleuser@sanctionco.com&token=12345`

  The verify endpoint is used to verify a user email. The endpoint is called with an email address
  and a verification token that is sent to the user via email when the account is created.
  Upon verification, the user object in the database will be updated to indicate that the email address
  is verified.

## Client Library Usage

Include the latest version of the client module as a Maven dependency in your `pom.xml`.

```xml
<dependency>
  <groupId>com.sanction.thunder</groupId>
  <artifactId>client</artifactId>
  <version>${thunder.version}</version>
</dependency>
```

Create a new `ThunderClient` instance with
  1. The endpoint to access Thunder over HTTP.
  2. Your application key.
  3. Your application secret.

```java
ThunderClient thunderClient = new ThunderBuilder("ENDPOINT", "USER-KEY", "USER_SECRET")
                                .newThunderClient();
```

Any of the methods in `ThunderClient` are now available for use. For example, to get a user:

```java
PilotUser user = thunderClient.getUser("EMAIL", "PASSWORD");
```

## Running Locally
- Requirements
  - Java 1.8
  - Maven 3.3.3
  - DynamoDB Local

Be sure to follow the [instructions](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Tools.DynamoDBLocal.html) on setting up and running DynamoDB locally from Amazon.

Fork this repo on GitHub. Then, clone your forked repo onto your machine.

```bash
$ git clone YOUR-FORK-URL
```

Navigate to the directory that you just created.

```bash
$ cd thunder
```

Edit `config.yaml` in the root directory to set the `dynamo-endpoint` variable to the endpoint you are running DynamoDB on locally. This is usually `localhost:8000`.

You may have to create a new table in your DynamoDB local instance.
You can do this by going to `localhost:8000/shell` and using a Javascript script to create a table.
Be sure that the table name is either the same as the one in the config file (as `dynamo-table-name`), or you change the name to be the name of the table that you created.

Compile and package the source code with Maven.

```bash
$ mvn package
```

Run the packaged jar.

```bash
$ java -jar application/target/application-*.jar server config.yaml
```

Thunder should now be running on localhost port 8080.

## Testing
There is a Python testing script available in the `scripts` directory.
To run this script, make sure you are in the base thunder directory and run the following command.

```bash
$ python scripts/tester.py
```

There are multiple optional command line arguments for the testing script. These are described in the table below, along with their default values. Additionally, when running the script from the command line, adding the `-h` option will display a help message with all optional arguments.

|        Flag        |                                                  Description                                                  |      Default Value      |
|:------------------:|:-------------------------------------------------------------------------------------------------------------:|:-----------------------:|
|    `-h` `--help`   |                                            Display a help message                                            |           ----          |
|  `-f` `--filename` |                                       The JSON file containing user details to test                           |   `user_details.json`   |
|  `-e` `--endpoint` |                                   The endpoint to connect to lightning with                                  | `http://localhost:8080` |
|    `-a` `--auth`   |                   The basic authentication credentials in the form `{app_name}:{app_secret}`                  |   `application:secret`  |
| `-v` `--verbose`   |                          Provides more output information when this flag is supplied                          |          `False`        |

Additionally, you can run the following commands using [HTTPie](https://github.com/jkbrzt/httpie) to test each of the available endpoints.
Simply replace the brackets with the appropriate information and run the command via the command line.

- `http -a {application}:{secret} GET localhost:8080/users?email={email} password:{password}`
- `http -a {application}:{secret} POST localhost:8080/users < {filename}`
- `http -a {application}:{secret} PUT localhost:8080/users < {filename} password:{password}`
- `http -a {application}:{secret} DELETE localhost:8080/users?email={email} password:{password}`

## Modifying for Personal Use
If you would like to create your own user management REST API based on this project, start by forking this repository.

After cloning the fork to your computer, you can take the following steps to make this project conform to your own:

1. Modify the `PilotUser` class.

This is the class that represents a user. Modify the name of the class in order to represent your own user.
For example, if your application is called `Thunder`, considering changing the name of the class to `ThunderUser`.
Then, modify the attributes of the class to include what is neccessary to represent a user in your application.

2. Modify the `PilotUsersDao` class.

If you are using DynamoDB for your implementation, this step may not be necessary other than refactoring based on
the keys you want to use to look up by. For example, if you want to get users based on username instead of email, you
will change the `insert()` method to insert `.withPrimaryKey("username", user.getUsername())`.

If you are using another database type, then you will want to completely rewrite this class. Keep all method names
the same, but modify the implementation to insert/search/delete from your database. Additionally, you will want to modify
the instantiation of this class in the `DaoModule` class to match your new constructor, and you will want to replace the `dynamodb`
package with your own package that includes a Dagger Module and a HealthCheck.

3. Open an issue for any further questions.

If you have questions about modifying this project to fit your own needs, feel free to open an issue on Github and we will
do our best to help you incorporate this project into your backend.

## Further Documentation
Further documentation can be found on our [wiki](https://github.com/RohanNagar/thunder/wiki).
Refer to the wiki while developing before opening an issue or pull request.

### Quick Links
* [Testing Overview](https://github.com/RohanNagar/thunder/wiki/Testing-Overview)
* [User Attributes](https://github.com/RohanNagar/thunder/wiki/User-Attributes)
