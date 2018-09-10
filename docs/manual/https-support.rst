.. title:: HTTPS Support

.. _https:

#############
HTTPS Support
#############

Hyper Text Transport Protocol Secure (HTTPS) allows the encryption of traffic between Thunder and its client connections.
SanctionCo highly recommends that you secure your traffic since unencrypted traffic exposes sensitive data to potential attackers.

There are two primary concerns when connecting to a Thunder instance:

1. Is my data confidential?
2. Is this Thunder instance trustworthy?

Thankfully both of these concerns are addressable using Transport Layer Security (TLS) as an underlying protocol to the existing HTTP protocol.

TLS allows the encryption of traffic between Thunder and its clients using any number of specific cipher algorithms,
and allows the validation of a servers ownership using trust chaining.

Quick Start
===========

If you don't want to create your own CA for testing you can always use the default one in the thunder ``config/`` directory.
The default java key store is called ``dev-server.jks`` and is already defined in the dev config file.
The only action you need to take it to import the ``ca-chain.cert.pem`` file also located in the ``config/`` directory.
Once imported you need to trust the certificate and you you'll be ready to test your HTTPS functionality!

Full Example
============

This short tutorial will walk you through the steps needed to secure your Thunder instances using your own self signed root certificate.

.. note::

    This should only be used for development and testing.
    In production it is highly recommended that you purchase a signed certificate from a common CA or use a well established key management system
    through any number of available cloud services. There are many steps not covered in this tutorial that are crucial to the long term success
    and security of a key management system.

Step 1: Create a self signed root CA certificate
------------------------------------------------

The root CA will act as your identity. Users will have a copy of your root CA certificate and will
use this when verifying the authenticity of a Thunder instance. While the public key is known by anyone,
it is crucial that you keep the private key safe (preferably offline).

This command will create your root CA certificate and it's corresponding private key both in PEM format.

.. code-block:: bash

	$ openssl req -x509 -new -out rootCA.crt -config openssl_ca.cnf

Step 2: Create a server certificate
-----------------------------------

The server certificate is what a specific Thunder instance uses to encrypt traffic to a connected user.
Both the private and public keys are stored on the server to make this possible.

This command will create a certificate sign request (CSR) containing your servers public key. We will sign this
key with the rootCA's private key and output a new certificate containing the servers private key and a signature from the root CA.
The config file also defines the key length for generating a private key and where to write it to.

.. code-block:: bash

	$ openssl req -new -out server.csr -config openssl.cnf

Step 3: Sign the server certificate with the root CA certificate
----------------------------------------------------------------

Signing the server certificate with our root CA certificate allows a user with our trusted root CA certificate
to validate any specific Thunder certificate as trusted since the root CA signed it.

.. note::

    Signing a CSR with extension fields does NOT copy the fields to the resulting certificate.
    For this you have to specify the extensions in the command line directly as shown in the below command.

.. code-block:: bash

	$ openssl x509 -req -in server.csr -CA rootCA.crt -CAkey rootCA.key -CAcreateserial -out server.crt -days 500 -sha256 -extfile openssl.cnf -extensions v3_req

Step 4: Convert server certificate and private key to PKCS#12 format
--------------------------------------------------------------------

The java keystore (jks) follows the ``pkcs#12`` standard for storing and managing public certificates and private keys.
This means we need to convert our public certificate and private key into a ``pkcs12`` file so the java keystore and import and use our certificates.

.. code-block:: bash

	$ openssl pkcs12 -export -in server.crt -inkey server.key -out server.p12 -CAfile rootCA.crt

This command takes in our server cert and private key, then takes in the CA certificate that signed it to create a complete certificate chain.
If we had CA's further up the chain we would include them too.

Step 5: Load the server certificate into the Java keystore
----------------------------------------------------------

The keystore allows Dropwizard to recognize our keys and encrypt our traffic / prove our identity to users.
The entire certificate chain from the root CA's certificate to the servers certificate needs to be included.
This is because a user should be able to confirm the root of trust as your CA certificate no matter how many intermediate CA's and server certificates exist.

.. code-block:: bash

	$ keytool -importkeystore -deststorepass password -destkeypass password -destkeystore server.jks \
	-srckeystore server.p12 -srcstoretype PKCS12 -srcstorepass password

