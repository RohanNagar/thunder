#!/bin/bash

# File where current Bootstrap version is held
CURRENT_VERSION_FILE="scripts/tools/current-bootstrap-version.txt"

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
RESPONSE=$(curl -L --write-out "%{http_code}" --silent --output /dev/null "https://cdn.jsdelivr.net/npm/bootstrap@$BOOTSTRAP_LATEST/dist/css/bootstrap.min.css")
echo "Response from CURL to verify the CSS exists: $RESPONSE"

if [ "$RESPONSE" -ne 200 ] ; then
  echo "The Bootstrap CSS for version $BOOTSTRAP_LATEST does not exist. Response: $RESPONSE"
  exit 1
fi

# Get current version
CURRENT_VERSION="$(perl -pe '($_)=/([0-9]+([.][0-9]+)+)/' "$CURRENT_VERSION_FILE" )"
echo "Current bootstrap version in success.html: $CURRENT_VERSION"

# Check if update is required
if ! version_gt "$BOOTSTRAP_LATEST" "$CURRENT_VERSION" ; then
  echo "Current version is up to date."
  exit 0
fi

# Update the version in all files
echo "Updating bootstrap version..."

CURRENT_RESOURCE="https://cdn.jsdelivr.net/npm/bootstrap@${CURRENT_VERSION}/dist/css/bootstrap.min.css"
DESIRED_RESOURCE="https://cdn.jsdelivr.net/npm/bootstrap@${BOOTSTRAP_LATEST}/dist/css/bootstrap.min.css"

if [ "$(uname -s)" = "Darwin" ] ; then
  # macOS
  grep -rl --include=*.{html,yaml,java} "${CURRENT_RESOURCE}" . | xargs sed -i '' "s,${CURRENT_RESOURCE},${DESIRED_RESOURCE},g"
else
  # Linux
  grep -rl --include=*.{html,yaml,java} "${CURRENT_RESOURCE}" . | xargs sed -i "s,${CURRENT_RESOURCE},${DESIRED_RESOURCE},g"
fi

# Update the current version file
echo "$BOOTSTRAP_LATEST" > "$CURRENT_VERSION_FILE"
echo "" >> "$CURRENT_VERSION_FILE"

# Set PR details
echo ::set-output name=pr_title::"Update Bootstrap CSS version to ${BOOTSTRAP_LATEST}"
echo ::set-output name=pr_body::"Update Bootstrap CSS version to ${BOOTSTRAP_LATEST}"
echo ::set-output name=commit_message::"Update Bootstrap CSS version to ${BOOTSTRAP_LATEST}"
