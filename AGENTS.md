# Agent Instructions

## Bug report (required)

[`bugreport.md`](bugreport.md) in the repo root is the project's running bug log. It must always exist and stay current.

- **Read it before changing code.** It lists known defects, their root causes, and which code paths are dead.
- **Update it in the same commit as your change** whenever you:
  - fix an issue: set its status to `Fixed (<short-sha>)` and name the regression test that covers it;
  - find a new issue: add it with the next free ID in its category (`BUG-`, `BLD-`, `CQ-`, `TEST-`, `DOC-`), a severity, and a reproduction someone else can run;
  - change behaviour that an entry describes: update or re-triage the entry.
- **Never delete fixed entries.** Keep the summary table and the counts in the header in sync.
- After a full review, update "Last full review at commit" and add a row to the changelog.
- The full rules are in the "How to maintain this file" section of `bugreport.md`.

## Project notes

- Build and test with `mvn -B verify`. CI runs on every push.
- `src/main/resources/fxml/main.fxml` + `MainController` is the only live UI. Before extending other controllers, components, or FXML files, check CQ-001 in the bug report: they are dead code.
- Algorithm changes need tests that check correctness invariants (see TEST-002), not just "result is non-empty".
