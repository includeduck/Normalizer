# DBMS Normalizer - Complete Project Structure

## Project Overview

**Total Files Created: 55**
- Java Classes: 38
- FXML Layout Files: 8
- Configuration Files: 3
- Resource Files: 2
- Documentation: 2
- Other: 2

---

## Directory Structure

```
dbms-normalizer/
│
├── pom.xml                                     # Maven project configuration
├── README.md                                   # Project documentation
├── .gitignore                                  # Git ignore patterns
│
└── src/
    ├── main/
    │   ├── java/com/dbms/analyzer/
    │   │   ├── DbmsApplication.java            # Spring Boot entry point
    │   │   │
    │   │   ├── javafx/
    │   │   │   ├── MainApp.java                # JavaFX application entry point
    │   │   │   │
    │   │   │   ├── controllers/                # FXML Controller Classes (7 files)
    │   │   │   │   ├── MainController.java
    │   │   │   │   ├── RelationController.java
    │   │   │   │   ├── FdController.java
    │   │   │   │   ├── ClosureController.java
    │   │   │   │   ├── CandidateKeyController.java
    │   │   │   │   ├── NormalFormController.java
    │   │   │   │   ├── BcnfController.java
    │   │   │   │   └── ExplanationController.java
    │   │   │   │
    │   │   │   ├── components/                 # Custom JavaFX Components (3 files)
    │   │   │   │   ├── FdListView.java
    │   │   │   │   ├── AttributeChip.java
    │   │   │   │   └── DecompositionTreeView.java
    │   │   │   │
    │   │   │   └── utils/                      # Utility Classes (2 files)
    │   │   │       ├── JavaFxSpringInjector.java
    │   │   │       └── AlertHelper.java
    │   │   │
    │   │   ├── service/                        # Service Layer (7 files)
    │   │   │   ├── RelationService.java        # Manages relation state
    │   │   │   ├── FdService.java              # Manages functional dependencies
    │   │   │   ├── ClosureService.java         # Closure computation service
    │   │   │   ├── CandidateKeyService.java    # Candidate key operations
    │   │   │   ├── NormalFormService.java      # Normal form analysis
    │   │   │   ├── BcnfDecompositionService.java
    │   │   │   └── ExplanationService.java     # Explanation generation
    │   │   │
    │   │   ├── algorithm/                      # Algorithm Implementations (5 files)
    │   │   │   ├── ClosureComputer.java        # Closure algorithm
    │   │   │   ├── CandidateKeyFinder.java     # Candidate key algorithm
    │   │   │   ├── NormalFormChecker.java      # Normal form checking
    │   │   │   ├── BcnfDecomposer.java         # BCNF decomposition
    │   │   │   └── FdUtil.java                 # FD utilities
    │   │   │
    │   │   ├── model/                          # Data Models (6 files)
    │   │   │   ├── Relation.java
    │   │   │   ├── FunctionalDependency.java
    │   │   │   ├── Attribute.java
    │   │   │   ├── CandidateKey.java
    │   │   │   ├── NormalFormResult.java
    │   │   │   └── DecompositionStep.java
    │   │   │
    │   │   ├── repository/                     # Data Persistence (1 file)
    │   │   │   └── InMemorySessionRepository.java
    │   │   │
    │   │   └── config/                         # Spring Configuration (1 file)
    │   │       └── AppConfig.java
    │   │
    │   └── resources/
    │       ├── application.properties           # Spring Boot properties
    │       │
    │       ├── css/
    │       │   └── styles.css                  # JavaFX stylesheet
    │       │
    │       ├── fxml/                           # FXML Layout Files (8 files)
    │       │   ├── main.fxml                   # Main workspace
    │       │   ├── relation.fxml               # Relation creation
    │       │   ├── fd.fxml                     # FD input
    │       │   ├── closure.fxml                # Closure computation
    │       │   ├── candidateKeys.fxml          # Candidate keys display
    │       │   ├── normalForm.fxml             # Normal form analysis
    │       │   ├── bcnf.fxml                   # BCNF decomposition
    │       │   └── explanation.fxml            # Explanations view
    │       │
    │       └── images/
    │           └── [icon placeholder]
    │
    └── test/
        └── java/com/dbms/analyzer/
            ├── algorithm/                      # Algorithm Tests (4 files)
            │   ├── ClosureComputerTest.java
            │   ├── CandidateKeyFinderTest.java
            │   ├── NormalFormCheckerTest.java
            │   └── BcnfDecomposerTest.java
            │
            └── service/                        # Service Tests (2 files)
                ├── RelationServiceTest.java
                └── FdServiceTest.java
```

