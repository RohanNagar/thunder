<h1 align="center">
  <br>
  <img src="application/src/main/resources/vertical.png" alt="thunder"height="150px"></p>
  <br>
</h1>

<h4 align="center">A fully customizable user management REST API.</h4>

<p align="center">
  <a href="https://travis-ci.org/RohanNagar/thunder">
    <img src="https://travis-ci.org/RohanNagar/thunder.svg?branch=master" alt="Build Status">
  </a>
  <a href="https://jitpack.io/#RohanNagar/thunder">
    <img src="https://jitpack.io/v/RohanNagar/thunder.svg" alt="Release">
  </a>
  <a href="https://hub.docker.com/r/rohannagar/thunder">
    <img src="https://img.shields.io/docker/pulls/rohannagar/thunder.svg" alt="Docker Pulls">
  </a>
  <a href="https://coveralls.io/github/RohanNagar/thunder?branch=master">
    <img src="https://coveralls.io/repos/github/RohanNagar/thunder/badge.svg?branch=master" alt="Coverage Status">
  </a>
  <a href="https://www.codetriage.com/rohannagar/thunder">
    <img src="https://www.codetriage.com/rohannagar/thunder/badges/users.svg" alt="Open Source Helpers">
  </a>
  <a href="https://github.com/RohanNagar/thunder/blob/master/LICENSE.md">
    <img src="https://img.shields.io/badge/license-MIT-FF7178.svg" alt="License">
  </a>
</p>

<p align="center">
  <a href="#features">Features</a> •
  <a href="#client-library-usage">Client Library</a> •
  <a href="#running-locally">Running Locally</a> •
  <a href="#running-on-kubernetes">Running on Kubernetes</a> •
  <a href="https://github.com/RohanNagar/thunder/wiki/Changelog">Changelog</a> •
  <a href="https://github.com/RohanNagar/thunder/wiki">Further Documentation</a>
</p>

## Features

- Connects to AWS DynamoDB
- REST API for CRUD (Create/Retrieve/Update/Delete) operations
- Built-in email verification
- Customizable email message contents
- Customizable verification success page
- Customizable user properties
- Property validation on create/update
- [Official Docker Image](https://hub.docker.com/r/rohannagar/thunder/)
- [Java](#client-library-usage) and [JavaScript](https://github.com/RohanNagar/thunder-client-js) client libraries

## Client Library Usage

Thunder is available through [JitPack](https://jitpack.io/#RohanNagar/thunder).
This means you can include the client whether your project is Maven, Gradle, sbt, or Leiningen.
See the [wiki](https://github.com/RohanNagar/thunder/wiki/Using-the-Java-Client) for more detailed information.

Include the latest version of the client module as a dependency. For example, with Maven:

```xml
<dependency>
  <groupId>com.github.RohanNagar.thunder</groupId>
  <artifactId>client</artifactId>
  <version>${thunder.version}</version>
</dependency>
```

Create a new `ThunderClient` instance with
  1. The endpoint to access Thunder over HTTP.
  2. Your application key.
  3. Your application secret.

> Note: The endpoint **must** end in a slash '/'.

```java
ThunderClient thunderClient = new ThunderBuilder("ENDPOINT", "USER-KEY", "USER_SECRET")
                                .newThunderClient();
```

Any of the methods in `ThunderClient` are now available for use. For example, to get a user:

```java
PilotUser user = thunderClient.getUser("EMAIL", "PASSWORD")
  .execute()
  .body();
```

## Running Locally
Fork this repo on GitHub. Then, clone your forked repo onto your machine
and navigate to the created directory.

```bash
$ git clone YOUR-FORK-URL
$ cd thunder
```

Run the `bootstrap.sh` script to make sure your machine has all the necessary dependencies
and to install code dependencies.

```bash
$ ./scripts/tools/bootstrap.sh
```

> Note: The script will install Java 8, Maven, Node.js, and NPM for you.
>
> For those on Linux, the script will use `apt-get` to install the packages.
>
> For those on macOS, the script will use `brew` to install the packages.
>
> If you run into issues with the bootstrap script, please
> [let us know](https://github.com/RohanNagar/thunder/issues/new?template=bug_report.md)!

Compile and package the source code with Maven.

```bash
$ mvn package
```

Start up local dependencies (DynamoDB and SES) in the background so that Thunder can perform all functionality.

```bash
$ node scripts/tools/run-local-dependencies.js &
```

Run the packaged jar.

```bash
$ java -jar application/target/application-*.jar server config/local-dev-config.yaml
```

Thunder should now be running on localhost port 8080!

## Running on Kubernetes

The official Thunder Docker image is published on [Docker Hub](https://hub.docker.com/r/rohannagar/thunder/).

1. Modify the `scripts/kubernetes/thunder-deployment.yaml` file to use the desired image version.
The default image is `rohannagar/thunder:edge`. Also set the correct values for your AWS access keys.

```yaml
...
containers:
  - name: thunder
    image: rohannagar/thunder:edge # Replace this if desired
    imagePullPolicy: Always
    env:
      - name: AWS_ACCESS_KEY_ID
        value: # Set value here
      - name: AWS_SECRET_ACCESS_KEY
        value: # Set value here
...
```

2. Modify the `scripts/kubernetes/thunder-config.yaml` file by replacing the `config.yaml` section with
the configuration you desire.

3. Connect to your K8s cluster, running anywhere.

4. Deploy the Thunder ConfigMap to your K8s cluster:

```bash
$ kubectl apply -f scripts/kubernetes/thunder-config.yaml
```

5. Deploy the Thunder deployment to your K8s cluster:

```bash
$ kubectl apply -f scripts/kubernetes/thunder-deployment.yaml
```

6. Run `kubectl get pods` to see that two instances of Thunder have started up and are running!

## Further Documentation
Further documentation can be found on our [wiki](https://github.com/RohanNagar/thunder/wiki).
Refer to the wiki while developing before opening an issue or pull request.
