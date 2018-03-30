#!/bin/bash

# Navigate to top level thunder directory
cd "$(dirname "$0")/../.."
echo "Running from directory:"
pwd

# Display the node version being used
echo "Using node version:"
node -v

# Start Thunder
echo "Starting Thunder and running Node.js integration tests..."
java -jar application/target/application-*.jar server config/test-config.yaml &

THUNDER_PID=$!
echo "Thunder PID is $THUNDER_PID"

# Wait for Thunder to start
echo "Waiting 5 seconds for Thunder to start up..."
sleep 5

# Run the tests
echo "Starting script..."
node scripts/src/test-runner.js
TEST_RESULT=$?

echo "Done running script. Killing Thunder..."
kill -9 "$THUNDER_PID"

# Determine success or failure
if [ "$TEST_RESULT" -eq 0 ]; then
    echo "Successfully finished integration tests."
    exit 0
else
    echo "There are integration test failures."
    exit 1
fi

