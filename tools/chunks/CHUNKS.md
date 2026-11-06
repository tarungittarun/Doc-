# OneDoc Tool Chunks

All build tools are split into 48MB chunks for git storage.
Reassemble with: bash tools/scripts/reassemble-tools.sh

| Tool | Chunks | Approx Size |
|------|--------|-------------|
| Java 17 (OpenJDK) | 7 | ~190MB |
| Gradle 8.10.2 | 4 | ~200MB |
| Android SDK 34 | 4 | ~500MB+ |

## Reassembly

```bash
bash tools/scripts/reassemble-tools.sh
```

Then set environment variables and build:

```bash
export JAVA_HOME=~/onedoc-tools/java17
export GRADLE_HOME=~/onedoc-tools/gradle/gradle-8.10.2
export ANDROID_HOME=~/onedoc-tools/android-sdk
export PATH=$JAVA_HOME/bin:$GRADLE_HOME/bin:$ANDROID_HOME/platform-tools:$PATH

cd /path/to/Doc-
./gradlew assembleRelease
```

## Pre-built APK

OneDoc-release.apk is committed to repo root. Install it directly without building.
