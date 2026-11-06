# OneDoc Sandbox Development Setup

This document describes how to develop and test OneDoc in environments where
**Java is not available** (e.g., this sandbox with glibc 2.36, where Java 17+
requires glibc 2.38+).

## Environment Limitations

| Tool | Status | Reason |
|------|--------|--------|
| Java 17+ | ❌ Not available | Requires glibc 2.38+, sandbox has 2.36 |
| Gradle | ❌ Requires Java | Cannot run without Java |
| Android SDK tools | ❌ Requires Java | sdkmanager, etc. need Java |
| **Python 3.11** | ✅ Available | Works natively |
| **Node.js 22** | ✅ Available | Works natively |
| **APK installation** | ✅ Works | Pre-built APK committed to repo |

## What You CAN Do in the Sandbox

### 1. Edit Source Code
All Kotlin source files are in `app/src/main/java/`. Edit them with any text editor.

### 2. Validate Project Structure
```bash
python3 tools/scripts/validate_project.py
```
Checks: file existence, Kotlin syntax basics, APK integrity, diagnostics config.

### 3. Inspect APK Without Java
```bash
python3 tools/scripts/apk_info.py OneDoc-release.apk
```
Extracts package name, version code/name, file counts, signature status — all from the ZIP structure.

### 4. Run Diagnostics Server Locally
```bash
python3 tools/scripts/diagnostics_server.py 8765
```
Then open http://localhost:8765 in your browser to view/manage diagnostics logs.

### 5. Install and Test the APK
The pre-built release APK is committed to the repo:
- **File:** `OneDoc-release.apk` (versionCode 6, versionName 1.0.5)
- **Install:** `adb install OneDoc-release.apk` (if adb available) or transfer to device manually
- **Important:** Uninstall v1.0.4 once, then install v1.0.5 fresh. After that, same-app updates work.

### 6. Build the APK (CI or Local with Java)
- **CI:** Push to `arena/db667982-doc` — GitHub Actions builds automatically
- **Local:** Requires Java 17 + Gradle 8.10.2 + Android SDK 34 on a machine with glibc 2.38+

## Sandbox-Friendly Workflow

```bash
# 1. Clone the repo
git clone -b arena/db667982-doc https://github.com/tarungittarun/Doc-.git
cd Doc-

# 2. Validate everything works
python3 tools/scripts/validate_project.py

# 3. View APK info
python3 tools/scripts/apk_info.py OneDoc-release.apk

# 4. Start diagnostics server (optional)
python3 tools/scripts/diagnostics_server.py 8765

# 5. Edit Kotlin files in app/src/main/java/
#    (No compilation possible in sandbox — validate syntax manually)

# 6. Commit changes — CI will build and test
git add .
git commit -m "Your changes"
git push origin arena/db667982-doc
```

## File Structure for Sandbox Development

```
tools/scripts/
├── validate_project.py    # Project validation (no Java needed)
├── apk_info.py            # APK metadata reader (no Java needed)
├── diagnostics_server.py  # Local HTTP server for diagnostics
├── reassemble-tools.sh    # Reassemble Gradle/Java/Android SDK from chunks
└── install-tools.sh       # Original installer (requires Java-capable machine)

tools/chunks/
├── java17/                # Java 17 chunks (for CI/local with glibc 2.38+)
├── gradle/                # Gradle 8.10.2 chunks
├── android-sdk/           # Android SDK 34 chunks
└── CHUNKS.md              # Chunk manifest

.github/workflows/
├── build.yml              # CI build (runs on Ubuntu 24.04 with Java 17)
├── java-chunks.yml        # Java 17 chunking workflow
├── java8-chunks.yml       # Java 8 chunking workflow (attempted)
└── ...                    # Other CI workflows
```

## Key Files

| File | Purpose | Works in Sandbox |
|------|---------|-------------------|
| `OneDoc-release.apk` | Pre-built signed APK | ✅ Install & test |
| `debug.keystore` | Signing keystore | ✅ For CI builds |
| `app/src/main/java/` | Kotlin source | ✅ Edit & review |
| `tools/scripts/validate_project.py` | Validation tool | ✅ Run locally |
| `tools/scripts/apk_info.py` | APK info tool | ✅ Run locally |
| `tools/scripts/diagnostics_server.py` | Diagnostics HTTP server | ✅ Run locally |

## CI Build Pipeline

When you push to `arena/db667982-doc`, GitHub Actions:

1. Downloads Java 17 + Gradle 8.10.2 + Android SDK 34
2. Builds debug and release APKs
3. Runs instrumentation tests on emulator
4. Commits the release APK back to the repo

You don't need Java locally — CI handles the build.
