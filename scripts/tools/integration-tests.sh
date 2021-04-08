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

# Start local dependencies
echo "Starting local dependencies..."
node scripts/tools/run-local-dependencies.js &

DEPENDENCIES_PID=$!
echo "Local dependencies PID is $DEPENDENCIES_PID"
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
./scripts/node_modules/.bin/artillery run "scripts/tests/general/tests.yml" -o artillery.json --quiet

echo "Done running script. Killing Thunder..."
kill -9 "$THUNDER_PID"

echo "Killing local dependencies..."
kill -9 "$DEPENDENCIES_PID"

# Determine success or failure
if [ "$(jq '.aggregate.errors' artillery.json | jq length)" -eq 0 ]; then
    echo "Successfully finished integration tests."
    exit 0
else
    echo "There are integration test failures."
    exit 1
fi
