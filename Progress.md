# Progress

Last updated: 2026-05-15

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

### Main JavaFX Workflow
- [x] Main screen loads through Spring-managed FXML controller.
- [x] Relation creation is wired from the main screen.
- [x] Functional dependency creation is wired from the main screen.
- [x] Relation and FD list refresh after updates.
- [x] Attribute closure computation is wired from the main screen.
- [x] Candidate key computation is wired from the main screen.
- [x] Normal form analysis button is wired from the main screen.
- [x] BCNF decomposition button is wired from the main screen.
- [x] Main result panel is bound and displays action results/errors.
- [x] Status label updates after main actions.
- [x] FD removal from the main screen via list selection.
- [x] Session clear/reset from the main screen.
- [x] Duplicate FD detection with user feedback.
- [x] Minimal cover computation wired from the main screen.
- [x] 3NF synthesis wired from the main screen.
- [x] Decomposition analysis (dep preservation + lossless join) wired from the main screen.

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
- [x] FD parser edge case tests pass (14 tests).
- [x] BCNF decomposition tests pass (3 tests).
- [x] `mvn test` passes (59 tests, 0 failures).
- [x] `mvn -DskipTests package` passes.
- [x] `mvn javafx:run` reaches desktop app startup and remains alive.

## Partially Implemented

### UI
- [x] Main screen can run the full workflow through simple input dialogs.
- [x] Buttons organized into logical rows (management, analysis, decomposition).
- [ ] Dedicated relation dialog is not integrated into the main workflow.
- [ ] Dedicated FD dialog is not integrated into the main workflow.
- [ ] Dedicated closure, candidate key, normal form, BCNF, and explanation screens are still mostly standalone stubs.
- [ ] Main screen layout is functional but not polished.
- [ ] Error handling is currently text-based in the result panel.

## Remaining Work

### Core Backend
- [ ] Normalize attribute handling so multi-character attribute names are supported consistently.
- [ ] Add relation validation for empty relation names.
- [ ] Decide whether `InMemorySessionRepository` should replace direct state in `RelationService`.

### Normalization Algorithms
- [ ] Add support for MVDs and 4NF/5NF (stretch goal).

### JavaFX Application
- [ ] Replace simple `TextInputDialog` flows with proper embedded forms or modal FXML dialogs.
- [ ] Add validation messages next to fields.
- [ ] Add relation summary panel with attributes, FDs, keys, and current normal form.
- [ ] Add result tabs for closure, keys, normal forms, decomposition, and explanations.
- [ ] Add keyboard-friendly navigation.
- [ ] Improve visual design and spacing.
- [ ] Add deterministic sorted rendering for all sets.
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
