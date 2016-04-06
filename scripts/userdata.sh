#!/bin/bash

# Install Java
sudo yum remove java-1.7.0-openjdk
sudo yum install java-1.8.0

# Download jar and SSL cert from S3
aws s3 cp s3://certificates.sanction.com/PilotCert.crt thunder/
aws s3 cp s3://artifacts.sanction.com/maven/releases/com/sanction/thunder/application/0.3.0/application-0.3.0.jar thunder/
aws s3 cp s3://artifacts.sanction.com/config/com/sanction/thunder/config.yaml thunder/

# Set SSL cert in Java
keytool -import -trustcacerts -alias PilotCert -file thunder/PilotCert.crt -keystore cacerts

# Start the application
java -jar thunder/application-0.3.0.jar server config.yaml