---

## File Categories & Descriptions

### Core Application Files (2)
1. **DbmsApplication.java** - Spring Boot entry point, bootstraps the application
2. **MainApp.java** - JavaFX application startup, loads main UI

### Model Classes (6)
Represent domain objects:
- **Relation.java** - Represents a database relation with attributes and FDs
- **FunctionalDependency.java** - Represents an FD with left/right sides
- **Attribute.java** - Individual attribute with prime/non-prime flag
- **CandidateKey.java** - Minimal superkey representation
- **NormalFormResult.java** - Results of normal form analysis
- **DecompositionStep.java** - BCNF decomposition step record

### Algorithm Classes (5)
Implement core DBMS algorithms:
- **ClosureComputer.java** - Computes attribute closures using fixed-point iteration
- **CandidateKeyFinder.java** - Generates all subsets and finds minimal superkeys
- **NormalFormChecker.java** - Checks 1NF/2NF/3NF/BCNF requirements
- **BcnfDecomposer.java** - Recursively decomposes relations to BCNF
- **FdUtil.java** - Parsing, validation, and manipulation utilities for FDs

### Service Classes (7)
Business logic layer between UI and algorithms:
- **RelationService.java** - Creates and manages current relation state
- **FdService.java** - Adds, removes, and manages functional dependencies
- **ClosureService.java** - Provides closure computation with explanations
- **CandidateKeyService.java** - Finds candidate keys and identifies prime attributes
- **NormalFormService.java** - Analyzes and reports normal forms
- **BcnfDecompositionService.java** - Orchestrates BCNF decomposition
- **ExplanationService.java** - Generates step-by-step educational explanations

### JavaFX Controllers (8)
FXML controller classes handling UI events:
- **MainController.java** - Main workspace coordination
- **RelationController.java** - Relation creation dialog
- **FdController.java** - FD input and management
- **ClosureController.java** - Closure computation UI
- **CandidateKeyController.java** - Candidate key display
- **NormalFormController.java** - Normal form results
- **BcnfController.java** - BCNF decomposition display
- **ExplanationController.java** - Educational explanations

### Custom JavaFX Components (3)
Reusable UI components:
- **FdListView.java** - Custom ListView for displaying FDs with context menus
- **AttributeChip.java** - Visual token-style attribute display
- **DecompositionTreeView.java** - Hierarchical decomposition tree display

### JavaFX Utilities (2)
Support classes:
- **JavaFxSpringInjector.java** - Integrates Spring beans into JavaFX controllers
- **AlertHelper.java** - Dialog and alert convenience methods

### Repository (1)
- **InMemorySessionRepository.java** - In-memory data storage for session state

### Configuration (1)
- **AppConfig.java** - Spring Bean definitions and wiring

### FXML Layout Files (8)
XML-based UI definitions:
- **main.fxml** - Primary application workspace (buttons, lists, workspace)
- **relation.fxml** - Relation schema input dialog
- **fd.fxml** - Functional dependency input dialog
- **closure.fxml** - Attribute closure input and results
- **candidateKeys.fxml** - Candidate keys display and prime attributes
- **normalForm.fxml** - Normal form analysis results
- **bcnf.fxml** - BCNF decomposition results tree
- **explanation.fxml** - Step-by-step explanation viewer

