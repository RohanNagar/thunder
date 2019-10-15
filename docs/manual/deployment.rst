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

To create a DynamoDB table, use the template in ``scripts/aws/dynamo-table.yaml`` and deploy that template to AWS using CloudFormation.
Use the desired table name as the ``TableName`` parameter to the template.

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


Login to Azure
^^^^^^^^^^^^^^

.. code-block:: bash

    $ az login


Create a Resource Group
^^^^^^^^^^^^^^^^^^^^^^^

.. code-block:: bash

    $ az group create --name thunder --location eastus


Register Resource Providers
^^^^^^^^^^^^^^^^^^^^^^^^^^^

If not already done, make sure you have the necessary resource providers registered.

.. code-block:: bash

    $ az provider register -n Microsoft.Network
    $ az provider register -n Microsoft.Storage
    $ az provider register -n Microsoft.Compute
    $ az provider register -n Microsoft.ContainerService


Create AKS Cluster and Connect
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. code-block:: bash

    $ az aks create --resource-group thunder --name thunder --node-count 1 --generate-ssh-keys --kubernetes-version 1.14.6 --node-vm-size Standard_B4ms

    $ az aks get-credentials --resource-group thunder --name thunder

    # Verify that you are connected
    $ kubectl get nodes


4. Deploy Thunder
=================

Use the `Helm chart <https://github.com/RohanNagar/thunder/tree/master/scripts/deploy/helm/thunder>`_ to deploy Thunder
to your Kubernetes cluster.

.. code-block:: bash

    # Make sure Helm is set up locally and install Tiller in the cluster
    $ helm init

Edit the ``values.yaml`` file to set the configuration. Then, install the chart.

.. code-block:: bash

    $ helm install --name thunder scripts/deploy/helm/thunder

If you have the following error:

.. code-block:: bash

    Error: release thunder failed: namespaces "default" is forbidden: User "system:serviceaccount:kube-system:default" cannot get resource
    "namespaces" in API group "" in the namespace "default"

Then run the following commands and try again:

.. code-block:: bash

    $ kubectl create serviceaccount --namespace kube-system tiller
    $ kubectl create clusterrolebinding tiller-cluster-rule --clusterrole=cluster-admin --serviceaccount=kube-system:tiller
    $ kubectl patch deploy --namespace kube-system tiller-deploy -p '{"spec":{"template":{"spec":{"serviceAccount":"tiller"}}}}'

After installing the Helm chart, wait a few minutes for the load balancer to come up. Once it's up, you'll have an IP to use!

.. code-block:: bash

    $ export SERVICE_IP=$(kubectl get svc --namespace default thunder -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
    $ echo http://$SERVICE_IP:80


5. Add Domain Record (Optional)
===============================

If you have a custom domain name that you own, and you want to point it to your running instance of Thunder, find the IP address of your Load Balancer by running:

.. code-block:: bash

    $ kubectl get svc thunder

and looking for the External IP. Using this IP address, add an ``A`` record to your domain or subdomain that you want to point to Thunder.
If you are on AWS, add a ``CNAME`` record using the domain name of the Elastic Load Balancer.