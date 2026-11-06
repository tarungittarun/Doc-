#!/bin/bash
# OneDoc Tool Reassembly Script
#
# Reassembles chunked build tools from tools/chunks/ into usable directories.
#
# NOTE: Java 17 chunks require glibc 2.38+. This sandbox has glibc 2.36,
# so Java 17 will NOT run here. Gradle and Android SDK will extract but
# need Java to function.
#
# Use CI (GitHub Actions) to build APKs without local Java.

shopt -s nullglob
INSTALL_DIR="${HOME}/onedoc-tools"
REPO_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
CHUNKS_DIR="${REPO_DIR}/tools/chunks"

echo "=== OneDoc Tool Reassembly ==="
echo "Sandbox glibc: $(ldd --version 2>&1 | head -1 | grep -o '[0-9]\+\.[0-9]\+')"
echo ""

JAVA_CHUNKS=("${CHUNKS_DIR}"/java17/java17.tar.part.*)
if [ ${#JAVA_CHUNKS[@]} -gt 0 ]; then
  echo "[1/3] Java 17..."
  mkdir -p "${INSTALL_DIR}/java17"
  cat "${JAVA_CHUNKS[@]}" > /tmp/java17.tar
  tar -xf /tmp/java17.tar -C "${INSTALL_DIR}/java17" --strip-components=1
  rm -f /tmp/java17.tar
  echo "  Extracted to ${INSTALL_DIR}/java17"
  echo "  WARNING: Requires glibc 2.38+ to run. Sandbox has 2.36."
  echo "  Use CI for builds, or install Java locally on a compatible machine."
else
  echo "[1/3] Java 17 - no chunks found, skipping"
fi

GRADLE_CHUNKS=("${CHUNKS_DIR}"/gradle/gradle.tar.part.*)
if [ ${#GRADLE_CHUNKS[@]} -gt 0 ]; then
  echo "[2/3] Gradle 8.10.2..."
  mkdir -p "${INSTALL_DIR}/gradle"
  cat "${GRADLE_CHUNKS[@]}" > /tmp/gradle.tar
  tar -xf /tmp/gradle.tar -C "${INSTALL_DIR}/gradle" --strip-components=1
  rm -f /tmp/gradle.tar
  echo "  Extracted to ${INSTALL_DIR}/gradle"
  echo "  NOTE: Gradle needs Java to run. Use CI for builds."
else
  echo "[2/3] Gradle 8.10.2 - no chunks found, skipping"
fi

ANDROID_CHUNKS=("${CHUNKS_DIR}"/android-sdk/android-sdk.tar.part.*)
if [ ${#ANDROID_CHUNKS[@]} -gt 0 ]; then
  echo "[3/3] Android SDK..."
  mkdir -p "${INSTALL_DIR}/android-sdk"
  cat "${ANDROID_CHUNKS[@]}" > /tmp/android-sdk.tar
  tar -xf /tmp/android-sdk.tar -C "${INSTALL_DIR}/android-sdk" --strip-components=1
  rm -f /tmp/android-sdk.tar
  echo "  Extracted to ${INSTALL_DIR}/android-sdk"
  echo "  NOTE: SDK tools need Java to run. Use CI for builds."
else
  echo "[3/3] Android SDK - no chunks found, skipping"
fi

echo ""
echo "=== Reassembly Complete ==="
echo ""
echo "Sandbox Development (no Java needed):"
echo "  python3 tools/scripts/validate_project.py"
echo "  python3 tools/scripts/apk_info.py OneDoc-release.apk"
echo "  python3 tools/scripts/diagnostics_server.py 8765"
echo ""
echo "CI Build (push to trigger):"
echo "  git push origin arena/db667982-doc"
echo ""
echo "Local Build (Java-capable machine, glibc 2.38+):"
echo "  export JAVA_HOME=${INSTALL_DIR}/java17"
echo "  export GRADLE_HOME=${INSTALL_DIR}/gradle"
echo "  export ANDROID_HOME=${INSTALL_DIR}/android-sdk"
echo "  export PATH=\${JAVA_HOME}/bin:\${GRADLE_HOME}/bin:\${ANDROID_HOME}/platform-tools:\${PATH}"
echo "  ./gradlew assembleRelease"
