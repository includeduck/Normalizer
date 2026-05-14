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

### Tests And Verification
- [x] Existing algorithm tests pass.
- [x] Existing service tests pass.
- [x] Spring context smoke test passes.
- [x] 2NF and 3NF tests pass.
- [x] `mvn test` passes.
- [x] `mvn -DskipTests package` passes.
- [x] `mvn javafx:run` reaches desktop app startup and remains alive.

## Partially Implemented

### Algorithms
- [x] Attribute closure computation.
- [x] Candidate key discovery.
- [x] 2NF detection for partial dependencies on candidate-key subsets.
- [x] 3NF detection for non-trivial FDs whose determinant is not a superkey and whose RHS is non-prime.
- [x] Detailed normal form violation reporting for 2NF, 3NF, and BCNF checks.
- [x] Basic BCNF check.
- [x] Basic BCNF decomposition path.
- [ ] FD projection during BCNF decomposition.
- [ ] Lossless join and dependency preservation checks.

### UI
- [x] Main screen can run the first workflow through simple input dialogs.
- [ ] Dedicated relation dialog is not integrated into the main workflow.
- [ ] Dedicated FD dialog is not integrated into the main workflow.
- [ ] Dedicated closure, candidate key, normal form, BCNF, and explanation screens are still mostly standalone stubs.
- [ ] Main screen layout is functional but not polished.
- [ ] Error handling is currently text-based in the result panel.

## Remaining Work

### Core Backend
- [ ] Normalize attribute handling so multi-character attribute names are supported consistently.
- [ ] Make `Relation`, `FunctionalDependency`, and `CandidateKey` output deterministic.
- [ ] Add relation validation for empty relation names.
- [ ] Add duplicate FD handling feedback.
- [ ] Add FD removal/editing.
- [ ] Add session reset and clear actions.
- [ ] Decide whether `InMemorySessionRepository` should replace direct state in `RelationService`.

### Normalization Algorithms
- [x] Implement 2NF:
  - Detect partial dependency from a proper subset of a candidate key to a non-prime attribute.
  - Ignore trivial dependencies.
  - Report specific violating determinants and non-prime attributes.
- [x] Implement 3NF:
  - For every FD X -> A, verify X is a superkey, A is prime, or the FD is trivial.
  - Report specific violating FDs.
- [ ] Improve BCNF:
  - Ignore trivial dependencies.
  - Report the exact violating determinant.
  - Preserve relevant FDs in decomposed relations.
- [ ] Add minimal cover computation.
- [ ] Add 3NF synthesis.
- [ ] Add dependency preservation analysis.
- [ ] Add lossless join analysis.

### JavaFX Application
- [ ] Replace simple `TextInputDialog` flows with proper embedded forms or modal FXML dialogs.
- [ ] Add validation messages next to fields.
- [ ] Add relation summary panel with attributes, FDs, keys, and current normal form.
- [ ] Add FD list actions: add, edit, remove, clear.
- [ ] Add result tabs for closure, keys, normal forms, decomposition, and explanations.
- [ ] Add keyboard-friendly navigation.
- [ ] Improve visual design and spacing.
- [ ] Add deterministic sorted rendering for all sets.
- [ ] Add startup sample data or examples for development/demo mode.

### Tests
- [x] Add unit tests for 2NF.
- [x] Add unit tests for 3NF.
- [ ] Add unit tests for BCNF decomposition FD projection.
- [ ] Add service tests using Spring context where useful.
- [ ] Add JavaFX controller tests or lightweight FXML loading tests.
- [ ] Add parser tests for relation and FD input edge cases.

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
