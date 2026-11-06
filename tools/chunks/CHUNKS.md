# OneDoc Tool Chunks

Build tools are split into 48MB chunks for git storage.
Reassemble with: bash tools/scripts/reassemble-tools.sh

| Tool | Chunks | Approx Size |
|------|--------|-------------|
| Gradle 8.10.2 | 4 | ~200MB |

## Reassembly

=== OneDoc Tool Reassembly ===
Reassembling Gradle 8.10.2...
=== Reassembly Complete ===
export GRADLE_HOME=/home/runner/onedoc-tools/gradle/gradle-8.10.2
export PATH=${GRADLE_HOME}/bin:${PATH}

Also install Android SDK 34 via: tools/scripts/install-tools.sh
Then: export ANDROID_HOME=$HOME/onedoc-tools/android-sdk

Then set environment variables and build:



Android SDK 34 can be installed separately via tools/scripts/install-tools.sh.

## Pre-built APK

OneDoc-release.apk is committed to repo root.
