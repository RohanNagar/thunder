#!/bin/bash

# Get the test file from passed arguments
TEST_FILE=$1

docker-compose -f scripts/tests/general/docker-compose.yml up -d
sleep 10 # Wait for Thunder to start

node scripts/tests/test-runner.js "$TEST_FILE"
