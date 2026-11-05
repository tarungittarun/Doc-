# OneDoc CI Logging

Every CI step writes its output to a per-step log file under `ci-logs/<job-name>/`.
All log files are uploaded as GitHub Actions artifacts after each run.

## Where to find CI logs

### GitHub Actions UI (recommended)
1. Go to https://github.com/tarungittarun/Doc-/actions
2. Click on the workflow run you want to inspect
3. Look under "Artifacts" for the job-specific log bundle:
   - `ci-logs-traceability` — traceability check logs
   - `ci-logs-build-debug` — debug APK build logs
   - `ci-logs-build-release` — release APK build logs
   - `ci-logs-emulator-shard-1` — emulator test shard 1 logs
   - `ci-logs-emulator-shard-2` — emulator test shard 2 logs
   - `ci-logs-screenshots-pdf` — screenshot PDF report logs
   - `ci-logs-verify-ci-lane` — CI lane verification logs
   - `ci-logs-release-build` — release workflow build logs
   - `ci-logs-provenance` — release provenance logs
   - `ci-logs-release` — release workflow release logs
   - `ci-logs-pr-traceability` — PR lane traceability logs
   - `ci-logs-pr-static-gates` — PR lane static gates logs
   - `ci-logs-pr-verify` — PR lane verification logs
4. Download the artifact ZIP and extract to see individual step log files.

### Log file naming convention
Each job creates `ci-logs/<job-name>/<step-name>.log` files, e.g.:
- `ci-logs/build-debug/gradle-download.log`
- `ci-logs/build-debug/jdk-version.log`
- `ci-logs/build-debug/keystore-restore.log`
- `ci-logs/build-debug/gradle-build.log`
- `ci-logs/build-release/gradle-build.log`
- `ci-logs/emulator-shard-1/gradle-build.log`
- `ci-logs/emulator-shard-1/emulator-test.log`
- `ci-logs/emulator-shard-1/logcat-shard1.txt`

### Each step includes:
- **Validation**: exit codes are checked; failures stop the job with `::error::`
- **Logging**: stdout + stderr are teed to both the GitHub Actions log and a file
- **Key error extraction**: Gradle builds grep for `e:`, `Execution failed`, `FAILED`, `Exception`, etc. and surface them as notices

## Local CI log directory

`ci-logs/` is gitignored (per-run build artifacts). A `.gitkeep` placeholder exists so the directory structure is documented. Logs are not committed to git — they are ephemeral per-run and accessed via GitHub Actions artifacts.

## If you cannot download artifacts

If you cannot access GitHub Actions artifacts, the run ID and status are visible in the:
- GitHub Actions web UI at https://github.com/tarungittarun/Doc-/actions
- Pull request checks
- Commit status badges

Use the run ID to locate the specific artifacts for that run.