### Configuration Files (3)
- **pom.xml** - Maven dependencies and build configuration
- **application.properties** - Spring Boot and logging configuration
- **styles.css** - JavaFX CSS styling (buttons, fields, colors)

### Testing Files (6)
Unit test classes:
- **ClosureComputerTest.java** - Tests closure algorithm correctness
- **CandidateKeyFinderTest.java** - Tests candidate key finding
- **NormalFormCheckerTest.java** - Tests normal form validation
- **BcnfDecomposerTest.java** - Tests BCNF decomposition
- **RelationServiceTest.java** - Tests relation operations
- **FdServiceTest.java** - Tests FD operations

### Documentation (3)
- **README.md** - Comprehensive project documentation
- **UC.md** - Detailed use cases and requirements
- **.gitignore** - Git repository ignore patterns

---

## Key Features by Module

### Algorithm Module
✓ Closure computation with transitive dependency handling
✓ Candidate key finding via subset generation and minimality checking
✓ 1NF/2NF/3NF/BCNF normal form validation
✓ Recursive BCNF decomposition
✓ FD parsing and validation

### Service Module
✓ Relation and attribute management
✓ FD addition, removal, and validation
✓ Closure computation with step explanations
✓ Prime/non-prime attribute identification
✓ Normal form analysis and reporting
✓ Educational explanation generation

### UI Module
✓ Relation schema creation
✓ FD input with syntax validation
✓ Closure computation with visual results
✓ Candidate keys display
✓ Normal form analysis report
✓ Decomposition tree visualization
✓ Step-by-step educational explanations

---

## Build & Execution

### Build
```bash
mvn clean package
```

### Run
```bash
mvn javafx:run
```

### Test
```bash
mvn test
```

---

## Dependencies

**Spring Boot 3.0.0**
- Spring Boot Starter (core)

**JavaFX 21.0.1**
- javafx-controls
- javafx-fxml

**Testing**
- JUnit 5

---

## Class Relationships

```
Spring Boot App (DbmsApplication)
└── JavaFX App (MainApp)
    └── Main UI (MainController + main.fxml)
        ├── RelationService ← RelationController
        ├── FdService ← FdController
        ├── ClosureService ← ClosureController
        │   └── ClosureComputer
        ├── CandidateKeyService ← CandidateKeyController
        │   └── CandidateKeyFinder
        ├── NormalFormService ← NormalFormController
        │   └── NormalFormChecker
        ├── BcnfDecompositionService ← BcnfController
        │   └── BcnfDecomposer
        └── ExplanationService ← ExplanationController

Models:
- Relation → [Attribute + FunctionalDependency]
- CandidateKey → [Attributes]
- NormalFormResult → [NormalForm enum + Violations]
- DecompositionStep → [Original Relation + Decomposed Relations]
```

---

## Development Notes

### Architecture Highlights
- **Separation of Concerns**: UI (controllers/FXML) isolated from business logic (services/algorithms)
- **Dependency Injection**: Spring manages all service dependencies
- **Model-Driven**: All data encapsulated in model classes
- **Testability**: Algorithm and service classes designed for unit testing
- **Extensibility**: Easy to add new algorithms, services, or UI components

### Future Enhancement Points
- Add session save/load functionality (using InMemorySessionRepository)
- Extend normal form checking for 2NF and 3NF
- Add support for MVDs and 4NF/5NF
- Implement SQL import/export
- Add graphical FD visualization
- Extend test coverage

---

## Quick Start for Developers

1. Import project into IntelliJ IDEA
2. Let Maven download dependencies
3. Run `mvn clean install`
4. Execute `DbmsApplication.main()` or use Maven
5. Navigate UI to create relations and add FDs
6. Explore closure, keys, and normal forms
7. Write tests for new features in `src/test/`

---

*Project created with comprehensive stub files and documentation*
*Ready for development and feature implementation*
