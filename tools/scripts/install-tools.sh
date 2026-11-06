#!/bin/bash
# OneDoc Local Tool Installer
# Downloads and sets up Java 17, Android SDK 34, Gradle 8.10.2
# Run: bash tools/scripts/install-tools.sh

set -e

INSTALL_DIR="${HOME}/onedoc-tools"
mkdir -p "${INSTALL_DIR}"

echo "=== OneDoc Local Tool Installer ==="
echo "Install directory: ${INSTALL_DIR}"
echo ""

# ---------- Java 17 ----------
echo "[1/3] Downloading Java 17 (Eclipse Temurin 17.0.11+9)..."
JAVA_URL="https://api.adoptium.net/v3/binary/version/17.0.11+9/linux/x64/jdk/hotspot/normal/eclipse?project=jdk"
curl -fsSL -o "${INSTALL_DIR}/openjdk-17.tar.gz" "${JAVA_URL}"
echo "Extracting Java 17..."
mkdir -p "${INSTALL_DIR}/java17"
tar -xzf "${INSTALL_DIR}/openjdk-17.tar.gz" -C "${INSTALL_DIR}/java17" --strip-components=1
rm -f "${INSTALL_DIR}/openjdk-17.tar.gz"
echo "Java 17 installed at: ${INSTALL_DIR}/java17"
echo ""

# ---------- Gradle 8.10.2 ----------
echo "[2/3] Downloading Gradle 8.10.2..."
GRADLE_URL="https://services.gradle.org/distributions/gradle-8.10.2-bin.zip"
curl -fsSL -o "${INSTALL_DIR}/gradle-8.10.2-bin.zip" "${GRADLE_URL}"
echo "Extracting Gradle 8.10.2..."
mkdir -p "${INSTALL_DIR}/gradle"
unzip -q "${INSTALL_DIR}/gradle-8.10.2-bin.zip" -d "${INSTALL_DIR}/gradle"
rm -f "${INSTALL_DIR}/gradle-8.10.2-bin.zip"
echo "Gradle 8.10.2 installed at: ${INSTALL_DIR}/gradle"
echo ""

# ---------- Android SDK ----------
echo "[3/3] Downloading Android SDK command-line tools..."
SDK_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
curl -fsSL -o "${INSTALL_DIR}/cmdline-tools.zip" "${SDK_URL}"
echo "Extracting Android SDK command-line tools..."
mkdir -p "${INSTALL_DIR}/android-sdk/cmdline-tools"
unzip -q "${INSTALL_DIR}/cmdline-tools.zip" -d "${INSTALL_DIR}/android-sdk/cmdline-tools"
rm -f "${INSTALL_DIR}/cmdline-tools.zip"
# cmdline-tools needs a 'latest' subdirectory for sdkmanager
mkdir -p "${INSTALL_DIR}/android-sdk/cmdline-tools/latest"
mv "${INSTALL_DIR}/android-sdk/cmdline-tools/bin" "${INSTALL_DIR}/android-sdk/cmdline-tools/latest/" 2>/dev/null || true
mv "${INSTALL_DIR}/android-sdk/cmdline-tools/lib" "${INSTALL_DIR}/android-sdk/cmdline-tools/latest/" 2>/dev/null || true
# Accept licenses and install SDK 34
echo "y" | "${INSTALL_DIR}/android-sdk/cmdline-tools/latest/bin/sdkmanager" --sdk_root="${INSTALL_DIR}/android-sdk" --licenses > /dev/null 2>&1 || true
"${INSTALL_DIR}/android-sdk/cmdline-tools/latest/bin/sdkmanager" --sdk_root="${INSTALL_DIR}/android-sdk" "platform-tools" "platforms;android-34" "build-tools;34.0.0"
echo "Android SDK 34 installed at: ${INSTALL_DIR}/android-sdk"
echo ""

echo "=== Installation Complete ==="
echo "Add these to your ~/.bashrc or ~/.profile:"
echo "export JAVA_HOME=${INSTALL_DIR}/java17"
echo "export ANDROID_HOME=${INSTALL_DIR}/android-sdk"
echo "export ANDROID_SDK_ROOT=\${ANDROID_HOME}"
echo "export GRADLE_HOME=${INSTALL_DIR}/gradle/gradle-8.10.2"
echo "export PATH=\${JAVA_HOME}/bin:\${GRADLE_HOME}/bin:\${ANDROID_HOME}/platform-tools:\${PATH}"
echo ""
echo "Then build OneDoc:"
echo "  cd /path/to/Doc-"
echo "  ./gradlew assembleRelease"
