# Progress

Last updated: 2026-10-12

Known defects and their fixes are tracked in [`bugreport.md`](../bugreport.md); this file tracks features and project setup.

## Implemented

### Project Setup
- [x] Maven project builds with a Java 21 target (`./mvnw -B verify`).
- [x] Maven Wrapper (`mvnw`, `mvnw.cmd`) pins Maven 3.9.16; CI and `build-exe.ps1` use it.
- [x] Mockito is loaded as a test Java agent (`-javaagent` in Surefire), so tests no longer warn about self-attaching or dynamic agent loading.
- [x] Spring Boot (non-web) provides dependency injection; JavaFX controls and FXML from Maven.
- [x] `./mvnw javafx:run` launches the app; the desktop launcher starts JavaFX first and boots Spring inside its lifecycle.
- [x] GitHub Actions CI runs `./mvnw -B verify` on pushes to `main` and on pull requests.
- [x] `.gitattributes` keeps text files LF in the repository (CRLF for Windows scripts).
- [x] `build-exe.bat` / `build-exe.ps1` build `Normalizer.exe` with `jpackage`, reading the version from `pom.xml` and bundling only the JDK modules the app needs.
- [x] IntelliJ `.idea/` settings stay local (ignored by git).

### Backend Wiring
- [x] Services, controller and algorithm classes are wired by Spring with constructor injection (`AppConfig`).
- [x] Relations and FDs are immutable model objects; `RelationService` holds the current relation.
- [x] One name grammar for relations, attributes and FDs: letters, digits and underscores, not starting with a digit.
- [x] Attribute lists can be comma/space separated, or compact (`AB`, `ab`, `A1B`), which is split into the relation's attribute names; ambiguous input is rejected.
- [x] Relations are limited to 20 attributes (`AnalysisLimits`).
- [x] Uncaught errors and start-up failures are shown in an error dialog (`UnexpectedErrorHandler`).

### Main JavaFX Workflow
- [x] Single main window (`main.fxml` + `MainController`) with embedded relation, FD and closure forms and inline validation.
- [x] Actions: create relation, add/remove FDs (duplicates rejected), closure, keys, normal form, minimal cover, explain, BCNF, 3NF, Check Result, clear.
- [x] Result tabs: App Tutorial, Closure, Keys, Normal Forms, Minimal Cover, Decomposition, Check Result, Explanation.
- [x] Relation summary panel shows attributes, FDs, candidate keys, current normal form and the last decomposition.
- [x] Built-in examples (student/course, employee/department, library loans); loading one is all-or-nothing.
- [x] Keyboard-friendly Enter submission and mnemonics; windowed/maximized/fullscreen controls.
- [x] Dark bluish-green stylesheet; workflow and summary panes scroll independently.
- [x] Report text is built by `ResultFormatter`; the tutorial text lives in `resources/text/tutorial.txt`.

### Algorithms
- [x] Attribute closure (fixed point), with a step trace.
- [x] Candidate keys: pruned search (always / never / maybe in a key) with bitmask closures.
- [x] 2NF, 3NF and BCNF checks with detailed violations; 1NF is assumed (attributes are atomic).
- [x] Minimal cover (singleton right sides, extraneous attributes, redundant FDs), with a step trace.
- [x] BCNF decomposition checked against all implied FDs (F⁺); final relations carry their projected FDs.
- [x] 3NF synthesis (minimal cover, one relation per left side, key relation, remove subsumed), with projected FDs.
- [x] Dependency preservation (standard fixed-point test) and lossless join (binary shortcut and chase).
- [x] Results do not depend on hash-set iteration order (`FdOrder`).
- [x] Implemented scope: normalization up to BCNF (4NF/5NF planned below).

### Tests And Verification
- [x] 123 tests, all passing (`./mvnw -B verify`).
- [x] Seeded random tests against brute-force oracles for keys, BCNF, 3NF, minimal cover, normal forms and order independence.
- [x] Textbook examples with exact expected answers.
- [x] Performance tests at the 20-attribute limit.
- [x] Spring context tests, including bean wiring.
- [x] UI smoke test (`MainControllerUiTest`) that clicks through the real window; skipped on machines without a display, including CI.
- [x] Packaged app image verified by building it and starting the bundled runtime and `Normalizer.exe`.

## Remaining Work

### Tooling
- [ ] Add formatting/checkstyle configuration.
- [ ] Optionally run JavaFX from the module path in the packaged app, to remove its "unsupported configuration" start-up warning.
- [ ] Optionally run `MainControllerUiTest` in CI (needs a virtual display, e.g. xvfb).

### 4NF and 5NF (see [Roadmap.md](Roadmap.md))
- [ ] Phase 1: MVD and JD model classes, parser (`X ->> Y`, `*(A,B | B,C)`), UI input and listing.
- [ ] Phase 2: implication engine (chase for FDs, MVDs and JDs; dependency basis cross-checked against it).
- [ ] Phase 3: closures and candidate keys under FDs + MVDs; 4NF check with traces.
- [ ] Phase 4: 4NF decomposition and dependency projection; Check Result reports lost MVDs/JDs.
- [ ] Phase 5: 5NF check with respect to the given dependencies; 5NF decomposition.
- [ ] Phase 6: examples, limits and performance tests, docs and tutorial.
- [ ] Owner decisions needed first: JD syntax, effect of MVDs on the existing FD features, 4NF/5NF attribute limit, 5NF scope (see the roadmap's open questions).

### Other Features (see `UC.md`, section 6)
- [ ] Save and load analysis sessions.
- [ ] SQL schema export.
- [ ] Minimal cover visualization.
- [ ] Interactive decomposition graphs.
- [ ] Report generation.

## Development Commands

```bash
./mvnw -B verify       # build and run all tests (mvnw.cmd on Windows)
./mvnw javafx:run      # run the application
build-exe.bat          # build target/dist/Normalizer/Normalizer.exe (Windows)
```

## Environment Notes

- Building needs JDK 21 or newer; this machine builds with JDK 25. Maven comes from the wrapper.
- `build-exe.bat` needs `jpackage`, `jlink` and `jdeps` from that JDK on `PATH`.
- The packaged app can also be started from its bundled runtime for console output:
  `target/dist/Normalizer/runtime/bin/java -cp "target/dist/Normalizer/app/*" com.dbms.analyzer.Launcher`
