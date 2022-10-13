.. title:: Configuration Options

.. _configuration-all:

#####################
Configuration Options
#####################

Thunder is highly configurable to fit your needs. This page serves as an extensive guide to what configuration options are available.
If something that you wanted to configure is not available, open an issue to let us know!

.. _configuration-database:

Database
========

This configuration object is **REQUIRED**.
Use the ``type`` option within the ``database`` configuration in order to select the type of
database that you are using. The remaining configuration options will change depending on the value
of ``type``. See :ref:`configuration-database-dynamo`, :ref:`configuration-database-memory`,
and :ref:`configuration-database-mongo` below.

.. code-block:: yaml

    database:
      type: [dynamodb/memory/mongodb]


=================================== ==================================  =============================================================================
Name                                Default                             Description
=================================== ==================================  =============================================================================
type                                **REQUIRED**                        The database type to connect to. One of ``dynamodb``, ``memory``, or ``mongodb``.
=================================== ==================================  =============================================================================

.. _configuration-database-dynamo:

DynamoDB
--------

.. code-block:: yaml

    database:
      type: dynamodb
      endpoint:
      region:
      tableName:


=================================== ==================================  =============================================================================
Name                                Default                             Description
=================================== ==================================  =============================================================================
endpoint                            **REQUIRED**                        The endpoint used to access DynamoDB.
region                              **REQUIRED**                        The AWS region that the DynamoDB table exists in.
tableName                           **REQUIRED**                        The name of the DynamoDB table.
=================================== ==================================  =============================================================================

.. _configuration-database-memory:

In-Memory
--------

Please note that while ``memory`` is an option to enable the use of an in-memory database,
this configuration should **NOT** be used in production as data loss can easily occur.

.. code-block:: yaml

    database:
      type: memory
      maxMemoryPercentage:


=================================== ==================================  =============================================================================
Name                                Default                             Description
=================================== ==================================  =============================================================================
maxMemoryPercentage                 75                                  The maximum amount of JVM memory that can be in use. If the amount of used
                                                                        memory goes above this percentage, then ``POST`` requests to Thunder will
                                                                        begin to fail.
=================================== ==================================  =============================================================================

.. _configuration-database-mongo:

MongoDB
--------

.. code-block:: yaml

    database:
      type: mongodb
      connectionString:
      databaseName:
      collectionName:


=================================== ==================================  =============================================================================
Name                                Default                             Description
=================================== ==================================  =============================================================================
connectionString                    **REQUIRED**                        The connection string used to access MongoDB.
databaseName                        **REQUIRED**                        The name of the database within the MongoDB instance.
collectionName                      **REQUIRED**                        The name collection (table) within the database.
=================================== ==================================  =============================================================================

.. _configuration-email:

Email
=====

The email verification feature of Thunder allows you to ensure user email addresses actually belong to them.
By performing a ``POST`` on the ``/verify`` endpoint, an email will be sent to the address of the specified user.
The contents of this email can be customized through the :ref:`configuration-message-options` configuration.
If no custom contents are used, the default contents are included in the application and can be found
`on Github <https://github.com/RohanNagar/thunder/tree/master/application/src/main/resources>`_.

.. code-block:: yaml

    email:
      type: [none|ses]
      endpoint:
      region:
      fromAddress:
      messageOptions:
        subject:
        bodyHtmlFilePath:
        bodyTextFilePath:
        urlPlaceholderString:
        successHtmlFilePath:


=================================== ==================================  =============================================================================
Name                                Default                             Description
=================================== ==================================  =============================================================================
type                                none                                The type of email provider to use for verification. Currently, ``ses`` is the only available provider. Use ``none`` to disable email verification.
endpoint                            **REQUIRED IF ENABLED**             The endpoint used to access Amazon SES.
region                              **REQUIRED IF ENABLED**             The AWS region to use SES in.
fromAddress                         **REQUIRED IF ENABLED**             The address to send emails from.
messageOptions                      null                                See :ref:`configuration-message-options` below. If ``null``, default options are used.
=================================== ==================================  =============================================================================

.. _configuration-message-options:

Message Options
===============

.. code-block:: yaml

    messageOptions:
      subject:
      bodyHtmlFilePath:
      bodyTextFilePath:
      urlPlaceholderString:
      successHtmlFilePath:


=================================== ==================================  =============================================================================
Name                                Default                             Description
=================================== ==================================  =============================================================================
subject                             "Account Verification"              The subject line for the email to be sent.
bodyHtmlFilePath                    null                                The path to the HTML to include in the verification email body.
                                                                        If ``null``, then a default body is used.
bodyTextFilePath                    null                                The path to the text to include in the verification email body.
                                                                        If ``null``, then a default body is used.
urlPlaceholderString                CODEGEN-URL                         The string contained in the body files that should be replaced with a per-user account verification URL.
successHtml                         null                                The path to the HTML page to show users when they have successfully verified their email address.
                                                                        If ``null``, then a default page is shown.
=================================== ==================================  =============================================================================

.. _configuration-auth:

Authentication
==============

