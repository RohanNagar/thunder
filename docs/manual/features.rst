.. title:: Features

.. _features:

########
Features
########

REST API for user object operations
===================================

At its core, Thunder is a REST API that provides endpoints to manage user accounts and information.
Your frontend application can use Thunder to create, retrieve, update, and delete user accounts.
All of the user information is stored in a database that Thunder interfaces with.

.. note::

    Currently, Thunder only supports DynamoDB databases, so an AWS account is required to run
    Thunder.

Email Verification
==================

Thunder provides functionality to send verification emails and keep email verification state.
``POST`` requests to ``/verify`` will send a verification email with a verification URL. ``GET``
requests to ``/verify`` will mark the email address as verified. Finally, applications can also
reset the verification status of a user's email address for any reason at ``/verify/reset``.

.. note::

    Thunder currently relies on Simple Email Service (SES) to send emails, so an AWS account is
    required if email verification is enabled for your instance of Thunder.

Server-Side Password Hashing
============================

Thunder can perform server-side password hashing of user passwords. By default in version 2.0+,
Thunder will not hash any user passwords. However, you can enable this in your configuration, and
additionally specify the hashing algorithm to be used. See :ref:`configuration-hash` for more
information on the configuration options.

Additional User Properties
==========================

Thunder always requires that your user objects contain an email address and a password. However,
you can include any additional number of properties in your user objects. By default, additional
user properties are flexible and Thunder will not perform any validation of these properties. For
example, you can create two users like the following:

User 1
------

.. code-block:: json

    {
      "email": "sampleuser@sanctionco.com",
      "password": "hunter2",
      "properties": {
        "appId": 1234567890
      }
    }

User 2
------

.. code-block:: json

    {
      "email": "seconduser@sanctionco.com",
      "password": "hunter3",
      "properties": {
        "appId": 1234567890,
        "additionalProperty": "So many properties!"
      }
    }

and Thunder will accept both.

You can also configure Thunder to perform validation on these properties to ensure that all users
have the same properties and that they are the correct type (String, Integer, Double, etc). See
:ref:`configuration-properties` for more information on the configuration options.

Customizable Email Contents
===========================

The contents of verification emails can be completely customized. See :ref:`configuration-email`
for more information on the configuration options.

Customizable Verification Success Page
======================================

The success page that is shown to the end-user when their email is successfully verified can be
customized. See :ref:`configuration-email` for more information on the configuration options.

Official Docker Image
=====================

Thunder provides an `official Docker image <https://hub.docker.com/r/rohannagar/thunder/>`_ so that
your instance of Thunder can be easily run in a container environment. There is also documentation
on how to run Thunder in Kubernetes.

Client Libraries
================

Thunder provides client libraries for easy communication between your application and your instance
of Thunder. See :ref:`client-libraries` for more information on the client libraries.
