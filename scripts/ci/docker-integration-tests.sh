#!/bin/sh

# Check arguments
if [ "$1" ]; then
  echo "Correct number of arguments supplied."
else
  echo "Incorrect number of arguments, please make sure you include TEST_NAME".
  exit 1
fi

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
./scripts/node_modules/.bin/artillery run "scripts/tests/$TEST_NAME/tests.yml" -o artillery.json --quiet
TEST_EXIT_CODE=$?

echo "Running k6 tests..."
k6 run "scripts/tests/$TEST_NAME/test.js"

# Clean up
echo "Done running tests..."
docker-compose -f "scripts/tests/$TEST_NAME/docker-compose.yml" down

# Determine success or failure. Artillery should have no errors and should have exited with 0.
if [ "$(jq '.aggregate.errors' artillery.json | jq length)" -eq 0 ] && [ "$TEST_EXIT_CODE" -eq 0 ]; then
  echo "Successfully finished integration tests."
  exit 0
else
  echo "There are integration test failures."
  exit 1
fi
