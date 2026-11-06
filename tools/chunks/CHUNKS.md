# OneDoc Tool Chunks

Build tools are split into 48MB chunks for git storage.
Reassemble with: bash tools/scripts/reassemble-tools.sh

| Tool | Chunks | Approx Size |
|------|--------|-------------|
| Gradle 8.10.2 | 4 | ~200MB |

## Reassembly

=== OneDoc Tool Reassembly ===
[1/2] Reassembling Gradle 8.10.2...
=== Reassembly Complete ===
export GRADLE_HOME=/home/runner/onedoc-tools/gradle/gradle-8.10.2
export PATH=/usr/share/gradle-9.8.2/bin:/snap/bin:/home/runner/.local/bin:/opt/pipx_bin:/home/runner/.cargo/bin:/home/runner/.config/composer/vendor/bin:/usr/local/.ghcup/bin:/home/runner/.dotnet/tools:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games:/snap/bin

Then set environment variables and build:



## Pre-built APK

OneDoc-release.apk is committed to repo root.
