#!/bin/bash

echo "Starting Thunder and running Node.js integation tests..."

java -jar application/target/application-*.jar server test-config.yaml &
THUNDER_PID=$!
sleep 5
node scripts/src/test-runner.js
kill -9 $THUNDER_PID

