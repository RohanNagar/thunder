.. title:: Endpoints

#########
Endpoints
#########

This page documents each of the available endpoints and what options exist.

- [Create User](#post)
- [Update User](#put)
- [Get User](#get)
- [Delete User](#delete)
- [Send Verification Email](#post-1)
- [Verify User](#get-1)
- [Get Verification Success Page](#get-2)

Create User
===========

The POST endpoint is for adding a new user to the database.

Basic Authentication: Yes

Header Parameters: None

Query Parameters: None

Body:
  - A JSON object that defines the new user

Example
-------

.. code-block:: text

   POST https://thunder.sanctionco.com/users

.. code-block:: json

   {
     "email" : {
       "address" : "sampleuser@sanctionco.com"
     },
     "password" : "12345",
     "properties" : {
       "myCustomProperty" : "Hello World"
     }
   }

#### Response:
  ```json
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
  ```
<hr>

### `PUT`

The PUT endpoint is for updating a specific user.

#### Basic Authentication: Yes

#### Header Parameters:
  - `password` The (hashed) password of the user to update

#### Query Parameters:
  - `email` The current email address of the user to update.
    This is optional, and only required if the email is to be changed.

#### Body:
  - A JSON object that defines the updated user.
    All fields must be present in the JSON, or they will be overridden in the database as `null`.
    The email address **can** be a new email.

#### Example:
  `PUT https://thunder.sanctionco.com/users?email=sampleuser@sanctionco.com`

  ```json
  Request Body:

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
  ```

#### Response:
  ```json
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
  ```
<hr>

### `GET`

Gets a user object from the database.

#### Basic Authentication: Yes

#### Header Parameters:
  - `password` The (hashed) password of the user

#### Query Parameters:
  - `email` The email address of the user to get

#### Example:
  `GET https://thunder.sanctionco.com/users?email=sampleuser@sanctionco.com`

#### Response:
  ```json
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
  ```
<hr>

### `DELETE`

Deletes a user from the database. The response contains the deleted object.

#### Basic Authentication: Yes

#### Header Parameters:
  - `password` The (hashed) password of the user

#### Query Parameters:
  - `email` The email address of the user to delete

#### Example:
  `DELETE https://thunder.sanctionco.com/users?email=sampleuser@sanctionco.com`

#### Response:
  ```json
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
  ```

## `/verify`

### `POST`

A POST request sent to the verify endpoint is used to initiate a user verification process by
sending a verification email to the email address provided as a query parameter. The user in the database will be
updated to include a unique verification token that is sent along with the email.

#### Basic Authentication: Yes

#### Header Parameters:
  - `password` The (hashed) password of the user

#### Query Parameters:
  - `email` The email address of the user to send the email to

#### Example:
  `POST https://thunder.sanctionco.com/verify?email=sampleuser@sanctionco.com`

#### Response:
  ```json
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
  ```
<hr>

### `GET`

A GET request sent to the verify endpoint is used to verify a user email. Typically, the user will click on this
link in their email to verify their account. Upon verification, the user object in the database will be updated to
indicate that the email address is verified.

#### Basic Authentication: No

#### Header Parameters: None

#### Query Parameters:
  - `email` The email address of the user to verify
  - `token` The verification token that was sent to the user via email
  - `response_type` Determines if the method should return either an HTML success page or a JSON user response
    If HTML is specified, the URL will redirect to `/verify/success`. The default `response_type` is JSON.

#### Example:
  `GET https://thunder.sanctionco.com/verify?email=sampleuser@sanctionco.com&token=0a4b81f3-0756-468e-8d98-7199eaab2ab8&response_type=json`

#### Response:
  ```json
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
  ```

## `/verify/success`

### `GET`

This GET request will return an HTML success page that is shown after a user successfully verifies
their account. `GET /verify` will redirect to this URL if the `response_type` query parameter
is set to `html`.

#### Basic Authentication: No

#### Header Parameters: None

#### Query Parameters: None

#### Example:
  `GET https://thunder.sanctionco.com/verify/success`

#### Response:
  ```html
    <!DOCTYPE html>
    <html>
      <div class="alert alert-success">
        <div align="center"><strong>Success!</strong><br>Your account has been verified.</div>
      </div>
      <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" />
    </html>
  ```

