<h1 align="center">
  <br>
  <img src="docs/logo/logo-v1-text.png" alt="thunder"height="200px"></p>
  <br>
</h1>

<h4 align="center">A fully customizable user management REST API.</h4>

<p align="center">
  <a href="https://travis-ci.org/RohanNagar/thunder">
    <img src="https://travis-ci.org/RohanNagar/thunder.svg?branch=master" alt="Build Status">
  </a>
  <a href="https://codecov.io/gh/RohanNagar/thunder">
    <img src="https://codecov.io/gh/RohanNagar/thunder/branch/master/graph/badge.svg" alt="Coverage Status">
  </a>
  <a href="https://search.maven.org/artifact/com.sanctionco.thunder/client/2.0.0/jar">
    <img src="https://img.shields.io/maven-central/v/com.sanctionco.thunder/client.svg?colorB=brightgreen&label=maven%20central" alt="Maven Central">
  </a>
  <a href="http://javadoc.io/doc/com.sanctionco.thunder/client">
    <img src="http://javadoc.io/badge/com.sanctionco.thunder/client.svg" alt="Javadoc">
  </a>
  <a href="https://hub.docker.com/r/rohannagar/thunder">
    <img src="https://img.shields.io/docker/pulls/rohannagar/thunder.svg" alt="Docker Pulls">
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
  <a href="#running-locally">Running Locally</a> •
  <a href="#running-on-kubernetes">Running on Kubernetes</a> •
  <a href="https://github.com/RohanNagar/thunder/wiki/Using-the-Java-Client">Client Library</a> •
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
- [Java](https://github.com/RohanNagar/thunder/wiki/Using-the-Java-Client)
and [JavaScript](https://github.com/RohanNagar/thunder-client-js) client libraries

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

3. Connect to your K8s cluster using `kubectl`.

4. Deploy the Thunder ConfigMap to your K8s cluster:

```bash
$ kubectl apply -f scripts/kubernetes/thunder-config.yaml
```

5. Deploy the Thunder deployment to your K8s cluster:

```bash
$ kubectl apply -f scripts/kubernetes/thunder-deployment.yaml
```

6. Run `kubectl get pods` to see that two instances of Thunder have started up and are running!

## Open Source Libraries
Thank you to the open source projects used in this project. Thunder would not be possible without them.

## Further Documentation
Further documentation can be found on our [wiki](https://github.com/RohanNagar/thunder/wiki).
Refer to the wiki while developing before opening an issue or pull request.
