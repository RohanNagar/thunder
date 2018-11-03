.. title:: Endpoints

.. _endpoints:

#########
Endpoints
#########

Create User
===========

.. http:post:: /users

   Creates a new user in the database.

   **Example**:

   .. http:example:: curl wget httpie

      POST /users HTTP/1.1
      Authorization: Basic YWRtaW46YWRtaW4=
      Content-Type: application/json

      {
        "email" : {
          "address" : "sampleuser@sanctionco.com"
        },
        "password" : "12345",
        "properties" : {
          "myCustomProperty" : "Hello World"
        }
      }


      HTTP/1.1 201 CREATED
      Content-Type: application/json

      {
        "email" : {
          "address" : "sampleuser@sanctionco.com",
          "verified" : false,
          "verificationToken" : null
        },
        "password" : "12345",
        "properties" : {
          "myCustomProperty" : "Hello World"
        }
      }

   :reqheader Authorization: basic authentication application name and secret
   :statuscode 201: user was successfully created
   :statuscode 400: the create request was malformed
   :statuscode 409: the user already exists in the database
   :statuscode 500: the database rejected the request for an unknown reason
   :statuscode 503: the database is currently unavailable

Update User
===========

.. http:put:: /users

   Updates an existing user in the database.

   **Example**:

   .. http:example:: curl wget httpie

      PUT /users?email=sampleuser@sanctionco.com HTTP/1.1
      Authorization: Basic YWRtaW46YWRtaW4=
      Content-Type: application/json
      password: YWRtaW46YWRtaW4=

      {
        "email" : {
          "address" : "newsampleuser@sanctionco.com",
          "verified" : false,
          "verificationToken" : null
        },
        "password" : "12345",
        "properties" : {
          "myCustomProperty" : "My properties have changed"
        }
      }


      HTTP/1.1 200 OK
      Content-Type: application/json

      {
        "email" : {
          "address" : "newsampleuser@sanctionco.com",
          "verified" : false,
          "verificationToken" : null
        },
        "password" : "12345",
        "properties" : {
          "myCustomProperty" : "My properties have changed"
        }
      }

   :query email: the existing email address of the user to update. This is optional, and only
         required if the email is to be changed.
   :reqheader Authorization: basic authentication application name and secret
   :reqheader password: the (hashed) password of the user to update
   :statuscode 200: user was successfully updated
   :statuscode 400: the update request was malformed
   :statuscode 401: the request was unauthorized
   :statuscode 404: the existing user to update was not found in the database
   :statuscode 409: a user with the new email already exists in the database
   :statuscode 500: the database rejected the request for an unknown reason
   :statuscode 503: the database is currently unavailable

Get User
========

.. http:get:: /users

   Updates an existing user in the database.

   **Example**:

   .. http:example:: curl wget httpie

      GET /users?email=sampleuser@sanctionco.com HTTP/1.1
      Authorization: Basic YWRtaW46YWRtaW4=
      Content-Type: application/json
      password: YWRtaW46YWRtaW4=


      HTTP/1.1 200 OK
      Content-Type: application/json

      {
        "email" : {
          "address" : "sampleuser@sanctionco.com",
          "verified" : false,
          "verificationToken" : null
        },
        "password" : "12345",
        "properties" : {
          "myCustomProperty" : "Hello World"
        }
      }

   :query email: the email address of the user
   :reqheader Authorization: basic authentication application name and secret
   :reqheader password: the (hashed) password of the user
   :statuscode 200: the operation was successful
   :statuscode 400: the get request was malformed
   :statuscode 401: the request was unauthorized
   :statuscode 404: the user was not found in the database
   :statuscode 503: the database is currently unavailable

Delete User
===========

.. http:delete:: /users

   Deletes a user from the database.

   **Example**:

   .. http:example:: curl wget httpie

      DELETE /users?email=sampleuser@sanctionco.com HTTP/1.1
      Authorization: Basic YWRtaW46YWRtaW4=
      Content-Type: application/json
      password: YWRtaW46YWRtaW4=


      HTTP/1.1 200 OK
      Content-Type: application/json

      {
        "email" : {
          "address" : "sampleuser@sanctionco.com",
          "verified" : false,
          "verificationToken" : null
        },
        "password" : "12345",
        "properties" : {
          "myCustomProperty" : "Hello World"
        }
      }

   :query email: the email address of the user
   :reqheader Authorization: basic authentication application name and secret
   :reqheader password: the (hashed) password of the user
   :statuscode 200: the operation was successful
   :statuscode 400: the delete request was malformed
   :statuscode 401: the request was unauthorized
   :statuscode 404: the user was not found in the database
   :statuscode 503: the database is currently unavailable

