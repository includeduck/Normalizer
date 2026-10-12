# DBMS Normalizer - Project Structure

## Project Overview

- Java classes (main): 36
- Java test classes: 22
- FXML layout files: 1
- Resource files: 3 (`application.properties`, `styles.css`, `text/tutorial.txt`)

Known issues and their status are tracked in [`bugreport.md`](../bugreport.md).

---

## Directory Structure

```
dbms-normalizer/
│
├── pom.xml                                     # Maven project configuration
├── README.md                                   # Project documentation
├── bugreport.md                                # Running bug and issue log
├── AGENTS.md                                   # Instructions for AI agents
├── MarkDown/                                   # UC.md, Progress.md, this file
├── build-exe.bat / build-exe.ps1               # Windows .exe packaging (jpackage)
├── mvnw / mvnw.cmd, .mvn/wrapper/              # Maven Wrapper (pins Maven 3.9.16)
├── .github/workflows/ci.yml                    # CI: build and test (main and PRs)
│
└── src/
    ├── main/
    │   ├── java/com/dbms/analyzer/
    │   │   ├── Launcher.java                   # Desktop entry point
    │   │   ├── DbmsApplication.java            # Spring Boot configuration
    │   │   │
    │   │   ├── javafx/
    │   │   │   ├── MainApp.java                # JavaFX application; boots Spring, loads main UI
    │   │   │   ├── ResultFormatter.java        # Report text for the UI (no JavaFX types)
    │   │   │   ├── SampleRelations.java        # Built-in examples for "Load Example"
    │   │   │   ├── controllers/
    │   │   │   │   └── MainController.java     # The single UI controller (main.fxml)
    │   │   │   └── utils/
    │   │   │       └── UnexpectedErrorHandler.java  # Error dialogs for uncaught errors
    │   │   │
    │   │   ├── service/                        # Service Layer (10 files)
    │   │   │   ├── RelationService.java        # Holds the current relation
    │   │   │   ├── FdService.java              # Adds/removes functional dependencies
    │   │   │   ├── ClosureService.java         # Attribute closure
    │   │   │   ├── CandidateKeyService.java    # Candidate keys (cached), prime attributes
    │   │   │   ├── NormalFormService.java      # Normal form analysis
    │   │   │   ├── MinimalCoverService.java    # Minimal cover
    │   │   │   ├── BcnfDecompositionService.java
    │   │   │   ├── ThreeNfService.java         # 3NF synthesis
    │   │   │   ├── DecompositionAnalysisService.java  # Dependency preservation, lossless join
    │   │   │   └── ExplanationService.java     # Explanation text
    │   │   │
    │   │   ├── algorithm/                      # Algorithm Implementations (13 files)
    │   │   │   ├── ClosureComputer.java        # Fixed-point attribute closure
    │   │   │   ├── CandidateKeyFinder.java     # Pruned candidate-key search
    │   │   │   ├── NormalFormChecker.java      # 2NF/3NF/BCNF checks
    │   │   │   ├── MinimalCoverComputer.java   # Minimal (canonical) cover
    │   │   │   ├── BcnfDecomposer.java         # Recursive BCNF decomposition
    │   │   │   ├── ThreeNfSynthesizer.java     # 3NF synthesis
    │   │   │   ├── DependencyPreservationChecker.java
    │   │   │   ├── LosslessJoinChecker.java    # Binary test and chase
    │   │   │   ├── FdUtil.java                 # FD parsing and validation
    │   │   │   ├── FdOrder.java                # Canonical ordering of FDs and keys
    │   │   │   ├── FdProjector.java            # Projection of F⁺ onto a sub-schema
    │   │   │   ├── FdBitIndex.java             # Bitmask closures (package-private)
    │   │   │   └── AnalysisLimits.java         # Maximum attribute count
    │   │   │
    │   │   ├── model/                          # Data Models (5 files)
    │   │   │   ├── Relation.java
    │   │   │   ├── FunctionalDependency.java
    │   │   │   ├── CandidateKey.java
    │   │   │   ├── NormalFormResult.java
    │   │   │   └── DecompositionStep.java
    │   │   │
    │   │   └── config/
    │   │       └── AppConfig.java              # Algorithm bean definitions
    │   │
    │   └── resources/
    │       ├── application.properties          # Spring Boot properties
    │       ├── css/styles.css                  # JavaFX stylesheet
    │       ├── fxml/main.fxml                  # Main workspace (the only FXML file)
    │       └── text/tutorial.txt               # "App Tutorial" tab text
    │
    └── test/java/com/dbms/analyzer/
        ├── DbmsApplicationContextTest.java     # Spring context loads
        ├── algorithm/                          # Algorithm, property and textbook tests (12 files)
        ├── javafx/                             # ResultFormatterTest, MainControllerUiTest, error handler
        ├── model/                              # RelationTest, FunctionalDependencyTest
        └── service/                            # Service tests (4 files)
```

