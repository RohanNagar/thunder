#!/bin/bash

TAG_NUMBER="${TRAVIS_TAG:1}"
IMAGE_TAG="rohannagar/thunder:$TAG_NUMBER"

# This script assumes that the rohannagar/thunder:edge image
# has already been built.
docker tag rohannagar/thunder:edge "$IMAGE_TAG"

docker push "$IMAGE_TAG"
