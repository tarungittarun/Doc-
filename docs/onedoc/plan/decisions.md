# Decision ledger — OneDoc (append-only, work order §3 rule 9)

Rules:
1. Every choice **not dictated by the blueprint** gets an entry here before the code lands.
2. Entries are never edited or deleted; revisiting a decision = a new entry that supersedes it.
3. Each entry names the blueprint sections it *could* have affected.
4. The G7 review walks this file top to bottom — it is how the owner audits an autonomous build.

Entry format:

```markdown
### D-<n> — <question>
- Date / phase:
- Binding sections that could be affected: §…
- Options considered: a) … b) …
- Decision:
- Consequence / revisit-if:
```

---

### D-000 — EXAMPLE ENTRY (format illustration only — do not count as a decision)
- Date / phase: 31 Oct 2026 / draft
- Binding sections that could be affected: §23.5
- Options considered: a) add library X now b) wait for G1 catalog freeze
- Decision: b) wait
- Consequence / revisit-if: revisit only if a G1 task is blocked without X

---

### D-001 — G0 closed by owner acceptance in work order
- Date / phase: 2026-10-31 / G0
- Binding sections that could be affected: §front (charter, constraints C1–C10), §10 (owner acceptance act)
- Options considered: a) treat work order as owner acceptance b) wait for separate owner reply
- Decision: a) The work order itself constitutes the owner's formal acceptance of OneDoc blueprint v1.0 (commit `2244cea`) as the build contract, per work order §10.
- Consequence / revisit-if: G0 closed; G1 begins. Revisit only if the owner issues a correction to the blueprint.
