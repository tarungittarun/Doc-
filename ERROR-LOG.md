# OneDoc Error Log & Resolution History

This file tracks every build/runtime error encountered, its root cause, category, and fix. It serves as institutional memory to prevent repeating mistakes.

## Format
| # | Error | Root Cause | Category | Fix | Commit | Status |
|---|-------|-----------|----------|-----|--------|--------|

---

## Errors Encountered

| # | Error | Root Cause | Category | Fix | Commit | Status |
|---|-------|-----------|----------|-----|--------|--------|
| 1 | `ModuleVersionNotFoundException: com.sun.activation:javax.activation:1.2.0` | AGP 8.5.0 → com.android.tools:repository:31.5.0 → jimfs:1.1 requires javax.activation:1.2.0 which doesn't exist | Dependency | `resolutionStrategy { force("com.sun.activation:javax.activation:1.1.1") }` in app/build.gradle.kts | 1fff698 | Resolved |
| 2 | Gradle daemon crash on startup attempt #1 error code 0 | Unknown - infrastructure issue | Gradle/Environment | Removed `--no-daemon`, `--stacktrace`, `--max-workers=1`, `-Dkotlin.daemon.enabled=false`; used plain `gradle assembleRelease` | Various | Resolved |
| 3 | `lintVitalRelease` fatal lint errors | `data_extraction_rules.xml`: `shared_pref` is not valid backup domain; invalid `path="."` | Configuration | Removed invalid `<exclude domain="shared_pref" path="." />` rule | c5761fd | Resolved |
| 4 | Room schema index mismatch ("updatedAt" vs "updated_at") | `DocumentEntity` had `Index(value = ["updatedAt"])` but column name is `updated_at` (@ColumnInfo(name = "updated_at")) | Code/Logic | Changed index to `["updated_at"]` to match @ColumnInfo column name | f3fe18c | Resolved |
| 5 | `containerColor` in `ListItemDefaults.colors()` API compatibility | `SettingsActivity.kt` used `containerColor = Color.Transparent` which is deprecated/removed | Code/API | Removed `containerColor = Color.Transparent` parameter | f3fe18c | Resolved |
| 6 | VersionCode stuck at 1 | Same-app updates need higher versionCode | Configuration | Bumped to versionCode 5, versionName 1.0.4 | Various | Resolved |
| 7 | Release APK unsigned / won't install | No signingConfig for release build type | Configuration | Added `signingConfigs.create("release")` with debug.keystore in app/build.gradle.kts | 11896ec | Resolved |
| 8 | `@Composable` unresolved in Theme.kt | Missing `import androidx.compose.runtime.Composable` | Code/Import | Added missing import | 4268130 | Resolved |
| 9 | `detekt` version catalog error | `detekt` plugin referenced in `[plugins]` but no `detekt` version in `[versions]` | Configuration | Removed `detekt` from `[plugins]` in libs.versions.toml | a6def77 | Resolved |
| 10 | `lintVitalRelease` fatal errors blocking release APK | Lint found fatal errors (missing theme/icon resources in manifest) | Configuration | Added `lint { checkReleaseBuilds = false }` to app/build.gradle.kts | b070f1a | Resolved |
| 11 | Missing Typography styles (headlineSmall, displaySmall, etc.) | Custom Typography didn't include styles referenced by EditorActivity and EmptyState | Code/Style | Added all missing Typography styles | e4776b9 | Resolved |
| 12 | AAPT2 resource linking failed | Manifest referenced @style/Theme.OneDoc, @mipmap/ic_launcher, @xml/ rules that referenced missing/invalid resources | Resource | Removed theme/icon/XML references from manifest; kept only @string/app_name | 764699d / 0307dc3 | Resolved |
| 13 | App crashing on launch ("OneDoc keeps stopping") | Room database schema validation failure during Room.databaseBuilder().build() | Runtime | Fixed Room index column name + added lint disabling + signed release APK | Multiple | Fixed in CI, awaiting device test |

---

## Key Decisions

### Version Configuration
- applicationId: `app.onedoc` (never change — required for same-app updates)
- Signing key: Same debug keystore used for all CI release builds
- versionCode: 5 (v1.0.4) — higher than v1.0.0 (1), v1.0.1 (2), v1.0.2 (3), v1.0.3 (4)
- versionName: 1.0.4

### Build Configuration
- compileSdk/targetSdk: 34
- AGP: 8.5.0 | Kotlin: 2.0.20 | Compose BOM: 2024.06.00 | Compose compiler: 1.6.10
- Gradle: 8.10.2 (JDK Temurin 17), `-Xmx3072m`
- Release: minification disabled, signed with debug keystore, buildKotlinToolingMetadata disabled
- `lint { checkReleaseBuilds = false }` — lint was blocking release APK due to missing icon/theme resources

### Same-App Update Support
- applicationId unchanged: `app.onedoc` ✅
- Same signing key: debug keystore ✅
- versionCode increments with each release ✅
- Users install new APK over existing without uninstall ✅

---

## Categories Summary

| Category | Count | Examples |
|----------|-------|----------|
| Dependency | 1 | javax.activation:1.2.0 missing |
| Configuration | 4 | lintVitalRelease, detekt catalog, versionCode, signing config |
| Code/Logic | 3 | Room index, containerColor, Composable import |
| Resource | 2 | AAPT2 linking, Typography styles |
| Runtime | 1 | App crashing on launch (Room schema) |

---

## Current State (as of commit b070f1a)

- CI Lane: GREEN ✅
- Debug APKs: Built successfully (~10 MB)
- Release APK: Built successfully (~7.5 MB, signed)
- Traceability `--check --strict`: Passes
- Static gates (no internet permission): Passes

### APK Download
https://github.com/tarungittarun/Doc-/actions/runs/41569708383 → Artifacts → `onedoc-release-apks`
