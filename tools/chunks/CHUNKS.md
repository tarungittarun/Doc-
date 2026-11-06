# OneDoc Tool Chunks

Build tools are split into 48MB chunks for git storage.
Reassemble with: bash tools/scripts/reassemble-tools.sh

| Tool | Chunks | Approx Size |
|------|--------|-------------|
| Gradle 8.10.2 | 4 | ~200MB |
| Android SDK 34 | 0 | ~500MB+ |

## Reassembly

=== OneDoc Tool Reassembly ===
[1/2] Reassembling Gradle 8.10.2...
[2/2] Reassembling Android SDK...
Skipping Android SDK - no chunks found
=== Reassembly Complete ===
export GRADLE_HOME=/home/runner/onedoc-tools/gradle/gradle-8.10.2
export PATH=${GRADLE_HOME}/bin:${PATH}

Then set environment variables and build:



## Pre-built APK

OneDoc-release.apk is committed to repo root.