---

## File Categories & Descriptions

### Core Application Files (3)
- **Launcher.java** - Main desktop entry point used by Maven and packaged runs
- **DbmsApplication.java** - Spring Boot application configuration
- **MainApp.java** - JavaFX application startup; boots Spring, loads `main.fxml`, installs `UnexpectedErrorHandler`, and shows a dialog if start-up fails

### Model Classes (5)
- **Relation.java** - A relation with attributes and FDs
- **FunctionalDependency.java** - An FD with left and right sides (immutable)
- **CandidateKey.java** - A minimal superkey (immutable)
- **NormalFormResult.java** - Highest normal form, sorted violations, explanation
- **DecompositionStep.java** - One BCNF decomposition step

### Algorithm Classes (13)
- **ClosureComputer.java** - Attribute closure by fixed-point iteration, with an optional trace
- **CandidateKeyFinder.java** - Classifies attributes (always / never / maybe in a key) and searches the remaining subsets smallest first
- **NormalFormChecker.java** - 2NF/3NF/BCNF checks on the original relation (1NF is assumed)
- **MinimalCoverComputer.java** - Singleton right sides, extraneous attributes, redundant FDs
- **BcnfDecomposer.java** - Recursive BCNF decomposition; every check uses closures under the original FDs, and final relations carry their projected FDs
- **ThreeNfSynthesizer.java** - 3NF synthesis from the minimal cover
- **DependencyPreservationChecker.java** - Standard fixed-point preservation test
- **LosslessJoinChecker.java** - Binary shortcut and the chase for n-way decompositions
- **FdUtil.java** - FD and attribute-set parsing and validation
- **FdOrder.java** - Canonical order, so results don't depend on hash-set iteration
- **FdProjector.java** - Minimal cover of the projection of F⁺ onto a sub-schema (used by BCNF and 3NF)
- **FdBitIndex.java** - Bitmask closures used by the subset-enumerating algorithms
- **AnalysisLimits.java** - `MAX_ATTRIBUTES` (20)

### Service Classes (10)
Business logic between the UI and the algorithms. Each service works on the current relation held by `RelationService`.

### JavaFX (5)
- **MainController.java** - Handles all UI events for `main.fxml`
- **ResultFormatter.java** - Builds the report text shown in each tab; unit-tested without a display
- **SampleRelations.java** - The built-in example relations
- **UnexpectedErrorHandler.java** - Default uncaught-exception handler that logs and shows an error dialog
- **main.fxml** - The application window

### Configuration (1)
- **AppConfig.java** - Spring bean definitions for the algorithm classes

---

## Build & Execution

```bash
./mvnw -B verify       # build and run all tests (mvnw.cmd on Windows)
./mvnw javafx:run      # run the application
build-exe.bat          # build target/dist/Normalizer/Normalizer.exe (Windows)
```

---

## Dependencies

**Spring Boot 3.5.14** - Spring Boot Starter (core)

**JavaFX 21.0.9** - javafx-controls, javafx-fxml

**Testing** - JUnit 5 (via spring-boot-starter-test)

---

## Class Relationships

```
Launcher
└── JavaFX App (MainApp)
    └── Spring Boot Context (DbmsApplication)
        └── MainController + main.fxml
            ├── RelationService
            ├── FdService
            ├── ClosureService               → ClosureComputer
            ├── CandidateKeyService          → CandidateKeyFinder
            ├── NormalFormService            → NormalFormChecker
            ├── MinimalCoverService          → MinimalCoverComputer
            ├── BcnfDecompositionService     → BcnfDecomposer
            ├── ThreeNfService               → ThreeNfSynthesizer
            ├── DecompositionAnalysisService → DependencyPreservationChecker, LosslessJoinChecker
            └── ExplanationService

Models:
- Relation → [attribute names + FunctionalDependency]
- CandidateKey → [attribute names]
- NormalFormResult → [NormalForm enum + violations]
- DecompositionStep → [original relation + decomposed relations]
```

---

## Development Notes

- **Separation of concerns**: the UI (`MainController`, `main.fxml`) is separate from the services and algorithms.
- **Dependency injection**: Spring wires the services, the controller and the algorithm classes (constructor injection via `AppConfig`).
- **Testability**: algorithms are plain classes; the decomposition, key-finding and normal-form tests check results against brute-force answers on random inputs, and `MainControllerUiTest` clicks through the real window (skipped without a display).

## Quick Start for Developers

1. Read `bugreport.md` and `AGENTS.md`
2. Run `./mvnw -B verify`
3. Run `./mvnw javafx:run`
4. Add tests for new features in `src/test/`, and update `bugreport.md` in the same commit
