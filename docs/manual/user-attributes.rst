.. title:: User Attributes

.. _user-attributes:

###############
User Attributes
###############

Exposed Attributes
==================

- ``email`` The email for the user, represented as a string. This is always required, as it is the unique identifier for a user.

- ``password`` The user's password as a string. This is always required, and will be stored as a hashed version of the actual password.

- ``creationTime`` A ``long`` representing the time in epoch milliseconds that this user was created in the database.

- ``lastUpdateTime`` A ``long`` representing the time in epoch milliseconds that this user was last updated in the database.

- ``properties`` A map of additional user properties. These can be anything you wish, using a `String` as the identifier and any object type as the value. Properties can be validated on `POST`/`PUT` by enabling :ref:`configuration-properties`.

Extra Attributes
================

In addition, Thunder stores metadata informational attributes about each user. These are stored in the database but are not exposed through the API at this time.

- ``id`` A unique identifier for the user. This will be created when the new user is created, and never updated after that.

- ``version`` A unique string that determines the current version of the user. This is used to verify updates to a user, in the case where two updates to the same user happen simultaneously.
