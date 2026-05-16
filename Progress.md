# Progress

Last updated: 2026-05-16

## Implemented

### Project Setup
- [x] Maven project parses and builds successfully.
- [x] Java 21 build target configured.
- [x] Spring Boot backend dependency configured.
- [x] JavaFX controls and FXML dependencies configured.
- [x] JavaFX Maven plugin configured for `mvn javafx:run`.
- [x] Spring Boot Maven plugin configured for packaging.
- [x] Desktop launcher added so JavaFX starts first and boots Spring inside the JavaFX lifecycle.
- [x] Spring context configured as a non-web application.
- [x] Main FXML startup issue fixed by importing `java.lang.String`.
- [x] IntelliJ project settings updated locally for Java 21 and Maven run configurations.

### Backend Wiring
- [x] Spring component scanning works for services, controllers, repository, and config.
- [x] Algorithm beans defined in `AppConfig`.
- [x] Service layer converted to constructor injection for core services.
- [x] Spring context test added to verify core app/controller wiring.
- [x] Attribute parsing supports comma-separated multi-character names while preserving single-letter shorthand.
- [x] Relation creation validates empty names and rejects whitespace inside attribute names.

### Main JavaFX Workflow
- [x] Main screen loads through Spring-managed FXML controller.
- [x] Relation creation is wired from the main screen.
- [x] Functional dependency creation is wired from the main screen.
- [x] Relation and FD list refresh after updates.
- [x] Attribute closure computation is wired from the main screen.
- [x] Candidate key computation is wired from the main screen.
- [x] Normal form analysis button is wired from the main screen.
- [x] BCNF decomposition button is wired from the main screen.
- [x] Main result tabs are bound and display action results/errors.
- [x] Status label updates after main actions.
- [x] FD removal from the main screen via list selection.
- [x] Session clear/reset from the main screen.
- [x] Duplicate FD detection with user feedback.
- [x] Minimal cover computation wired from the main screen.
- [x] 3NF synthesis wired from the main screen.
- [x] Decomposition analysis (dep preservation + lossless join) wired from the main screen.
- [x] Main workflow uses embedded relation, FD, and closure forms instead of prompt dialogs.
- [x] Inline validation messages are shown beside relation, FD, and closure inputs.
- [x] Relation summary panel shows attributes, FDs, candidate keys, current normal form, and last decomposition.
- [x] Result tabs are available for closure, keys, normal forms, minimal cover, decomposition, and explanations.
- [x] Main actions support keyboard-friendly Enter submission and mnemonic shortcuts.
- [x] Main screen visual design and spacing polished with a dedicated stylesheet.
- [x] Header window mode controls added for windowed, maximized, and fullscreen states.
- [x] Main stylesheet refreshed with a dark bluish-green desktop theme.
- [x] Workflow panel compacted so all action sections fit on a 1920 x 1080 screen.
- [x] App Tutorial result tab added with step-by-step usage guidance.
- [x] Workflow and summary panes made independently scrollable for smaller or resized windows.

### Algorithms
- [x] Attribute closure computation.
- [x] Candidate key discovery.
- [x] 2NF detection for partial dependencies on candidate-key subsets.
- [x] 3NF detection for non-trivial FDs whose determinant is not a superkey and whose RHS is non-prime.
- [x] Detailed normal form violation reporting for 2NF, 3NF, and BCNF checks.
- [x] Basic BCNF check.
- [x] BCNF decomposition with FD projection onto sub-relations.
- [x] Trivial FD filtering during BCNF violation search.
- [x] Exact violating determinant reported during BCNF decomposition.
- [x] Step-by-step BCNF decomposition explanation.
- [x] Minimal cover computation (decompose RHS, remove extraneous LHS, remove redundant FDs).
- [x] 3NF synthesis (minimal cover → relation per FD → candidate key relation → remove subsumed).
- [x] Dependency preservation checking (projected FD closure equivalence).
- [x] Lossless join checking (binary superkey shortcut + general chase algorithm).

### Models
- [x] Deterministic `toString()` for `Relation`, `FunctionalDependency`, and `CandidateKey` (sorted attributes).
- [x] `equals`/`hashCode` on `Relation` for reliable Set usage.

### Tests And Verification
- [x] Existing algorithm tests pass.
- [x] Existing service tests pass.
- [x] Spring context smoke test passes.
- [x] 2NF and 3NF tests pass.
- [x] Minimal cover algorithm tests pass (6 tests).
- [x] 3NF synthesis tests pass (4 tests).
- [x] Dependency preservation tests pass (4 tests).
- [x] Lossless join tests pass (5 tests).
- [x] FD parser edge case tests pass (19 tests).
- [x] Closure service multi-character attribute tests pass (2 tests).
- [x] BCNF decomposition tests pass (3 tests).
- [x] `mvn test` passes (72 tests, 0 failures).
- [x] `mvn -DskipTests package` passes.
- [x] `mvn javafx:run` reaches desktop app startup and remains alive.

## Partially Implemented

### UI
- [x] Main screen can run the full workflow through embedded forms.
- [x] Buttons/actions are organized into relation, FD, closure, analysis, and decomposition sections.
- [x] Relation, FD, closure, candidate key, normal form, BCNF, and explanation workflows are integrated into the main workspace.
- [x] Main screen layout is polished with workflow, result, and summary panes.
- [x] Error handling uses inline validation labels and a status bar.

## Remaining Work

### Core Backend
- [ ] Decide whether `InMemorySessionRepository` should replace direct state in `RelationService`.

### Normalization Algorithms
- [ ] Add support for MVDs and 4NF/5NF (stretch goal).

### JavaFX Application
- [ ] Add startup sample data or examples for development/demo mode.

### Tests
- [ ] Add service tests using Spring context where useful.
- [ ] Add JavaFX controller tests or lightweight FXML loading tests.

### Packaging And Tooling
- [ ] Decide whether `.idea` files should stay local-only or be committed intentionally.
- [ ] Add Maven Wrapper (`mvnw`) for consistent local setup.
- [ ] Add formatting/checkstyle configuration.
- [ ] Add CI workflow for tests.
- [ ] Investigate Mockito dynamic agent warning from tests.
- [ ] Document direct Java/JAR launch expectations for JavaFX runtime modules.

## Current Development Commands

```bash
mvn test
mvn -DskipTests package
mvn javafx:run
```


## Known Environment Notes

- Maven is currently using JDK 21 successfully.
- The shell `java -version` may still point to Java 8 on this machine, so direct Java commands should be run after fixing `JAVA_HOME` and `PATH`.
- `mvn javafx:run` is the recommended development launch command for now.
