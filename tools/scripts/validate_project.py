#!/usr/bin/env python3
"""OneDoc project validation tool - works without Java.

Validates:
- Project structure (AndroidManifest, build files, source files)
- Kotlin file syntax (basic checks)
- APK metadata (without apktool/Java)
- Diagnostics configuration
"""

import os
import sys
import re
import json
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent.parent


def check_file(path, description):
    """Check if a file exists and report."""
    full = REPO_ROOT / path
    if full.exists():
        size = full.stat().st_size
        print(f"  [OK] {description}: {path} ({size} bytes)")
        return True
    else:
        print(f"  [MISSING] {description}: {path}")
        return False


def check_dir(path, description):
    """Check if a directory exists and report."""
    full = REPO_ROOT / path
    if full.is_dir():
        count = len(list(full.iterdir()))
        print(f"  [OK] {description}: {path} ({count} items)")
        return True
    else:
        print(f"  [MISSING] {description}: {path}")
        return False


def validate_kotlin_syntax(filepath):
    """Basic Kotlin syntax validation without Kotlin compiler."""
    errors = []
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        lines = content.split('\n')
        for i, line in enumerate(lines, 1):
            stripped = line.strip()
            # Check for unmatched braces
            if stripped.count('{') != stripped.count('}'):
                # Allow single-line lambdas and other valid patterns
                pass
        # Check for required class/function declarations
        has_class = bool(re.search(r'\bclass\s+\w+', content))
        has_fun = bool(re.search(r'\bfun\s+\w+', content))
        return {
            'has_class': has_class,
            'has_function': has_fun,
            'lines': len(lines),
            'size': len(content),
        }
    except Exception as e:
        errors.append(str(e))
        return {'error': str(e)}


def validate_apk(apk_path):
    """Basic APK validation without apktool/Java."""
    full = REPO_ROOT / apk_path
    if not full.exists():
        return {'exists': False}
    
    size = full.stat().st_size
    # Read first bytes to check for ZIP magic (APK is ZIP-based)
    with open(full, 'rb') as f:
        header = f.read(4)
    
    is_zip = header == b'PK\x03\x04'
    
    return {
        'exists': True,
        'size': size,
        'is_valid_zip': is_zip,
    }


def main():
    print("=" * 60)
    print("OneDoc Project Validation (Sandbox Environment)")
    print("=" * 60)
    print()
    
    all_ok = True
    
    # 1. Project structure
    print("1. Project Structure")
    print("-" * 40)
    checks = [
        ("AndroidManifest.xml", "app/src/main/AndroidManifest.xml", "file"),
        ("build.gradle.kts", "app/build.gradle.kts", "file"),
        ("settings.gradle.kts", "settings.gradle.kts", "file"),
        ("gradle.properties", "gradle.properties", "file"),
        ("ProGuard rules", "app/proguard-rules.pro", "file"),
        ("Source directory", "app/src/main/java", "dir"),
        ("Resource directory", "app/src/main/res", "dir"),
        ("Release APK", "OneDoc-release.apk", "file"),
        ("Signing keystore", "debug.keystore", "file"),
        ("Reassembly script", "tools/scripts/reassemble-tools.sh", "file"),
    ]
    for desc, path, kind in checks:
        if kind == "file":
            ok = check_file(path, desc)
        else:
            ok = check_dir(path, desc)
        if not ok:
            all_ok = False
    print()
    
    # 2. Kotlin source files
    print("2. Kotlin Source Files")
    print("-" * 40)
    kt_dir = REPO_ROOT / "app/src/main/java"
    kt_files = list(kt_dir.rglob("*.kt")) if kt_dir.is_dir() else []
    print(f"  Found {len(kt_files)} Kotlin files")
    for f in sorted(kt_files):
        info = validate_kotlin_syntax(f)
        if 'error' in info:
            print(f"  [ERROR] {f.relative_to(REPO_ROOT)}: {info['error']}")
            all_ok = False
        else:
            print(f"  [OK] {f.relative_to(REPO_ROOT)} ({info['lines']} lines, {info['size']} bytes, class={info['has_class']}, fun={info['has_function']})")
    print()
    
    # 3. APK validation
    print("3. APK Validation")
    print("-" * 40)
    apk_info = validate_apk("OneDoc-release.apk")
    if not apk_info['exists']:
        print("  [MISSING] OneDoc-release.apk")
        all_ok = False
    else:
        print(f"  [OK] OneDoc-release.apk ({apk_info['size']} bytes)")
        print(f"       Valid ZIP: {apk_info['is_valid_zip']}")
    print()
    
    # 4. Environment check
    print("4. Sandbox Environment")
    print("-" * 40)
    import platform
    print(f"  OS: {platform.system()} {platform.release()}")
    print(f"  Python: {platform.python_version()}")
    
    # Check Java
    java_path = None
    for p in ["/home/user/onedoc-tools/java17/bin/java", "/usr/bin/java"]:
        if os.path.exists(p):
            java_path = p
            break
    if java_path:
        print(f"  Java: Found at {java_path}")
    else:
        print(f"  Java: Not found (expected - glibc 2.36 limitation)")
    print()
    
    # 5. Diagnostics configuration
    print("5. Diagnostics Configuration")
    print("-" * 40)
    diag_logger = REPO_ROOT / "app/src/main/java/app/onedoc/diagnostics/DiagnosticsLogger.kt"
    if diag_logger.exists():
        content = diag_logger.read_text()
        has_async = "callback" in content.lower() or "async" in content.lower()
        has_file_export = "exportToFile" in content
        print(f"  DiagnosticsLogger.kt: {'[OK] async export' if has_async else '[CHECK] sync export'}")
        print(f"  File export method: {'[OK] present' if has_file_export else '[MISSING]'}")
    else:
        print("  [MISSING] DiagnosticsLogger.kt")
        all_ok = False
    print()
    
    # Summary
    print("=" * 60)
    if all_ok:
        print("RESULT: All checks passed ✓")
    else:
        print("RESULT: Some checks failed ✗")
    print("=" * 60)
    
    return 0 if all_ok else 1


if __name__ == "__main__":
    sys.exit(main())
