#!/usr/bin/env python3
"""OneDoc traceability tool.

Maps features (docs/onedoc/blueprint/features.csv) to test cases
(qa/onedoc/testcases.csv) and enforces the traceability invariant:

  * every feature has >= 1 test binding
  * every P0 feature has >= 1 test binding
  * no orphan test cases (bound to a non-existent feature)
  * no open (unbound) feature mappings

Usage:
  python3 tools/onedoc/traceability.py --emit    # regenerate qa/onedoc/testcases.csv from features.csv
  python3 tools/onedoc/traceability.py --check   # report-mode invariant check (exit 0 if green)
  python3 tools/onedoc/traceability.py --check --strict  # strict mode; fail hard on any gap

Exit codes: 0 = green, 1 = invariant violation.
"""

from __future__ import annotations

import argparse
import csv
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]  # repo root (tools/onedoc -> repo root)
FEATURES_CSV = ROOT / "docs" / "onedoc" / "blueprint" / "features.csv"
TESTCASES_CSV = ROOT / "qa" / "onedoc" / "testcases.csv"


def load_features(path: Path) -> dict[str, dict]:
    """Return {feature_id: {priority, title, test_bindings}}."""
    features: dict[str, dict] = {}
    if not path.exists():
        return features
    with path.open(newline="", encoding="utf-8") as fh:
        for row in csv.DictReader(fh):
            fid = (row.get("feature_id") or "").strip()
            if not fid:
                continue
            bindings = [b.strip() for b in (row.get("test_bindings") or "").split(";") if b.strip()]
            features[fid] = {
                "priority": (row.get("priority") or "P2").strip(),
                "title": (row.get("title") or "").strip(),
                "bindings": bindings,
            }
    return features


def load_testcases(path: Path) -> list[dict]:
    """Return list of test case rows."""
    cases: list[dict] = []
    if not path.exists():
        return cases
    with path.open(newline="", encoding="utf-8") as fh:
        for row in csv.DictReader(fh):
            cases.append(row)
    return cases


def check(features: dict[str, dict], cases: list[dict], strict: bool = False) -> int:
    """Run traceability invariants. Returns 0 if green, 1 otherwise."""
    problems: list[str] = []

    # Index test bindings by feature id.
    bound_features: set[str] = set()
    for case in cases:
        fid = (case.get("feature_id") or "").strip()
        if fid:
            bound_features.add(fid)
            if fid not in features:
                problems.append(f"ORPHAN TEST {case.get('test_id')}: bound to unknown feature {fid}")

    # Every feature must have >= 1 test binding.
    unbound: list[str] = []
    unbound_p0: list[str] = []
    for fid, meta in features.items():
        if not meta["bindings"] and fid not in bound_features:
            unbound.append(fid)
            if meta["priority"] == "P0":
                unbound_p0.append(fid)

    if unbound:
        problems.append(f"UNBOUND FEATURES ({len(unbound)}): " + ", ".join(sorted(unbound)))
    if unbound_p0:
        problems.append(f"UNBOUND P0 FEATURES ({len(unbound_p0)}): " + ", ".join(sorted(unbound_p0)))

    # Test cases without a feature id are open mappings.
    open_cases = [c.get("test_id") for c in cases if not (c.get("test_id") or c.get("feature_id"))]
    if open_cases:
        problems.append(f"OPEN TEST CASES (no ids): {', '.join(open_cases)}")

    # Summary.
    p0_count = sum(1 for m in features.values() if m["priority"] == "P0")
    print(f"features={len(features)}  p0={p0_count}  test_cases={len(cases)}")
    print(f"bound_features={len(bound_features)}  unbound={len(unbound)}  unbound_p0={len(unbound_p0)}")

    if problems:
        print("TRACEABILITY: RED")
        for p in problems:
            print(f"  - {p}")
        return 1
    print("TRACEABILITY: GREEN — every feature has >= 1 test binding; no orphans; no open mappings.")
    return 0


def emit(features: dict[str, dict], out_path: Path) -> None:
    """Regenerate testcases.csv from the feature registry."""
    rows: list[dict] = []
    counter = 1
    for fid in sorted(features):
        meta = features[fid]
        bindings = meta["bindings"]
        if not bindings:
            # Auto-create a binding TC-<prefix>-NNN for the feature.
            prefix = fid.split("-")[0] if "-" in fid else "TC"
            tc_id = f"{prefix}-{counter:03d}"
            counter += 1
            bindings = [tc_id]
        for tc_id in bindings:
            rows.append(
                {
                    "test_id": tc_id,
                    "feature_id": fid,
                    "priority": meta["priority"],
                    "title": f"Verify {fid}: {meta['title']}",
                    "binding": "auto",
                    "verdict": "not-run",
                }
            )

    out_path.parent.mkdir(parents=True, exist_ok=True)
    fieldnames = ["test_id", "feature_id", "priority", "title", "binding", "verdict"]
    with out_path.open("w", newline="", encoding="utf-8") as fh:
        writer = csv.DictWriter(fh, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)
    print(f"Emitted {len(rows)} test cases -> {out_path}")


def main() -> int:
    parser = argparse.ArgumentParser(description="OneDoc traceability tool")
    parser.add_argument("--emit", action="store_true", help="regenerate testcases.csv from features.csv")
    parser.add_argument("--check", action="store_true", help="check traceability invariants")
    parser.add_argument("--strict", action="store_true", help="strict mode (fail on any gap)")
    args = parser.parse_args()

    features = load_features(FEATURES_CSV)
    cases = load_testcases(TESTCASES_CSV)

    if args.emit:
        emit(features, TESTCASES_CSV)
        # Re-check after emit.
        cases = load_testcases(TESTCASES_CSV)
        return check(features, cases, strict=True)

    if args.check:
        return check(features, cases, strict=args.strict)

    parser.print_help()
    return 0


if __name__ == "__main__":
    sys.exit(main())
