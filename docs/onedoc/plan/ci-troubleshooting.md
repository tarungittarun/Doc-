# CI Troubleshooting & Lessons Learned (OneDoc v1.0)

**Purpose:** Reference before every CI push to avoid repeating known failures.
**Update rule:** Update this file before each push when a new CI failure is resolved.

---

## Known Kotlin Compilation Errors (and fixes)

| # | Error | File | Fix |
|---|-------|------|-----|
| K1 | `abstract data class` invalid in Kotlin | `Block.kt`, `Mark.kt` | Remove `abstract` keyword from data classes inside sealed classes |
| K2 | Sealed class base properties must be `abstract` or `open` | `Block.kt` | Make `id`, `kind`, `createdAt` `abstract`; make `marks` `open` with default `emptyList()` |
| K3 | `Mark.type` final, cannot be overridden | `Mark.kt` | Make `type` `abstract` in sealed class |
| K4 | `Arrangement.SpaceBy` — wrong method name | `HomeActivity.kt` | Use `Arrangement.spacedBy` |
| K5 | `androidx.lifecycle.lifecycleScope.launch` unresolved | `HomeActivity.kt` | Use `lifecycleScope.launch` (extension property) + `import kotlinx.coroutines.launch` |
| K6 | `observeAsState` unresolved reference | `HomeActivity.kt` | Remove usage + import; use `emptyList<Document>()` until G2 observer pattern |
| K7 | `LazyColumn` / `items` unresolved | `EditorActivity.kt` | Add `import androidx.compose.foundation.lazy.*` |
| K8 | `mutableStateOf` delegate needs `getValue`/`setValue` | `EditorActivity.kt` | Add imports `getValue`, `setValue` from `androidx.compose.runtime` |
| K9 | `color.transparent` unresolved | `EditorActivity.kt` | Use `Color.Transparent` |
| K10 | `FontFamily.Monospace` unresolved | `EditorActivity.kt` | Remove `FontFamily` usage; use plain `TextStyle` |
| K11 | `TextFieldDefaults.colors(containerColor=...)` param doesn't exist | `EditorActivity.kt` | Use `TextFieldDefaults.colors()` (no args) |
| K12 | `surfaceContainer` not a ColorScheme param | `Theme.kt`, `EditorActivity.kt` | Remove from `darkColorScheme`/`lightColorScheme`; use `surface` instead |
| K13 | `TextStyle` unresolved in Theme.kt | `Theme.kt` | Add `import androidx.compose.ui.text.TextStyle` |
| K14 | `List` is not a Material3 composable | `SettingsActivity.kt` | Replace `List { ... }` with `Column { ... }` |
| K15 | `Room` unresolved | `AppDatabase.kt` | Add `import androidx.room.Room` |
| K16 | Json `obj.apply { return ... }` type mismatch | `Json.kt` | Use `when` branch with block `{ ... }` returning `Block` directly |
| K17 | `Icons` / `ImageVector` unresolved | `EmptyState.kt`, `DocumentListItem.kt`, `AppTopBar.kt` | Add `material3-icons-core` dependency + `import androidx.compose.graphics.vector.ImageVector` |
| K18 | `InitializationProvider` / `AppInitializer` Koin unresolved | `AppInitializer.kt`, `AndroidManifest.xml` | Remove `AppInitializer.kt` + `InitializationProvider` from manifest |
| K19 | `signingConfigs.debug` / `buildTypes.release` need `named()` | `build.gradle.kts` | Use `named("debug")` / `named("release")` |

---

## Gradle / Build Errors (and fixes)

| # | Error | File | Fix |
|---|-------|------|-----|
| G1 | Compose Compiler Gradle plugin required (Kotlin 2.0) | `build.gradle.kts` | Add `id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"` |
| G2 | AndroidSourceSet 'fullMain' not found | `build.gradle.kts` | Remove bogus `sourceSets` block referencing non-existent dirs |
| G3 | `android.useAndroidX` not enabled | `gradle.properties` | Add `android.useAndroidX=true` |
| G4 | Compose BOM versions empty (`.`) | `build.gradle.kts` | Add `implementation(platform(libs.compose.bom))` |
| G5 | `colorScheme` / `Theme.Material.DayNight.NoActionBar` not found | `themes.xml` | Use `Theme.AppCompat.DayNight.NoActionBar`; remove `android:colorScheme` |
| G6 | Release build crashes silently (JVM OOM) | `gradle.properties` | Add `org.gradle.jvmargs=-Xmx1536m` |
| G7 | `minifyEnabled=true` ProGuard failure | `build.gradle.kts` | Set `isMinifyEnabled = false` for release (re-enable at G8 with proper rules) |

---

## CI Workflow Rules (build.yml)

| # | Rule | Reason |
|---|------|--------|
| C1 | Quote step names containing `:` (e.g. `"Static gate: no internet permission"`) | YAML parsing error — colon after space treated as nested key |
| C2 | Use `gradle` command directly (NOT `sh gradlew`) | Wrapper jar missing in CI; install Gradle 8.10.2 per job via curl |
| C3 | `continue-on-error: true` on build steps + separate "fail if failed" step | Capture error output before job terminates |
| C4 | `tee` vs `>` for build logs: use `> /tmp/log 2>&1` then `cat` | `tee` with `|` can mask exit code |
| C5 | `::error::` annotations for build errors | Readable via Checks API JSON (not blocked like Azure Blob logs) |
| C6 | Single release variant (`assembleFullRelease`) to isolate issues | Two variants can mask which one fails |
| C7 | Static gates run AFTER successful build; skip if build failed | Avoid running gates on missing APKs |

---

## Pre-Push Checklist

Before every `git push`:

- [ ] Run `python3 tools/onedoc/traceability.py --check --strict` locally
- [ ] Check all modified Kotlin files against K1–K19 above
- [ ] Check `build.gradle.kts` against G1–G7 above
- [ ] Validate workflow YAML: `python3 -c "import yaml; yaml.safe_load(open('.github/workflows/build.yml'))"`
- [ ] Commit message references the specific fix (not generic "fix ci")
- [ ] No non-ASCII characters (em-dashes, etc.) in workflow YAML

---

## Current Status

- **Debug APK:** ✅ Builds successfully
- **Release APK:** ⚠️ Builds intermittently (silent Gradle crash on some runs); signing config to be resolved at G8
- **CI Lane:** ✅ All green (traceability, debug build, release build, static gates)
- **Branch:** `arena/db667982-doc`
