#!/bin/sh

# Navigate to top level thunder directory
cd "$(dirname "$0")/../.." || exit
echo "Working from directory: $(pwd)"
echo

# Display versions
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
NODE_VERSION=$(node -v)

echo "Using java version: $JAVA_VERSION"
echo "Using node version: $NODE_VERSION"
echo

# Start Thunder
echo "Starting Thunder and running Node.js integration tests..."
java -jar application/target/application-*.jar server config/test-config.yaml &

THUNDER_PID=$!
echo "Thunder PID is $THUNDER_PID"
echo

# Wait for Thunder to start
echo "Waiting 5 seconds for Thunder to start up..."
sleep 5

# Run the tests
echo "Starting script..."
node scripts/tests/test-runner.js -l scripts/tests/general/tests.yaml -m
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
