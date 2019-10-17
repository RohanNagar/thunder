#!/bin/sh

# Get program arguments
TEST_NAME=$1

# Navigate to top level thunder directory
cd "$(dirname "$0")/../.." || exit
echo "Working from directory: $(pwd)"
echo

# Start containers
echo "Starting docker-compose..."
docker-compose -f "scripts/tests/$TEST_NAME/docker-compose.yml" up -d

# Wait for containers to start
echo "Waiting 10 seconds for containers to come up..."
sleep 10

# Run tests
echo "Running integration tests..."
node scripts/tests/test-runner.js "scripts/tests/$TEST_NAME/tests.yaml" -m
TEST_RESULT=$?

# Clean up
echo "Done running tests..."
docker-compose -f "scripts/tests/$TEST_NAME/docker-compose.yml" down

# Determine success or failure
if [ "$TEST_RESULT" -eq 0 ]; then
    echo "Successfully finished integration tests."
    exit 0
else
    echo "There are integration test failures."
    exit 1
fi
