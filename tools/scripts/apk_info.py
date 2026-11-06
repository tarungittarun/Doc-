#!/usr/bin/env python3
"""OneDoc APK information tool - works without Java/apktool.

Reads APK metadata directly from the ZIP structure.
"""

import sys
import struct
import zipfile
import re
from pathlib import Path


def read_apk_info(apk_path):
    """Extract basic info from APK without Java tools."""
    apk = Path(apk_path)
    if not apk.exists():
        return {"error": f"APK not found: {apk_path}"}

    info = {
        "path": str(apk),
        "size": apk.stat().st_size,
        "is_zip": False,
    }

    try:
        with zipfile.ZipFile(apk, 'r') as z:
            info["is_zip"] = True
            names = z.namelist()

            # Check for AndroidManifest.xml
            if "AndroidManifest.xml" in names:
                manifest = z.read("AndroidManifest.xml")
                info["manifest_size"] = len(manifest)
                # Extract package name from binary XML (simplified)
                pkg_match = re.search(rb'package="([^"]+)"', manifest)
                if pkg_match:
                    info["package"] = pkg_match.group(1).decode('utf-8', errors='replace')

                # Extract version info
                ver_match = re.search(rb'versionCode="(\d+)"', manifest)
                if ver_match:
                    info["version_code"] = int(ver_match.group(1))
                ver_match = re.search(rb'versionName="([^"]+)"', manifest)
                if ver_match:
                    info["version_name"] = ver_match.group(1).decode('utf-8', errors='replace')

            # List key files
            info["files"] = {
                "total": len(names),
                "dex_files": [n for n in names if n.endswith('.dex')],
                "so_files": [n for n in names if n.endswith('.so')],
                "resources": [n for n in names if 'resources' in n.lower()],
                "assets": [n for n in names if n.startswith('assets/')],
            }

            # Check for signatures
            sig_files = [n for n in names if 'META-INF' in n and n.endswith(('.RSA', '.DSA', '.EC'))]
            info["signed"] = len(sig_files) > 0
            info["signature_files"] = sig_files[:5]

    except zipfile.BadZipFile:
        info["error"] = "Not a valid ZIP/APK file"

    return info


def main():
    apk_path = sys.argv[1] if len(sys.argv) > 1 else "OneDoc-release.apk"
    info = read_apk_info(apk_path)

    if "error" in info:
        print(f"ERROR: {info['error']}")
        sys.exit(1)

    print("=" * 50)
    print("OneDoc APK Information")
    print("=" * 50)
    print(f"Path:         {info['path']}")
    print(f"Size:         {info['size']:,} bytes ({info['size'] / 1024 / 1024:.2f} MB)")
    print(f"Valid ZIP:    {info['is_zip']}")
    print(f"Signed:       {info.get('signed', False)}")
    if info.get('package'):
        print(f"Package:      {info['package']}")
    if info.get('version_code'):
        print(f"Version Code: {info['version_code']}")
    if info.get('version_name'):
        print(f"Version Name: {info['version_name']}")
    print()
    print("Contents:")
    files = info.get('files', {})
    print(f"  Total files:     {files.get('total', 0)}")
    print(f"  DEX files:       {len(files.get('dex_files', []))}")
    print(f"  Native libs:     {len(files.get('so_files', []))}")
    print(f"  Resource files:  {len(files.get('resources', []))}")
    print(f"  Asset files:     {len(files.get('assets', []))}")
    if info.get('signature_files'):
        print(f"  Signature files: {info['signature_files']}")
    print("=" * 50)


if __name__ == "__main__":
    main()
