#!/bin/bash

REQUIRED_JAVA_VERSION=8
MINIMUM_MAVEN_VERSION=3
MINIMUM_NODE_VERSION=9

JAVA8_APT_PKG="openjdk-8-jdk"
MAVEN_APT_PKG="maven"
NODE_APT_PKG="nodejs"

JAVA8_BREW_PKG="java8"
MAVEN_BREW_PKG="maven"
NODE_BREW_PKG="node"

# Checks the Java version installed on the machine.
# Returns 1 if Java is not installed or is the wrong version.
# Returns 0 if the correct Java version is installed.
check_java_version() {
  command -v java >/dev/null 2>&1 || {
    echo "Java is not installed on your machine."
    return 1
  }

  JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)

  if [ "$(echo "$JAVA_VERSION" | cut -d'.' -f2)" -eq "$REQUIRED_JAVA_VERSION" ]; then
    echo "Your Java version is okay. Java version: $JAVA_VERSION"
    return 0
  else
    echo "You currently have Java version $JAVA_VERSION. Download required."
    return 1
  fi
}

# Checks the Maven version installed on the machine.
# Returns 1 if Maven is not installed or is the wrong version.
# Returns 0 if the correct Maven version is installed.
check_maven_version() {
  command -v mvn >/dev/null 2>&1 || {
    echo "Maven is not installed on your machine."
    return 1
  }

  MAVEN_VERSION=$(mvn -v 2>&1 | head -n 1 | cut -d' ' -f3)

  if [ "$(echo "$MAVEN_VERSION" | cut -d'.' -f1)" -ge "$MINIMUM_MAVEN_VERSION" ]; then
    echo "Your Maven version is okay. Maven version: $MAVEN_VERSION"
    return 0
  else
    echo "You currently have Maven version $MAVEN_VERSION. Download required."
    return 1
  fi
}

# Checks the Node version installed on the machine.
# Returns 1 if Node is not installed or is the wrong version.
# Returns 0 if the correct Node version is installed.
check_node_version() {
  command -v node >/dev/null 2>&1 || {
    echo "Node is not installed on your machine."
    return 1
  }

  NODE_VERSION=$(node -v 2>&1)

  if [ "$(echo "$NODE_VERSION" | cut -d'.' -f1 | sed 's/^.//')" -ge "$MINIMUM_NODE_VERSION" ]; then
    echo "Your Node version is okay. Node version: $NODE_VERSION"
    return 0
  else
    echo "You currently have Node version $NODE_VERSION. Download required."
    return 1
  fi
}

# Installs packages using manual methods such as curl and wget.
# Param 1: Either "java" or "none"
# Param 2: Either "maven" or "none"
# Param 3: Either "node" or "none"
install_packages_manually() {
  if [ "$1" = "java" ]; then
    echo "Installing Java 8 using install-jdk.sh"
    wget https://raw.githubusercontent.com/sormuras/bach/master/install-jdk.sh

    # shellcheck disable=SC1091
    . ./install-jdk.sh -F 8

    rm ./install-jdk.sh
    echo
  fi

  if [ "$2" = "maven" ]; then
    echo "We don't know how to install Maven manually."
    echo "Please install it yourself."
    echo
  fi

  if [ "$3" = "node" ]; then
    echo "Installing the latest Node.js"
    curl "https://nodejs.org/dist/latest/node-${VERSION:-$(wget -qO- https://nodejs.org/dist/latest/ | sed -nE 's|.*>node-(.*)\.pkg</a>.*|\1|p')}.pkg" > "$HOME/Downloads/node-latest.pkg" && sudo installer -store -pkg "$HOME/Downloads/node-latest.pkg" -target "/"
    echo
  fi

  return 0
}

