<h1 align="center">
  <br>
  <img src="docs/logo/logo-v1-text.png" alt="thunder"height="200px"></p>
  <br>
</h1>

<h4 align="center">A fully customizable user management REST API.</h4>

<p align="center">
  <a href="https://github.com/RohanNagar/thunder/actions?query=workflow%3ACI+branch%3Amaster">
    <img src="https://github.com/rohannagar/thunder/workflows/CI/badge.svg?branch=master" alt="Build Status">
  </a>
  <a href="https://codecov.io/gh/RohanNagar/thunder">
    <img src="https://codecov.io/gh/RohanNagar/thunder/branch/master/graph/badge.svg" alt="Coverage Status">
  </a>
  <a href="https://search.maven.org/artifact/com.sanctionco.thunder/client">
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
</p>

<p align="center">
  <a href="https://thunder-api.readthedocs.io/en/latest/index.html">Read the Documentation</a>
</p>

<p align="center">
  <a href="#features">Features</a> •
  <a href="#running-locally">Running Locally</a> •
  <a href="#running-on-kubernetes">Running on Kubernetes</a> •
  <a href="https://thunder-api.readthedocs.io/en/latest/manual/client-libraries.html">Client Libraries</a> •
  <a href="https://github.com/RohanNagar/thunder/wiki/Changelog">Changelog</a>
</p>

## Features

- Connects to AWS DynamoDB or MongoDB
- REST API for CRUD (Create/Retrieve/Update/Delete) operations
- Built-in email verification with AWS Simple Email Service
- Server-side password hashing
- Support for both Basic Auth and OAuth 2.0
- Customizable user properties with validation
- Customizable email message contents
- Generated OpenAPI (Swagger) specification
- Metrics and healthchecks
- Official Docker Image and Helm Chart
  - [Docker Hub](https://hub.docker.com/r/rohannagar/thunder/)
  - [GitHub Container Registry](https://github.com/users/RohanNagar/packages/container/package/thunder)
- Multiple native [client libraries](https://thunder-api.readthedocs.io/en/latest/manual/client-libraries.html)

## Running Locally
Fork this repo on GitHub. Then, clone your forked repo onto your machine
and navigate to the created directory.

```bash
$ git clone YOUR-FORK-URL
$ cd thunder
```

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

Thunder is deployed through a Helm chart. See the `scripts/deploy/helm/thunder` directory for steps
on deploying through Helm.

## Further Documentation

Full documentation can be found on [ReadTheDocs](https://thunder-api.readthedocs.io/en/latest/).
For Thunder development documentation, refer to the [wiki](https://github.com/RohanNagar/thunder/wiki)
for information on how to build and write tests.