Send Verification Email
=======================

.. http:post:: /verify

   Initiates the user verification process by sending a verification email
   to the email address provided as a query parameter. The user in the database will be updated
   to include a unique verification token that is sent along with the email.

   **Example**:

   .. http:example:: curl wget httpie

      POST /verify?email=sampleuser@sanctionco.com HTTP/1.1
      Authorization: Basic YWRtaW46YWRtaW4=
      Content-Type: application/json
      password: YWRtaW46YWRtaW4=


      HTTP/1.1 200 OK
      Content-Type: application/json

      {
        "email" : {
          "address" : "sampleuser@sanctionco.com",
          "verified" : false,
          "verificationToken" : "0a4b81f3-0756-468e-8d98-7199eaab2ab8"
        },
        "password" : "12345",
        "properties" : {
          "myCustomProperty" : "Hello World"
        }
      }

   :query email: the email address of the user
   :reqheader Authorization: basic authentication application name and secret
   :reqheader password: the (hashed) password of the user
   :statuscode 200: the operation was successful
   :statuscode 400: the send email request was malformed
   :statuscode 401: the request was unauthorized
   :statuscode 404: the user to email was not found in the database
   :statuscode 500: the database rejected the request for an unknown reason
   :statuscode 503: the database is currently unavailable

Verify User
===========

.. http:get:: /verify

   Used to verify a user email. Typically, the user will click on this link in their email
   to verify their account. Upon verification, the user object in the database
   will be updated to indicate that the email address is verified.

   **Example**:

   .. http:example:: curl wget httpie

      GET /verify?email=sampleuser@sanctionco.com&token=0a4b81f3-0756-468e-8d98-7199eaab2ab8&response_type=json HTTP/1.1
      Content-Type: application/json


      HTTP/1.1 200 OK
      Content-Type: application/json

      {
        "email" : {
          "address" : "sampleuser@sanctionco.com",
          "verified" : true,
          "verificationToken" : "0a4b81f3-0756-468e-8d98-7199eaab2ab8"
        },
        "password" : "12345",
        "properties" : {
          "myCustomProperty" : "Hello World"
        }
      }

   :query email: the email address of the user
   :query token: the verification token from the email that was associated with the user
   :query response_type: the optional response type, either HTML or JSON. If HTML is specified,
         the URL will redirect to ``/verify/success``. The default ``response_type`` is JSON.
   :statuscode 200: the operation was successful and JSON was returned
   :statuscode 303: the request is redirecting to ``/verify/success``
   :statuscode 400: the get request was malformed
   :statuscode 404: the user to verify was not found in the database
   :statuscode 500: the request failed for a potentially unknown reason
   :statuscode 503: the database is currently unavailable

Reset Verification Status
=========================

.. http:post:: /verify/reset

   Resets the verification status of the user's email to false.

   **Example**:

   .. http:example:: curl wget httpie

      POST /verify/reset?email=sampleuser@sanctionco.com HTTP/1.1
      Authorization: Basic YWRtaW46YWRtaW4=
      Content-Type: application/json
      password: YWRtaW46YWRtaW4=


      HTTP/1.1 200 OK
      Content-Type: application/json

      {
        "email" : {
          "address" : "sampleuser@sanctionco.com",
          "verified" : false,
          "verificationToken" : null
        },
        "password" : "12345",
        "properties" : {
          "myCustomProperty" : "Hello World"
        }
      }

   :query email: the email address of the user
   :reqheader Authorization: basic authentication application name and secret
   :reqheader password: the (hashed) password of the user
   :statuscode 200: the operation was successful and JSON was returned
   :statuscode 400: the reset request was malformed
   :statuscode 401: the request was unauthorized
   :statuscode 404: the user to reset was not found in the database
   :statuscode 500: the database rejected the request for an unknown reason
   :statuscode 503: the database is currently unavailable

Get Verification Success Page
=============================

.. http:get:: /verify/success

   Returns an HTML success page that is shown after a user successfully verifies their account.
   ``GET /verify`` will redirect to this URL if the ``response_type`` query parameter
   is set to ``html``.

   **Example**:

   .. http:example:: curl wget httpie

      GET /verify/success HTTP/1.1
      Content-Type: text/html


      HTTP/1.1 200 OK
      Content-Type: text/html

      <!DOCTYPE html>
      <html>
        <div class="alert alert-success">
          <div align="center"><strong>Success!</strong><br>Your account has been verified.</div>
        </div>
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" />
      </html>

   :statuscode 200: the operation was successful