# Installs packages on Linux using apt-get.
# Falls back to manual methods if apt-get is not available.
install_packages_linux() {
  command -v apt-get >/dev/null 2>&1 || {
    echo "apt-get is not installed on your machine."
    echo "Falling back to manual installation..."
    echo

    install_packages_manually "$@"
    return $?
  }

  # Set up Node.js if installing it
  if [[ "$*" = *"$NODE_APT_PKG"* ]]; then
    echo "Setting up the Node.js 9 repository..."
    curl -sL https://deb.nodesource.com/setup_9.x | sudo -E bash -
  fi

  pkg_list=($@)

  # Check if there is anything to install
  if [ ${#pkg_list[*]} -eq 0 ]; then
    echo "No tools to install."
    echo
    
    return 0
  fi

  echo "Using apt-get to install packages: ${pkg_list[*]}"
  if ! apt-get install "${pkg_list[@]}" -y; then
    echo "[ERROR] There was an error installing packages."
    echo "[ERROR] Are you root? Make sure you run the script using 'sudo'"

    return 1
  fi

  return 0
}

# Installs packages on macOS using Homebrew
# Falls back to manual methods if apt-get is not available.
install_packages_macos() {
  command -v brew >/dev/null 2>&1 || {
    echo "Homebrew is not installed on your machine."
    echo "Falling back to manual installation..."
    echo

    install_packages_manually "$1 $2 $3"
    return $?
  }
    
  echo "Updating Homebrew..."
  brew update

  if [[ "$*" = *"$JAVA8_BREW_PKG"* ]]; then
    echo "Installing Java 8 with brew..."
    echo

    brew tap caskroom/versions
    brew cask install java8
  fi

  no_java=${*//$JAVA8_BREW_PKG/}
  pkg_list=($no_java)

  # Check if there is anything to install
  if [ ${#pkg_list[*]} -eq 0 ]; then
    echo "No tools to install."
    echo
    
    return 0
  fi

  echo "Using brew to install packages: ${pkg_list[*]}"
  if ! brew install "${pkg_list[@]}"; then
    echo "[ERROR] There was an error installing packages."

    return 1
  fi

  return 0
}

# Installs required dependencies for the application
echo "Welcome to Thunder development!"
echo "Setting up your machine to get ready for development..."
echo

# Navigate to top level thunder directory
cd "$(dirname "$0")/../.." || exit
echo "Working from directory: $(pwd)"
echo

# Check existing tool versions
packages=""

check_java_version
if [ "$?" == 1 ]; then
  if [ "$(uname -s)" = "Linux" ]; then
    packages="$packages $JAVA8_APT_PKG"
  elif [ "$(uname -s)" = "Darwin" ]; then
    packages="$packages $JAVA8_BREW_PKG"
  fi
fi

check_maven_version
if [ "$?" == 1 ]; then
  if [ "$(uname -s)" = "Linux" ]; then
    packages="$packages $MAVEN_APT_PKG"
  elif [ "$(uname -s)" = "Darwin" ]; then
    packages="$packages $MAVEN_BREW_PKG"
  fi
fi

check_node_version
if [ "$?" == 1 ]; then
  if [ "$(uname -s)" = "Linux" ]; then
    packages="$packages $NODE_APT_PKG"
  elif [ "$(uname -s)" = "Darwin" ]; then
    packages="$packages $NODE_BREW_PKG"
  fi
fi

echo

# Install tools on Linux
if [ "$(uname -s)" = "Linux" ]; then
  install_packages_linux "$packages"
  
  if [ $? == 1 ]; then
    echo "An error occurred installing tools."
    echo "Please try again using sudo or install manually."
    exit 1
  fi
fi

# Install tools on macOS
if [ "$(uname -s)" = "Darwin" ]; then
  install_packages_macos "$packages"
  
  if [ $? == 1 ]; then
    echo "An error occurred installing tools."
    echo "Please try again or install manually."
    exit 1
  fi
fi

# Install code dependencies
echo "Installing Maven dependencies..."
mvn install -DskipTests=true -Dmaven.javadoc.skip=true -B -V

echo "Installing NPM dependencies..."
npm --prefix scripts/ install