This is a required configuration block to define the authentication mechanism that clients will use
to make API calls to your Thunder instance. Both Basic Auth and OAuth 2.0 are supported types of
authentication. If this configuration section is not specified, then Thunder will not allow access
to any requests. You should specify at least one key that has access to the API (if using basic auth),
or set up OAuth.

.. code-block:: yaml

    auth:
      type: [basic|oauth]
      # Only use for basic auth
      keys:
        - application:
          secret:
        - application:
          secret:
      # Only use for OAuth
      hmacSecret:
      rsaPublicKeyFilePath:
      issuer:
      audience:


=================================== ==================================  =============================================================================
Name                                Default                             Description
=================================== ==================================  =============================================================================
type                                basic                               The type of authentication that Thunder should use. Either ``basic`` or ``oauth``.
keys                                EMPTY                               The list of approved keys for basic auth API access. Each key has two properties: ``application`` (the basic authentication
                                                                        username) and ``secret`` (the basic authentication password). Both properties on the key are required.
hmacSecret                          null                                The secret used to sign/verify JWT tokens signed with the HMAC family of algorithms. It is recommended
                                                                        to store this value in a secrets provider and reference it as described in :ref:`configuration-secrets`.
                                                                        Either this or ``rsaPublicKeyFilePath`` must be present.
rsaPublicKeyFilePath                null                                The path to a file containing the RSA public key used to verify JWT tokens signed with the RSA
                                                                        family of algorithms. The file must be in ``.der`` format, which can be generated with openssl:
                                                                        ``openssl rsa -in private_key.pem -pubout -outform DER -out public_key.der``. Either this or
                                                                        ``hmacSecret`` must be present.
issuer                              REQUIRED for oauth                  The issuer of JWT tokens. Will be used in JWT token validation.
audience                            none                                The audience to use when validation JWT tokens. If left empty, no audience will
                                                                        be required on JWT tokens.
=================================== ==================================  =============================================================================

.. _configuration-secrets:

Configuration Secrets
=====================

This configuration object is **OPTIONAL**.

If you want to keep specific configuration values in your configuration file a secret, you can
have Thunder read values of keys from a supported secrets provider. To have Thunder read a secret,
use the ``${...}`` notation, where ``...`` is the name of the secret stored in your secrets provider.

To configure your secrets provider, use the following configuration:

.. code-block:: yaml

    secrets:
      provider: [env|secretsmanager]

=================================== ==================================  =============================================================================
Name                                Default                             Description
=================================== ==================================  =============================================================================
provider                            env                                 The provider that is storing your secrets.
                                                                        Use ``env`` to read secrets from local environment variables.
                                                                        Use ``secretsmanager`` to read secrets from AWS Secrets Manager. See :ref:`configuration-secrets-secretsmanager` below.
=================================== ==================================  =============================================================================

.. _configuration-secrets-secretsmanager:

AWS Secrets Manager
-------------------

.. code-block:: yaml

    secrets:
      provider: secretsmanager
      endpoint:
      region:
      retryDelaySeconds:
      maxRetries:


=================================== ==================================  =============================================================================
Name                                Default                             Description
=================================== ==================================  =============================================================================
endpoint                            **REQUIRED**                        The endpoint used to access Secrets Manager.
region                              **REQUIRED**                        The AWS region that the Secrets Manager endpoint is in.
retryDelaySeconds                   1                                   The amount of time to wait between retries if there is an error connecting to
                                                                        Secrets Manager.
maxRetries                          0                                   The maximum amount of times to retry looking up a secret from Secrets Manager
                                                                        if there is an error connecting to Secrets Manager.
=================================== ==================================  =============================================================================

.. _configuration-hash:

User Password Hashing
=====================

This configuration object is **OPTIONAL**.

This group of options allows you to configure the hashing algorithm used by Thunder for server-side hashing of
user passwords, as well as the algorithm used to check the password value in the request header.

.. code-block:: yaml

    passwordHash:
      algorithm:
      serverSideHash:
      headerCheck:
      allowCommonMistakes:


=================================== ==================================  =============================================================================
Name                                Default                             Description
=================================== ==================================  =============================================================================
algorithm                           simple                              The algorithm to use for server side hashing and password comparison.
                                                                        Supported values are: ``simple``, ``sha256``, ``bcrypt``, and ``argon``.
serverSideHash                      false                               Whether or not to enable server side hashing. When enabled, a new user or
                                                                        updated password will be hashed within Thunder before being stored in the database.
headerCheck                         true                                Whether or not to enable password header checks. When enabled, the ``password`` header
                                                                        is required on ``GET``, ``PUT``, ``DELETE`` calls to ``/users``, ``POST`` calls to ``/verify``,
                                                                        and ``POST`` calls to ``/verify/reset``. When disabled, this header is not required.
allowCommonMistakes                 false                               Whether or not to allow the user to have common password mistakes. When enabled, if the user
                                                                        provides a password with any of the following common mistakes, the password will still be
                                                                        accepted as valid:
                                                                            1. The user inserted a random character before or after
                                                                            2. The user accidentally capitalized (or did not capitalize) the first letter
                                                                            3. The user mistakenly used caps lock
