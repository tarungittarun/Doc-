# OneDoc — Agent kickoff (work order, draft pending G0)

**Status: G0 CLOSED — owner acceptance recorded 2026-10-31.** This package is now the
build contract. Execute per work order §11, beginning with T-G1-001.

## 0. The go condition

G0 closed when the owner accepted `docs/onedoc/blueprint/onedoc-development-blueprint.pdf`
v1.0 (commit `2244cea`) as the build contract. Evidence: `reports/onedoc/gates/G0.md`
(CLOSED, 2026-10-31), D-001, S-001.

Execution branch (this session): `arena/db667982-doc` at `/home/user/Doc-`.
Reference blueprint repo: `https://github.com/tarungittarun/AIA-`
branch `arena/e4dfeee3-aia` (read-only reference; work committed here per Arena session
pinning).

## 1. Rules of engagement (absolute)

1. Fully local/on-device: no sign-in, no internet permission, no ads, no trackers, no
   exceptions (blueprint C1–C5).
2. Android 8.0+ (API 26); phones and tablets; portrait and landscape (C6).
3. Editor laws L1–L10 and journeys J1–J5 are release blockers (§13, §27).
4. Prove on the artifact: source tests help, only direct-APK QA passes ship (§21.1).
5. CI budgets: ≤ 3 parallel runners, ≤ 30 jobs/run (C8); publisher green ≠ QA pass.
6. BLOCKED discipline: never a product verdict; log, retry, never fake (§21.2).
7. Preserve predecessor artifacts under `reports/` and `qa/` — read-only history.
8. Ledgers always: D-nnn decisions, S-nnn sessions, gate reports (§29.2).
9. Traceability green after any catalogue change:
   `tools/onedoc/traceability.py --check --strict`.
10. Work only on branch `arena/db667982-doc`; incremental commits; no history rewrites.

## 2. Reading order (first 30 minutes)

Blueprint Part XII §29 (loop) → Part I (charter) → Part X §21–22 (quality/CI) →
Part V §13 (laws) → Appendix D (gate DoDs). Then Appendix N (environment) before T-G1-001.

## 3. Environment bootstrap (Day 0)

Follow Appendix N.1–N.3 verbatim: JDK 17, SDK 36, build-tools 36.0.0, AVDs
`od-api26/33/35/36`, golden boot snapshots. Recreate the tool venv after any sandbox
restore: `python3 -m venv .venv && .venv/bin/pip -q install reportlab pypdf pillow matplotlib pymupdf pyyaml`.

**BLOCKED in this sandbox:** no Java/JDK, no Android SDK, no network to dl.google.com.
Recorded in G1 report; unblock plan: owner provides SDK/JDK or pre-built environment.

## 4. First sprint (G1 = T-G1-001…005)

| Day | Tasks |
|---|---|
| 1 | T-G1-001 environment (BLOCKED — no SDK/Java) |
| 2 | T-G1-002 Gradle skeleton assembling both flavors (source committed; build deferred) |
| 3 | T-G1-003 CI pr.yml green within budgets |
| 4 | T-G1-004 shell app (home empty-state, settings stub, About) |
| 5 | T-G1-005 qa-driver skeleton + close G1 with DoD evidence |

## 5. Working files

| File | Role |
|---|---|
| `docs/onedoc/plan/task-graph.yaml` | seeded graph (47 tasks) — statuses move, ids never change |
| `docs/onedoc/plan/decisions.md` | append-only decision ledger |
| `docs/onedoc/plan/sessions.md` | append-only session log |
| `reports/onedoc/gates/G0.md` | gate report — CLOSED |
| `reports/onedoc/gates/G1..G8.md` | gate reports — to be closed per gate |
| `tools/onedoc/traceability.py` | catalogue generator + invariant checks |
| `qa/onedoc/testcases.csv` | generated catalogue (regenerate, never hand-edit) |
| `docs/onedoc/blueprint/` | blueprint content sources + PDF |

## 6. G0 exit checklist (complete)

- [x] Owner acceptance recorded in `reports/onedoc/gates/G0.md` (CLOSED, 2026-10-31)
- [x] D-001 appended (decision: charter accepted)
- [x] S-001 session block written
- [ ] `traceability.py --check --strict` exit 0 (pending traceability tool + catalogue)

## 7. Owner actions

1. Accept the blueprint — DONE (this work order, §10, dated 2026-10-31).
2. Optional: a Figma personal-access token for the nightly registry-drift check (§7.4).
3. Day-6 (G8 prep): the release keystore — an RC keystore covers everything until then (§26.1).
4. Unblock environment: provide JDK 17 + Android SDK 36 (or a pre-built environment) so
   T-G1-001..T-G1-005 can build and run on emulators.
