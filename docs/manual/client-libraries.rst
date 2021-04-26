.. title:: Client Libraries

.. _client-libraries:

################
Client Libraries
################

There are multiple client libraries available for you to use in your end-user applications
after you have Thunder running.

Java
====

The Thunder Java client is available on `Maven Central <https://search.maven.org/search?q=g:%22com.sanctionco.thunder%22%20AND%20a:%22client%22>`_.
To add the client to your Gradle, Maven, sbt, or Leiningen project, follow the instructions given at that link.
For Maven, you can also read the following instructions.

Maven
-----

Add the Thunder ``client`` artifact. The ``client`` artifact includes the ``api`` artifact, so there is no need to add both.

.. code-block:: xml

    <dependency>
      <groupId>com.sanctionco.thunder</groupId>
      <artifactId>client</artifactId>
      <version>${thunder.version}</version>
    </dependency>

To determine the latest version available, check out the
`README <https://github.com/RohanNagar/thunder/blob/master/README.md>`_, the
`GitHub releases page <https://github.com/RohanNagar/thunder/releases>`_, or the
`Maven Central Search <https://search.maven.org/search?q=g:%22com.sanctionco.thunder%22%20AND%20a:%22client%22>`_.

Usage
-----

Create a new ``ThunderClient`` instance with
  1. The endpoint to access Thunder over HTTP.
  2. Your application key/secret if using basic auth
     OR your access token if using OAuth.

.. code-block:: java

    ThunderClient thunderClient = ThunderClient.builder()
      .endpoint("http://your.endpoint.com")
      .authentication("USER-KEY", "USER_SECRET") // Basic auth
      .authentication("ACCESS-TOKEN") // OAuth 2.0 access token
      .build();

Any of the methods in ``ThunderClient`` are now available for use. For example, to get a user:

.. code-block:: java

    User user = thunderClient
      .getUser("EMAIL", "PASSWORD")
      .get();

All of the ``ThunderClient`` methods return a ``CompletableFuture`` that will allow you to only block
on the response until you want to.

JavaScript (Node.js)
====================

The official JavaScript Thunder client library is available on NPM.
See the `repository <https://github.com/RohanNagar/thunder-client-js>`_ for usage instructions.