=================================== ==================================  =============================================================================

.. _configuration-properties:

Property Validation
===================

This configuration object is **OPTIONAL**.

This configuration contains a list of additional user properties to be validated on ``POST`` or ``PUT`` calls to ``/users``.
The default is no validation if ``properties`` is not defined.

For each property, new and updated users will be validated to ensure their ``properties`` map includes a property with that name and type.

Additionally, there are two options to change the behavior of property validation, ``allowSubset`` and ``allowSuperset``.

``allowSubset`` allows a user's properties to be a subset of the defined ``allowed`` properties.

``allowSuperset`` allows a user's properties to be a superset of the defined ``allowed`` properties.

This leads to 4 scenarios:

1. Both true. Users can have extra fields than those specified, or less than those specified,
but the ones that are present and specified will be checked to make sure they are the correct type.

2. ``allowSuperset`` true and ``allowSubset`` false. Users can have extra fields than those specified,
but no less than those specified.

3. ``allowSuperset`` false and ``allowSubset`` true. Users can not have extra fields, but they can have less.
All properties must be in the list of specified properties.

4. Both false. Users can not have extra fields or less than those specified.
All specified fields must exist and be correct, and no more.

.. code-block:: yaml

    properties:
      allowSubset:
      allowSuperset:
      allowed:
        - name:
          type:
        - name:
          type:


=================================== ==================================  =============================================================================
Name                                Default                             Description
=================================== ==================================  =============================================================================
allowSubset                         true                                Allows a user's properties to be a subset of the defined ``allowed`` properties.
allowSuperset                       true                                Allows a user's properties to be a superset of the defined ``allowed`` properties.
allowed                             Empty list                          The list of additional user properties to validate on ``POST`` or ``PUT`` requests.
name                                **REQUIRED PER ALLOWED RULE**       The name of the property.
type                                **REQUIRED PER ALLOWED RULE**       The type of the property. Supported types are: ``string``, ``integer``, ``double``, ``boolean``, ``list``, and ``map``.
                                                                        Any other type defined is treated as ``Object``, meaning any object type will be allowed.
                                                                        Use ``object`` if you don't want to enforce a specific type for this property.
=================================== ==================================  =============================================================================

.. _configuration-email-address-validation:

Email Address Validation
========================

This configuration object is **OPTIONAL**.

By default, Thunder validates email addresses of new users with basic email validation. However,
you can add additional custom rules that are used as part of validation.

.. code-block:: yaml

    rules:
      - check: [startswith/endswith/contains/doesnotcontain]
        value:
      - check: [startswith/endswith/contains/doesnotcontain]
        value:

=================================== ==================================  =============================================================================
Name                                Default                             Description
=================================== ==================================  =============================================================================
rules                               none                                A list of rules to use when validating an email address. Each rule has two properties:
                                                                        ``check`` and ``value``. For each rule, both properties are required. The types of checks
                                                                        available are currently ``startswith``, ``endswith``, ``contains``, and ``doesnotcontain``.
                                                                        The value should be the value you want to check against. For example, if you want to make sure
                                                                        that email addresses end with a specific domain ``test.com``, you would use ``endswith`` as
                                                                        the ``check`` and ``test.com`` as the value.
=================================== ==================================  =============================================================================

.. _configuration-options:

Operation Options
=================

This configuration object is **OPTIONAL**.

This contains configuration options for individual requests made to Thunder.

.. code-block:: yaml

    options:
      operationTimeout:

=================================== ==================================  =============================================================================
Name                                Default                             Description
=================================== ==================================  =============================================================================
operationTimeout                    30s                                 Set the timeout for each Thunder operation.
=================================== ==================================  =============================================================================

.. _configuration-openapi:

OpenAPI
=======

This configuration object is **OPTIONAL**.

This contains configuration options for the OpenAPI and Swagger UI. Swagger UI is enabled by default,
however you can disable it through the ``enabled`` option. There are also additional options related
to the metadata of the generated OpenAPI.

.. code-block:: yaml

    openApi:
      enabled:
      title:
      version:
      description:
      contact:
      contactEmail:
      license:
      licenseUrl:


=================================== ==================================  =============================================================================
Name                                Default                             Description
=================================== ==================================  =============================================================================
enabled                             true                                Whether or not to enable OpenAPI generation and Swagger UI.
title                               Thunder API                         The title of the Swagger page.
version                             *Current version*                   The version of the application.
description                         A fully customizable user           The description of the application.
                                    management REST API
contact                             null                                The name of the contact person for the application.
contactEmail                        null                                The email of the contact person for the application.
license                             MIT                                 The name of the license for the application.
licenseUrl                          https://github.com/RohanNagar/      The URL of the license for the application.
                                    thunder/blob/master/LICENSE.md
=================================== ==================================  =============================================================================

.. _configuration-dropwizard:

Dropwizard Configuration
========================

In addition to the configuration options above, Dropwizard provides certain configuration options.
Those can be seen `here <http://www.dropwizard.io/1.3.1/docs/manual/configuration.html>`_.
