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
echo "=== Reassembly Complete ==="
echo "export GRADLE_HOME=${INSTALL_DIR}/gradle/gradle-8.10.2"
echo "export PATH=${GRADLE_HOME}/bin:${PATH}"
