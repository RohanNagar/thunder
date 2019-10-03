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

.. code-block:: yaml

    database:
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

.. _configuration-email:

Email
=====

This configuration object is **REQUIRED**.
The email verification feature of Thunder allows you to ensure user email addresses actually belong to them.
By performing a ``POST`` on the ``/verify`` endpoint, an email will be sent to the address of the specified user.
The contents of this email can be customized through the :ref:`configuration-message-options` configuration.
If no custom contents are used, the default contents are included in the application and can be found
`on Github <https://github.com/RohanNagar/thunder/tree/master/application/src/main/resources>`_.

.. code-block:: yaml

    email:
      enabled: true
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
enabled                             true                                Whether or not to enable the email verification endpoints (``/verify``)
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

Basic Authentication Keys
=========================

This configuration object is **REQUIRED**.

This is a list of approved keys that can access resource methods on ``/users`` and ``POST /verify``. At least one key is **REQUIRED**.

.. code-block:: yaml

    approvedKeys:
      - application:
        secret:
      - application:
        secret:


=================================== ==================================  =============================================================================
Name                                Default                             Description
=================================== ==================================  =============================================================================
application                         **REQUIRED**                        The name of the approved application (basic authentication username).
secret                              **REQUIRED**                        The secret of the approved application (basic authentication password).
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


=================================== ==================================  =============================================================================
Name                                Default                             Description
=================================== ==================================  =============================================================================
algorithm                           simple                              The algorithm to use for server side hashing and password comparison.
                                                                        Supported values are: ``simple``, ``md5``, and ``bcrypt``.
serverSideHash                      false                               Whether or not to enable server side hashing. When enabled, a new user or
                                                                        updated password will be hashed within Thunder before being stored in the database.
headerCheck                         true                                Whether or not to enable password header checks. When enabled, the ``password`` header
                                                                        is required on ``GET``, ``PUT``, ``DELETE`` calls to ``/users``, ``POST`` calls to ``/verify``,
                                                                        and ``POST`` calls to ``/verify/reset``. When disabled, this header is not required.
=================================== ==================================  =============================================================================

.. _configuration-properties:

Property Validation
===================

This configuration object is **OPTIONAL**.

This is a list of additional user properties to be validated on ``POST`` or ``PUT`` calls to ``/users``.
The default is no validation if ``properties`` is not defined.

For each property, new and updated users will be validated to ensure their ``properties`` map includes a property with that name and type.
Also, it will ensure no additional properties are defined.

.. code-block:: yaml

    properties:
      - name:
        type:
      - name:
        type:


=================================== ==================================  =============================================================================
Name                                Default                             Description
=================================== ==================================  =============================================================================
name                                **REQUIRED**                        The name of the property.
type                                **REQUIRED**                        The type of the property. Supported types are: ``string``, ``integer``, ``double``, ``boolean``, ``list``, and ``map``.
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