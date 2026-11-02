# OneDoc Android v1.0.1 — Release Report (Crash Fix + Update Support)

## Build result: CI LANE GREEN ✅

CI run: https://github.com/tarungittarun/Doc-/actions/runs/41413811233  
Branch: `arena/db667982-doc` (commit f3fe18c)

| Job | Result |
|-----|--------|
| Traceability check (`--check --strict`) | success |
| Build debug APKs | success |
| Build release APKs + static gates | success |
| CI lane green | success |

## APK download links

Download from the CI run page above → **Artifacts** section:

- `onedoc-release-apks` — release APK (~7.5 MB) — install this
- `onedoc-debug-apks` — debug APKs (~10 MB)

Direct binary download is not available from this sandbox (Azure Blob Storage egress is blocked); artifacts are fetchable via the GitHub Actions web UI at the run URL above.

## What was fixed (crash on launch)

The app was crashing immediately on launch ("OneDoc keeps stopping"). Root cause: **Room database schema validation failure** during app startup.

When `HomeActivity.onCreate()` runs, `DocumentRepository.getInstance()` is lazily initialized, which calls `Room.databaseBuilder().build()`. This validates the entity schema, including indexes. `DocumentEntity` had:

```kotlin
Index(value = ["updatedAt"], name = "idx_documents_updatedAt"),
```

But the actual column name is `updated_at` (via `@ColumnInfo(name = "updated_at")`). Room rejected the mismatch, throwing a fatal error during database initialization — crashing the app before the UI could appear.

**Fix:** Changed index value to `["updated_at"]` to match the `@ColumnInfo` column name.

Also fixed:
- `SettingsActivity.kt`: Removed `containerColor = Color.Transparent` from `ListItemDefaults.colors()` (API compatibility issue)
- Bumped `versionCode` from 1 → **2**, `versionName` from `1.0.0` → `1.0.1`

## Same-app update support (no uninstall required)

- `applicationId`: `app.onedoc` — unchanged ✅
- Signing key: same debug keystore used for all CI release builds ✅
- `versionCode`: **2** (was 1) — higher, so updates install over existing app ✅

Users can install v1.0.1 over v1.0.0 without uninstalling. Future releases just need `versionCode` incremented and the same signing key.

## Build configuration

- compileSdk / targetSdk: 34
- AGP: 8.5.0 | Kotlin: 2.0.20 | Compose BOM: 2024.06.00 | Compose compiler: 1.6.10
- Gradle: 8.10.2 (JDK Temurin 17), `org.gradle.jvmargs=-Xmx3072m`
- Release: minification disabled, signed with debug keystore, `buildKotlinToolingMetadata` disabled
- No `android.permission.INTERNET` / `ACCESS_NETWORK_STATE` (static gate passed)
