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

- Build and test with `./mvnw -B verify` (`mvnw.cmd` on Windows); the wrapper pins the Maven version. CI runs on pushes to `main` and on pull requests. `MainControllerUiTest` needs a display, so it is skipped in CI; run it locally after UI changes.
- The UI is a single window: `src/main/resources/fxml/main.fxml` with `MainController`. The old per-feature controllers, FXML files and components were removed as dead code (CQ-001); don't re-create them without a caller.
- Algorithm changes need tests that check correctness invariants (see TEST-002), not just "result is non-empty".
