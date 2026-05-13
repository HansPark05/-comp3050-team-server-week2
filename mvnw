#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script, version 3.2.0
# ----------------------------------------------------------------------------

set -e

MAVEN_WRAPPER_PROPERTIES=".mvn/wrapper/maven-wrapper.properties"

# Read the distributionUrl from maven-wrapper.properties
DISTRIBUTION_URL=$(grep "distributionUrl" "$MAVEN_WRAPPER_PROPERTIES" | sed 's/distributionUrl=//')

# Determine local Maven home
MAVEN_USER_HOME="${MAVEN_USER_HOME:-$HOME/.m2}"
MAVEN_WRAPPER_HOME="$MAVEN_USER_HOME/wrapper"
DISTRIBUTION_FILE_NAME=$(basename "$DISTRIBUTION_URL")
DISTRIBUTION_DIR_NAME="${DISTRIBUTION_FILE_NAME%-bin.zip}"
MAVEN_HOME="$MAVEN_WRAPPER_HOME/dists/$DISTRIBUTION_DIR_NAME"

if [ ! -d "$MAVEN_HOME" ]; then
  echo "Downloading Maven from $DISTRIBUTION_URL"
  mkdir -p "$MAVEN_WRAPPER_HOME/dists"
  TMP_ZIP="$MAVEN_WRAPPER_HOME/dists/$DISTRIBUTION_FILE_NAME"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL -o "$TMP_ZIP" "$DISTRIBUTION_URL"
  elif command -v wget >/dev/null 2>&1; then
    wget -q -O "$TMP_ZIP" "$DISTRIBUTION_URL"
  else
    echo "ERROR: Neither curl nor wget found. Please install Maven manually." >&2
    exit 1
  fi
  unzip -q "$TMP_ZIP" -d "$MAVEN_WRAPPER_HOME/dists"
  rm -f "$TMP_ZIP"
fi

# Find the mvn binary inside the extracted directory
MVN_BIN=$(find "$MAVEN_WRAPPER_HOME/dists" -maxdepth 3 -name "mvn" -path "*/bin/mvn" | grep "$DISTRIBUTION_DIR_NAME" | head -1)

if [ -z "$MVN_BIN" ]; then
  echo "ERROR: Could not find mvn binary after extraction." >&2
  exit 1
fi

exec "$MVN_BIN" "$@"
