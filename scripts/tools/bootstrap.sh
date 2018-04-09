#!/bin/bash

# Installs required dependencies for the application
echo "Welcome to Thunder development!"
echo "Setting up your machine to get ready for development...\n"

# Navigate to top level thunder directory
cd "$(dirname "$0")/../.." || exit
echo "Working from directory: $(pwd)\n"

# Install tools on Linux
if [ "$(uname -s)" = "Linux" ]; then
  # Set up Node.js 9 repository
  curl -sL https://deb.nodesource.com/setup_9.x | sudo -E bash -

  echo "Installing Java 8, Maven, and Node using apt-get."
  apt-get install openjdk-8-jdk maven nodejs -y

  if [[ $? -ne 0 ]]; then
    echo "[ERROR] There was an error installing packages."
    echo "[ERROR] Make sure you run the script using 'sudo'"

    exit 1
  fi
fi

# Install tools on macOS
if [ "$(uname -s)" = "Darwin" ]; then
  # Check for Homebrew
  which -s brew
  if [[ $? != 0 ]]; then
    echo "[ERROR] No Homebrew found in your environment!"
    echo "[ERROR] Please install Java 8, Maven, Node.js, and NPM separately."

    exit 1
  else
    echo "Updating Homebrew..."
    brew update

    echo "Installing Java 8, Maven, and Node using Homebrew."
    brew tap caskroom/versions

    brew cask install java8
    brew install maven
    brew install node
  fi
fi

echo "Installing Maven dependencies..."
mvn install -DskipTests=true -Dmaven.javadoc.skip=true -B -V

echo "Installing NPM dependencies..."
npm --prefix scripts/ install

