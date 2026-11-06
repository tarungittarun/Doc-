# OneDoc Build Tools

Tool versions and download URLs used by CI.
Run `tools/scripts/install-tools.sh` to install locally.

## Tool Versions

| Tool | Version | Download URL |
|------|---------|--------------|
| Java (JDK) | 17.0.11+9 (Eclipse Temurin) | https://api.adoptium.net/v3/binary/version/17.0.11+9/linux/x64/jdk/hotspot/normal/eclipse?project=jdk |
| Android SDK | command-line-tools 11076708 | https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip |
| Gradle | 8.10.2 | https://services.gradle.org/distributions/gradle-8.10.2-bin.zip |

## Local Installation

```bash
bash tools/scripts/install-tools.sh
```

Then set environment variables (shown at end of installer output) and build:

```bash
export JAVA_HOME=~/onedoc-tools/java17
export ANDROID_HOME=~/onedoc-tools/android-sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
export GRADLE_HOME=~/onedoc-tools/gradle/gradle-8.10.2
export PATH=$JAVA_HOME/bin:$GRADLE_HOME/bin:$ANDROID_HOME/platform-tools:$PATH

cd /path/to/Doc-
./gradlew assembleRelease
```

## Pre-built APK

A pre-built release APK is already committed to the repo root:
`OneDoc-release.apk` (versionCode 6, versionName 1.0.5)

Install it on your device after uninstalling v1.0.4 once.
