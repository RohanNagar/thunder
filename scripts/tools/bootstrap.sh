#!/bin/bash

# Installs required dependencies for the application
# Run from the top level directory (i.e. "thunder/")

# Install tools on Linux
if [ "$(uname -s)" = "Linux" ]; then
  # Set up Node.js 9 repository
  curl -sL https://deb.nodesource.com/setup_9.x | sudo -E bash -

  # Java 8, Maven, Node.js, NPM
  apt-get install openjdk-8-jdk maven nodejs -y
fi

# Install tools on macOS
if [ "$(uname -s)" = "Darwin" ]; then
  # Check for Homebrew
  which -s brew
  if [[ $? != 0 ]]; then
    # Install Homebrew
  fi

  # Install Java 8, Maven, Node.js, NPM
fi


# Install Maven dependencies
mvn install -DskipTests=true -Dmaven.javadoc.skip=true -B -V

# Install NPM dependencies
npm --prefix scripts/ install

