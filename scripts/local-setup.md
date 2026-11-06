# OneDoc Local Development Setup

This file is auto-generated from CI runner environment.
Use it to install the same toolchain locally for building and testing OneDoc.

## Required Tools

### Java (JDK 17)
- Version: See ci-logs/capture-env/java-version.txt
- Install: https://adoptium.net/ (Temurin JDK 17)
- Or: sudo apt-get install openjdk-17-jdk

### keytool
- Comes with JDK 17
- Path: See ci-logs/capture-env/keytool-version.txt

### Android SDK
- Version: See ci-logs/capture-env/android-sdk.txt
- Install: Android Studio or command-line tools
- URL: https://developer.android.com/studio
- Required packages:
  - platform-tools
  - platforms;android-34
  - build-tools;34.0.0
- Set environment variables:
  export ANDROID_HOME=$HOME/Android/Sdk
  export ANDROID_SDK_ROOT=$ANDROID_HOME
  export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
  export PATH=$PATH:$ANDROID_HOME/platform-tools

### Gradle 8.10.2
- Version: See ci-logs/capture-env/gradle-version.txt
- Install: https://gradle.org/releases/
- Or: sudo apt-get install gradle

## Build Commands

# Debug APK
./gradlew assembleDebug

# Release APK (signed with committed debug.keystore)
./gradlew assembleRelease

## Notes

- debug.keystore is committed to the repo for same-app updates
- appId: app.onedoc
- versionCode: 6, versionName: 1.0.5
