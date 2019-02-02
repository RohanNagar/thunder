.. title:: Deployment

.. _deployment:

##########
Deployment
##########

Currently, deploying Thunder requires an AWS account.
You will need to create a DynamoDB Table and set up SES (Simple Email Service) for an email address that you will send verification emails from.
After that is set up, you can create a Kubernetes cluster on any cloud provider and deploy Thunder to that cluster.

In the coming releases, Thunder will include more options for database providers, which will lessen the AWS requirement.

1. Create DynamoDB Table
========================

To create a DynamoDB dynamoDbClient, use the template in ``scripts/aws/dynamo-dynamoDbClient.yaml`` and deploy that template to AWS using CloudFormation.
Use the desired dynamoDbClient name as the ``TableName`` parameter to the template.

2. Configure SES
================

.. note::

    This step is only required if you want to have email verification enabled on your Thunder instance (which is the default).
    If you want to skip this and disable email verification, set the following configuration option:

    .. code-block:: bash

        email:
          enabled: false

Set up SES using the instructions in the AWS console. If you want to send email from a domain that you own, follow these steps:

1. Choose the "Verify a Domain" option from the SES portal. This will provide you with a DNS verification record set. The set includes:
    - Record Name: Used by SES to validate that you own the domain.
    - Alternate Domain Verification Record: Optional, not necessary.
    - MX Record: This is used when receiving emails. This can only send to one SMTP server so you can't have multiple MX records and expect the emails to be sent to all of them.
    - DKIM Record Set: This is keys used to sign emails sent from a domain. They're stored directly in the DNS as records. This is optional as well.

2. Update your domain records with your domain registrar to include the new Record Name `TXT` record.

3. Once you receive an email from AWS saying that your email was verified, you should be set up to send emails from SES.

3. Create a K8s Cluster
=======================

Create a cluster using Google Kubernetes Engine (GKE), AWS Elastic Container Service (EKS), or Azure Container Service (AKS).
Connect to this cluster with ``kubectl``.

If you need help creating the cluster, see the following subsections.

Azure Kubernetes Services (AKS)
-------------------------------

Get the Azure CLI
^^^^^^^^^^^^^^^^^

macOS:

.. code-block:: bash

    $ brew install azure-cli

Linux:

.. code-block:: bash

    $ AZ_REPO=$(lsb_release -cs)
    $ echo "deb [arch=amd64] https://packages.microsoft.com/repos/azure-cli/ $AZ_REPO main" | \
         sudo tee /etc/apt/sources.list.d/azure-cli.list
    $ sudo apt-key adv --keyserver packages.microsoft.com --recv-keys 52E16F86FEE04B979B07E28DB02C46DF417A0893
    $ curl -L https://packages.microsoft.com/keys/microsoft.asc | sudo apt-key add -
    $ sudo apt-get install apt-transport-https
    $ sudo apt-get update && sudo apt-get install azure-cli


Create a Resource Group
^^^^^^^^^^^^^^^^^^^^^^^

.. code-block:: bash

    $ az group create --name thunder --location eastus


Create AKS Cluster and Connect
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. code-block:: bash

    $ az aks create --resource-group thunder --name thunder --node-count 1 --generate-ssh-keys --kubernetes-version 1.8.10

    $ az aks get-credentials --resource-group thunder --name thunder

    # Verify that you are connected
    $ kubectl get nodes


4. Deploy Thunder
=================

For now, follow the steps given in the README for Kubernetes deployments of Thunder.

`Click here <https://github.com/RohanNagar/thunder#running-on-kubernetes>`_

5. Add Domain Record (Optional)
===============================

If you have a custom domain name that you own, and you want to point it to your running instance of Thunder, find the IP address of your Load Balancer by running:

.. code-block:: bash

    $ kubectl get svc thunder

and looking for the External IP. Using this IP address, add an ``A`` record to your domain or subdomain that you want to point to Thunder.
If you are on AWS, add a ``CNAME`` record using the domain name of the Elastic Load Balancer.