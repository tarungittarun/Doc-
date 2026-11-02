# OneDoc Android v1.0 — Release Report

## Build result: CI LANE GREEN ✅

CI run: https://github.com/tarungittarun/Doc-/actions/runs/41410262750  
Branch: `arena/db667982-doc` (commit c5761fd)

| Job | Result |
|-----|--------|
| Traceability check (`--check --strict`) | success |
| Build debug APKs | success |
| Build release APKs + static gates | success |
| CI lane green | success |

## APKs (GitHub Actions artifacts, 30-day retention)

Download from the CI run page above (Artifacts section):

- `onedoc-debug-apks` — debug APKs (~10 MB)
- `onedoc-release-apks` — release APK (~7.5 MB)

Direct binary download is not available from this sandbox (Azure Blob Storage egress is blocked); artifacts are fetchable via the GitHub Actions web UI at the run URL above.

## Build configuration

- compileSdk / targetSdk: 34
- AGP: 8.5.0 | Kotlin: 2.0.20 | Compose BOM: 2024.06.00 | Compose compiler: 1.6.10
- Gradle: 8.10.2 (JDK Temurin 17), `org.gradle.jvmargs=-Xmx3072m`
- applicationId: `app.onedoc` | versionCode: 1 | versionName: 1.0.0
- Release: minification disabled, signed with debug keystore, `buildKotlinToolingMetadata` disabled
- No `android.permission.INTERNET` / `ACCESS_NETWORK_STATE` (static gate passed)

## What was blocking the release build (resolved)

1. `ModuleVersionNotFoundException: com.sun.activation:javax.activation:1.2.0` — AGP 8.5.0 → `com.android.tools:repository:31.5.0` → `jimfs:1.1` requires a non-existent artifact. Fixed with `resolutionStrategy { force("com.sun.activation:javax.activation:1.1.1") }` in `app/build.gradle.kts`.
2. `lintVitalRelease` fatal lint errors in `app/src/main/res/xml/data_extraction_rules.xml` (`shared_pref` is not a valid backup domain; invalid `path="."` exclude). Fixed by removing the invalid exclude rule.
3. Traceability `--check --strict` passes on all runs; PR (fast) lane passes on all runs.
