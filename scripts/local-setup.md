# OneDoc Local Development Setup

This file documents the toolchain used by the GitHub Actions CI runners so you
can replicate the same environment locally for building and testing OneDoc.

## Required Tools

### Java (JDK 17)
- Install: https://adoptium.net/ (Temurin JDK 17)
- Or: `sudo apt-get install openjdk-17-jdk`
- Verify: `java -version` and `keytool -version`

### keytool
- Comes bundled with JDK 17
- Used for signing the release APK with the committed `debug.keystore`

### Android SDK
- Install: Android Studio or command-line tools
- URL: https://developer.android.com/studio
- Required SDK packages:
  - platform-tools
  - platforms;android-34
  - build-tools;34.0.0
- Set environment variables:
  ```bash
  export ANDROID_HOME=$HOME/Android/Sdk
  export ANDROID_SDK_ROOT=$ANDROID_HOME
  export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
  export PATH=$PATH:$ANDROID_HOME/platform-tools
  ```

### Gradle 8.10.2
- Install: https://gradle.org/releases/
- Or: `sudo apt-get install gradle`
- The repo also includes `gradlew` (Gradle wrapper)

## Build Commands

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (signed with committed debug.keystore)
./gradlew assembleRelease
```

## OneDoc-release.apk

A pre-built release APK is committed to the repo root:

```bash
git pull origin arena/db667982-doc
ls -la OneDoc-release.apk
sha256sum OneDoc-release.apk
```

Install it on your physical device after uninstalling the existing v1.0.4 once.

## Notes

- `debug.keystore` is committed to the repo for same-app updates
- appId: `app.onedoc`
- versionCode: 6, versionName: 1.0.5
- CI workflows: `.github/workflows/build.yml`, `.github/workflows/capture-env.yml`,
  `.github/workflows/onedoc-release.yml`, `.github/workflows/pr.yml`
