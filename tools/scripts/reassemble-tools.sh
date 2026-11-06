#!/bin/bash
INSTALL_DIR="${HOME}/onedoc-tools"
REPO_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
CHUNKS_DIR="${REPO_DIR}/tools/chunks"
echo "=== OneDoc Tool Reassembly ==="
if compgen -G "${CHUNKS_DIR}/java17/java17.tar.part.*" > /dev/null 2>&1; then
  echo "[1/3] Java 17..."
  mkdir -p "${INSTALL_DIR}/java17"
  cat "${CHUNKS_DIR}/java17/java17.tar.part.*" > /tmp/java17.tar
  tar -xf /tmp/java17.tar -C "${INSTALL_DIR}/java17" --strip-components=1
  rm -f /tmp/java17.tar
  echo "export JAVA_HOME=${INSTALL_DIR}/java17"
  echo "export PATH=\${JAVA_HOME}/bin:\${PATH}"
fi
if compgen -G "${CHUNKS_DIR}/gradle/gradle.tar.part.*" > /dev/null 2>&1; then
  echo "[2/3] Gradle 8.10.2..."
  mkdir -p "${INSTALL_DIR}/gradle"
  cat "${CHUNKS_DIR}/gradle/gradle.tar.part.*" > /tmp/gradle.tar
  tar -xf /tmp/gradle.tar -C "${INSTALL_DIR}/gradle" --strip-components=1
  rm -f /tmp/gradle.tar
  echo "export GRADLE_HOME=${INSTALL_DIR}/gradle/gradle-8.10.2"
  echo "export PATH=\${GRADLE_HOME}/bin:\${PATH}"
fi
if compgen -G "${CHUNKS_DIR}/android-sdk/android-sdk.tar.part.*" > /dev/null 2>&1; then
  echo "[3/3] Android SDK..."
  mkdir -p "${INSTALL_DIR}/android-sdk"
  cat "${CHUNKS_DIR}/android-sdk/android-sdk.tar.part.*" > /tmp/android-sdk.tar
  tar -xf /tmp/android-sdk.tar -C "${INSTALL_DIR}/android-sdk" --strip-components=1
  rm -f /tmp/android-sdk.tar
  echo "export ANDROID_HOME=${INSTALL_DIR}/android-sdk"
  echo "export PATH=\${ANDROID_HOME}/platform-tools:\${PATH}"
fi
echo "=== Done ==="
