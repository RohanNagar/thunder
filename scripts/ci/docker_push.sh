#!/bin/bash

echo "$DOCKER_PASSWORD" | docker login -u rohannagar --password-stdin
docker push rohannagar/thunder:edge
