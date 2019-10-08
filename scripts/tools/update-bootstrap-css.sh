#!/bin/bash

# File where current Bootstrap version is held
SUCCESS_HTML_FILE="application/src/main/resources/success.html"

# Function to check if a version is greater than another
function version_gt() { test "$(printf '%s\n' "$@" | sort -V | head -n 1)" != "$1"; }

# Navigate to top level thunder directory
cd "$(dirname "$0")/../.." || exit
echo "Working from directory: $(pwd)"
echo

# Get latest bootstrap version
BOOTSTRAP_LATEST="$(npm show bootstrap version)"
echo "Latest bootstrap version: $BOOTSTRAP_LATEST"

# Make sure the CSS exists
RESPONSE=$(curl --write-out "%{http_code}" --silent --output /dev/null "https://maxcdn.bootstrapcdn.com/bootstrap/$BOOTSTRAP_LATEST/css/bootstrap.min.css")
echo "Response from CURL to verify the CSS exists: $RESPONSE"

if [ "$RESPONSE" -ne 200 ] ; then
  echo "The Bootstrap CSS for version $BOOTSTRAP_LATEST does not exist. Response: $RESPONSE"
  exit 1
fi

# Get current version
CURRENT_VERSION="$(perl -pe '($_)=/([0-9]+([.][0-9]+)+)/' "$SUCCESS_HTML_FILE" )"
echo "Current bootstrap version in success.html: $CURRENT_VERSION"

# Check if update is required
if ! version_gt "$BOOTSTRAP_LATEST" "$CURRENT_VERSION" ; then
  echo "Current version is up to date."
  exit 0
fi

# Update the version in all files
echo "Updating bootstrap version..."

CURRENT_RESOURCE="https://maxcdn.bootstrapcdn.com/bootstrap/${CURRENT_VERSION}/css/bootstrap.min.css"
DESIRED_RESOURCE="https://maxcdn.bootstrapcdn.com/bootstrap/${BOOTSTRAP_LATEST}/css/bootstrap.min.css"

if [ "$(uname -s)" = "Darwin" ] ; then
  # macOS
  grep -rl --include=*.{html,yaml,java} "${CURRENT_RESOURCE}" . | xargs sed -i '' "s,${CURRENT_RESOURCE},${DESIRED_RESOURCE},g"
else
  # Linux
  grep -rl --include=*.{html,yaml,java} "${CURRENT_RESOURCE}" . | xargs sed -i "s,${CURRENT_RESOURCE},${DESIRED_RESOURCE},g"
fi

# Set PR details
echo ::set-env name=PULL_REQUEST_TITLE::"Update Bootstrap CSS version to ${BOOTSTRAP_LATEST}"
echo ::set-env name=COMMIT_MESSAGE::"Update Bootstrap CSS version to ${BOOTSTRAP_LATEST}"
