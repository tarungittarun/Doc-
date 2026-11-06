#!/bin/bash
shopt -s nullglob
INSTALL_DIR="${HOME}/onedoc-tools"
REPO_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
CHUNKS_DIR="${REPO_DIR}/tools/chunks"
echo "=== OneDoc Tool Reassembly ==="

JAVA8_CHUNKS=("${CHUNKS_DIR}"/java8/java8.tar.part.*)
if [ ${#JAVA8_CHUNKS[@]} -gt 0 ]; then
  echo "[1/4] Java 8..."
  mkdir -p "${INSTALL_DIR}/java8"
  cat "${JAVA8_CHUNKS[@]}" > /tmp/java8.tar
  tar -xf /tmp/java8.tar -C "${INSTALL_DIR}/java8" --strip-components=1
  rm -f /tmp/java8.tar
  echo "export JAVA8_HOME=${INSTALL_DIR}/java8"
  echo "export PATH=\${JAVA8_HOME}/bin:\${PATH}"
else
  echo "[1/4] Java 8 - no chunks found, skipping"
fi

JAVA17_CHUNKS=("${CHUNKS_DIR}"/java17/java17.tar.part.*)
if [ ${#JAVA17_CHUNKS[@]} -gt 0 ]; then
  echo "[2/4] Java 17..."
  mkdir -p "${INSTALL_DIR}/java17"
  cat "${JAVA17_CHUNKS[@]}" > /tmp/java17.tar
  tar -xf /tmp/java17.tar -C "${INSTALL_DIR}/java17" --strip-components=1
  rm -f /tmp/java17.tar
  echo "export JAVA_HOME=${INSTALL_DIR}/java17"
  echo "export PATH=\${JAVA_HOME}/bin:\${PATH}"
else
  echo "[2/4] Java 17 - no chunks found, skipping"
fi

GRADLE_CHUNKS=("${CHUNKS_DIR}"/gradle/gradle.tar.part.*)
if [ ${#GRADLE_CHUNKS[@]} -gt 0 ]; then
  echo "[3/4] Gradle 8.10.2..."
  mkdir -p "${INSTALL_DIR}/gradle"
  cat "${GRADLE_CHUNKS[@]}" > /tmp/gradle.tar
  tar -xf /tmp/gradle.tar -C "${INSTALL_DIR}/gradle" --strip-components=1
  rm -f /tmp/gradle.tar
  echo "export GRADLE_HOME=${INSTALL_DIR}/gradle"
  echo "export PATH=\${GRADLE_HOME}/bin:\${PATH}"
else
  echo "[3/4] Gradle 8.10.2 - no chunks found, skipping"
fi

ANDROID_CHUNKS=("${CHUNKS_DIR}"/android-sdk/android-sdk.tar.part.*)
if [ ${#ANDROID_CHUNKS[@]} -gt 0 ]; then
  echo "[4/4] Android SDK..."
  mkdir -p "${INSTALL_DIR}/android-sdk"
  cat "${ANDROID_CHUNKS[@]}" > /tmp/android-sdk.tar
  tar -xf /tmp/android-sdk.tar -C "${INSTALL_DIR}/android-sdk" --strip-components=1
  rm -f /tmp/android-sdk.tar
  echo "export ANDROID_HOME=${INSTALL_DIR}/android-sdk"
  echo "export PATH=\${ANDROID_HOME}/platform-tools:\${PATH}"
else
  echo "[4/4] Android SDK - no chunks found, skipping"
fi

echo "=== Done ==="
