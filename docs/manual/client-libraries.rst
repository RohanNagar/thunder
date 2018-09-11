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
  2. Your application key.
  3. Your application secret.

.. code-block:: java

    ThunderClient thunderClient = new ThunderBuilder("ENDPOINT", "USER-KEY", "USER_SECRET")
      .newThunderClient();

.. note::

    The endpoint **must** end in a slash '/'.

Any of the methods in ``ThunderClient`` are now available for use. For example, to get a user:

.. code-block:: java

    User user = thunderClient
      .getUser("EMAIL", "PASSWORD")
      .execute()
      .body();

JavaScript (Node.js)
====================

The official JavaScript Thunder client library is available on NPM.
See the `repository <https://github.com/RohanNagar/thunder-client-js>`_ for usage instructions.