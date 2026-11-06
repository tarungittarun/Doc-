#!/bin/bash
set -e
INSTALL_DIR="${HOME}/onedoc-tools"
REPO_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
CHUNKS_DIR="${REPO_DIR}/tools/chunks"
echo "=== OneDoc Tool Reassembly ==="
echo "[1/2] Reassembling Gradle 8.10.2..."
mkdir -p "${INSTALL_DIR}/gradle"
cat "${CHUNKS_DIR}/gradle/"gradle.tar.part.* > /tmp/gradle.tar
tar -xf /tmp/gradle.tar -C "${INSTALL_DIR}/gradle" --strip-components=1
rm -f /tmp/gradle.tar
echo "[2/2] Reassembling Android SDK..."
if compgen -G "${CHUNKS_DIR}/android-sdk/"android-sdk.tar.part.* > /dev/null 2>&1; then
  mkdir -p "${INSTALL_DIR}/android-sdk"
  cat "${CHUNKS_DIR}/android-sdk/"android-sdk.tar.part.* > /tmp/android-sdk.tar
  tar -xf /tmp/android-sdk.tar -C "${INSTALL_DIR}/android-sdk" --strip-components=1
  rm -f /tmp/android-sdk.tar
  echo "export ANDROID_HOME=${INSTALL_DIR}/android-sdk"
  echo "export PATH=\${ANDROID_HOME}/platform-tools:\${PATH}"
else
  echo "Skipping Android SDK - no chunks found"
fi
echo "=== Reassembly Complete ==="
echo "export GRADLE_HOME=${INSTALL_DIR}/gradle/gradle-8.10.2"
echo "export PATH=\${GRADLE_HOME}/bin:\${PATH}"