Make sure you use the password for the java key store you created in step 3 for the ``srcstorepass`` flag.
The ``destkeypass`` is the password for the java keystore you are creating.

Step 6: Add fields to Dropwizard configuration file
---------------------------------------------------

Next we need to give Dropwizard the path to our keystore so it can encrypt our traffic.
``keyStorePath`` and ``keyStorePassword`` will specify the path and password of the keystore created in step 5.
``validateCerts`` and ``validatePeers`` are included as false to clarify that peers and clients will not require a certificate themselves for validation.
Here is an example ``config.yaml`` used for Thunder's development environment:

.. code-block:: yaml

    # Information to access DynamoDB
    database:
      endpoint: http://localhost:4567
      region: us-east-1
      tableName: pilot-users-test

    # Information to access SES
    email:
      endpoint: http://localhost:9001
      region: us-east-1
      fromAddress: noreply@sanctionco.com

    # Approved Application Authentication Credentials
    approvedKeys:
      - application: application
        secret: secret

    # Server configuration
    server:
      applicationConnectors:
        - type: http
          port: 8080
        - type: https
          port: 8443
          keyStorePath: ./config/server.jks
          keyStorePassword: password
          validateCerts: false
          validatePeers: false

      adminConnectors:
        - type: http
          port: 8081
        - type: https
          port: 8444
          keyStorePath: ./config/server.jks
          keyStorePassword: password
          validateCerts: false
          validatePeers: false

Step 7: Load the root CA certificate into your local certificate store
----------------------------------------------------------------------

We need to load the root CA's certificate onto our computers local certificate store and mark it as trustworthy.
Most common CA certificates are already on your computer when you purchase the operating system.
This usually means you can connect to most websites without trouble since they will have signed a certificate with a common CA.
Our CA is anything but common so we have to take this extra step for our connection can be trusted.

On MacOS open keychain access and do file > import items then navigate to your public rootCA.crt certificate. Or:

.. code-block:: bash

    $ sudo security add-trusted-cert -d -r trustRoot -k /Library/Keychains/System.keychain ~/rootCA.crt

To remove:

.. code-block:: bash

    $ sudo security delete-certificate -c "<name of existing certificate>"

On Linux (Ubuntu):

.. code-block:: bash

    $ sudo cp rootCA.crt /usr/local/share/ca-certificates/rootCA.crt
    $ sudo update-ca-certificates

To remove:

.. code-block:: bash

    $ sudo rm /usr/local/share/ca-certificates/rootCA.crt
    $ sudo update-ca-certificates --fresh

Example certificate configuration files
---------------------------------------

Openssl CA config

.. code-block:: text

    # This file is used to create a CA certificate and private key for Sanction development.

    [ req ]
    default_bits        = 4096
    distinguished_name  = req_distinguished_name
    default_keyfile     = rootCA.key
    prompt			    = no
    encrypt_key         = no
    default_md          = sha256
    x509_extensions     = v3_ca

    [ req_distinguished_name ]
    countryName            = "US"
    stateOrProvinceName    = "Texas"
    localityName           = "Austin"
    organizationName       = "Sanction"
    organizationalUnitName = "Sanction Development CA"
    commonName             = "sanctionco.com"

    [ v3_ca ]
    subjectKeyIdentifier = hash
    authorityKeyIdentifier = keyid:always,issuer
    basicConstraints = critical, CA:true
    keyUsage = critical, digitalSignature, cRLSign, keyCertSign


Openssl server config

.. code-block:: text

    # This file is used to create a CSR for signing with the Sanction development CA.

    [ req ]
    default_bits       = 2048
    default_md         = sha256
    default_keyfile    = server.key
    prompt			   = no
    encrypt_key        = no
    distinguished_name = req_distinguished_name
    req_extensions     = v3_req

    [ req_distinguished_name ]
    countryName            = "US"
    stateOrProvinceName    = "Texas"
    localityName           = "Austin"
    organizationName       = "Sanction"
    organizationalUnitName = "Development"
    commonName             = "sanctionco.com"

    [ v3_req ]
    subjectAltName = DNS:www.sanctionco.com,DNS:sanctionco.com,DNS:localhost