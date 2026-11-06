# OneDoc Tool Chunks

Build tools are split into 48MB chunks for git storage.

## Important: Sandbox Compatibility

| Tool | Version | Chunks | Works in Sandbox? | Reason |
|------|---------|--------|-------------------|--------|
| Java 17 | 17.0.11+9 | 7 | ❌ No | Needs glibc 2.38+, sandbox has 2.36 |
| Gradle 8.10.2 | 8.10.2 | 4 | ❌ No | Requires Java |
| Android SDK 34 | 34 | 4 | ❌ No | Requires Java |

Use CI (GitHub Actions) to build APKs. The sandbox is for code editing and validation only.

## Reassembly

```bash
bash tools/scripts/reassemble-tools.sh
```

Extracted tools need Java to function. For sandbox development, use the Python tools:

```bash
python3 tools/scripts/validate_project.py
python3 tools/scripts/apk_info.py OneDoc-release.apk
python3 tools/scripts/diagnostics_server.py 8765
```

## Pre-built APK

`OneDoc-release.apk` is committed to repo root. Install directly on device without building.
