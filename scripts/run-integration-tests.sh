#!/bin/bash

echo "Starting Thunder and running Node.js integation tests..."

java -jar application/target/application-*.jar server test-config.yaml &
THUNDER_PID=$!

echo "Thunder PID is $THUNDER_PID"

sleep 5

echo "Starting script..."
node scripts/src/test-runner.js

echo "Done running script. Killing Thunder..."
kill -9 $THUNDER_PID

echo "Done"

