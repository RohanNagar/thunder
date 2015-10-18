# Thunder
![Version](https://img.shields.io/badge/version-v0.2.0-7f8c8d.svg)
[![Twitter](https://img.shields.io/badge/twitter-%40RohanNagar22-00aced.svg)](http://twitter.com/RohanNagar22)

Thunder is a REST API that interfaces with a DynamoDB database. Thunder is part of the backend for Social Storm, the aggregate social media platform.

* [Endpoints](#endpoints)
* [Running Locally](#running-locally)
* [Contributing](#contributing)
* [Testing](#testing)

## Endpoints
- `POST` `/users`
  
  Must post with a JSON body that defines a StormUser. The body should look similar to the following.

  ```json
  [
    {
      "username" : "Testy",
      "password" : "12345",
      "facebookAccessToken" : "facebookAccessToken",
      "twitterAccessToken" : "twitterAccessToken",
      "twitterAccessSecret" : "twitterAccessSecret"
    }
  ]
  ```
  
- `PUT` `/users`

  The PUT endpoint is for updating users. The body of the request must be JSON that defines the StormUser that is being updated. All fields must be present in the JSON, or they will be overridden in the database as `null`. Additionally, the username of the user must be the same in order for the PUT to be successful.
  
- `GET` `/users?username=Testy`
  
  The GET request must set the username query parameter. The response will contain the StormUser JSON object.

- `DELETE` `/users?username=Testy`

  The DELETE request must set the username query parameter. The user will be deleted in the database, and the response will contain the StormUser object that was just deleted.

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

Edit the config.yaml file to set the `dynamo-endpoint` variable to the endpoint you are running DynamoDB on locally. This is usually `localhost:8000`.

You may have to create a new table in your DynamoDB local instance. You can do this by going to `localhost:8000/shell` and using a Javascript script to create a table. Be sure that the table name is either the same as the one in `StormUsersDao.java`, or you change the name in `StormUsersDao.java` to be the name of the table that you created.

Compile and package the source code with Maven.

```bash
$ mvn package
```

Run the packaged jar.

```bash
$ java -jar application/target/application-*.jar server config.yaml
```

Thunder should now be running on localhost port 8080.

## Contributing
Make changes to your local repository and push them up to your fork on GitHub.
Submit a pull request to this repo with your changes as a single commit.
Your changes will be reviewed and merged when appropriate.

## Testing
You can run the following commands using [HTTPie](https://github.com/jkbrzt/httpie) to test each of the available endpoints. Simply replace the brackets with the appropriate information and run the command via the command line.

- `http -a {application}:{secret} GET localhost:8080/users?username={name}`
- `http -a {application}:{secret} POST localhost:8080/users < {filename}`
- `http -a {application}:{secret} PUT localhost:8080/users < {filename}`
- `http -a {application}:{secret} DELETE localhost:8080/users?username={name}`
