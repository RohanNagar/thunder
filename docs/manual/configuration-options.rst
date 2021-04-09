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
of ``type``. See :ref:`configuration-database-dynamo` and :ref:`configuration-database-mongo` below.

.. code-block:: yaml

    database:
      type: [dynamodb|mongodb]


=================================== ==================================  =============================================================================
Name                                Default                             Description
=================================== ==================================  =============================================================================
type                                **REQUIRED**                        The database type to connect to. Either ``dynamodb`` or ``mongodb``.
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

.. _configuration-auth-keys:

Authentication
==============

This is a list of approved keys that can access resource methods on ``/users`` and ``POST /verify``. If this configuration
section is not specified, then Thunder will not allow access to any requests. You should specify at least one key that
has access to the API.

.. code-block:: yaml

    auth:
      type: basic
      keys:
        - application:
          secret:
        - application:
          secret:


=================================== ==================================  =============================================================================
Name                                Default                             Description
=================================== ==================================  =============================================================================
type                                basic                               The type of authentication that Thunder should use. Currently, only ``basic`` is supported.
keys                                EMPTY                               The list of approved keys for API access. Each key has two properties: ``application`` (the basic authentication
                                                                        username) and ``secret`` (the basic authentication password). Both properties on the key are required.
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
      provider: [env]

=================================== ==================================  =============================================================================
Name                                Default                             Description
=================================== ==================================  =============================================================================
provider                            env                                 The provider that is storing your secrets. Currently, only ``env`` is supported.
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
                                                                        Supported values are: ``simple``, ``sha256``, and ``bcrypt``.
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
but the ones that are presents and specified will be checked to make sure they are the correct type.

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