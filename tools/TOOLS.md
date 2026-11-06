# OneDoc Development Tools

Two environments are supported: **CI (with Java)** and **Sandbox (without Java)**.

## Environment Compatibility

| Tool | CI (Ubuntu 24.04) | Sandbox (glibc 2.36) | Notes |
|------|-------------------|---------------------|-------|
| Java 17 | ✅ Available | ❌ Needs glibc 2.38+ | CI only |
| Gradle 8.10.2 | ✅ Available | ❌ Requires Java | CI only |
| Android SDK 34 | ✅ Available | ❌ Requires Java | CI only |
| Python 3.11 | ✅ Available | ✅ Available | Both |
| Node.js 22 | ✅ Available | ✅ Available | Both |
| APK installation | ✅ CI builds | ✅ Pre-built APK | Both |

## CI Build Tools (for building APKs)

These are chunked and committed to `tools/chunks/` for local reassembly on Java-capable machines:

| Tool | Version | Chunks | Approx Size |
|------|---------|--------|-------------|
| Java 17 (Temurin) | 17.0.11+9 | 7 | ~190 MB |
| Gradle | 8.10.2 | 4 | ~200 MB |
| Android SDK | 34 | 4 | ~500 MB+ |

Reassemble with: `bash tools/scripts/reassemble-tools.sh`

## Sandbox Development Tools (no Java needed)

All scripts run natively with Python 3.11:

| Script | Purpose | Command |
|--------|---------|---------|
| `validate_project.py` | Validate project structure & Kotlin syntax | `python3 tools/scripts/validate_project.py` |
| `apk_info.py` | Read APK metadata without Java | `python3 tools/scripts/apk_info.py OneDoc-release.apk` |
| `diagnostics_server.py` | Local HTTP server for diagnostics logs | `python3 tools/scripts/diagnostics_server.py 8765` |

## Pre-built APK

`OneDoc-release.apk` is committed to repo root (versionCode 6, versionName 1.0.5).

Install on device:
1. Uninstall v1.0.4 once (one-time only)
2. Install v1.0.5 fresh
3. Same-app updates work after that

## CI Workflows

| Workflow | Purpose |
|----------|---------|
| `build.yml` | Build debug + release APKs, run emulator tests |
| `java-chunks.yml` | Chunk Java 17 for local reassembly |
| `java8-chunks.yml` | Java 8 chunking (attempted, glibc compatibility) |
| `tools-chunks.yml` | Chunk Gradle + Android SDK |
| `capture-env.yml` | Capture CI runner environment |
| `pr.yml` | PR validation lane |
| `minimal-test.yml` | Minimal connectivity test |
