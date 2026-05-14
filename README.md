# DBMS Normalization & Functional Dependency Analyzer

An educational JavaFX application for analyzing relational database schemas using concepts from Database Management Systems (DBMS), including Functional Dependencies (FDs), Attribute Closure, Candidate Keys, Normal Forms, and BCNF decomposition.

## Overview

This application helps students, instructors, and database learners analyze relation schemas and understand database normalization rules through an intuitive graphical interface.

## Features

- **Relation Schema Creation**: Define relations with multiple attributes
- **Functional Dependency Management**: Add and validate functional dependencies
- **Attribute Closure Computation**: Calculate closure of attribute sets with step-by-step explanations
- **Candidate Key Detection**: Find all candidate keys and identify prime attributes
- **Normal Form Analysis**: Detect the highest normal form (1NF, 2NF, 3NF, BCNF) satisfied by a relation
- **BCNF Decomposition**: Recursively decompose relations to achieve BCNF
- **Educational Explanations**: View detailed algorithm steps and reasoning

## Technology Stack

- **JavaFX**: Desktop GUI framework
- **Spring Boot**: Backend architecture and dependency injection
- **Maven**: Build and dependency management
- **JUnit**: Unit testing framework
- **Java 17+**: Programming language

## Project Structure

```
dbms-normalizer/
├── pom.xml                                   # Maven configuration
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/dbms/analyzer/
│   │   │   ├── DbmsApplication.java         # Spring Boot entry point
│   │   │   ├── javafx/
│   │   │   │   ├── MainApp.java             # JavaFX entry point
│   │   │   │   ├── controllers/             # FXML controllers
│   │   │   │   ├── views/                   # FXML layout files
│   │   │   │   ├── components/              # Custom JavaFX components
│   │   │   │   └── utils/                   # Utility classes
│   │   │   ├── service/                     # Business logic services
│   │   │   ├── algorithm/                   # Core DBMS algorithms
│   │   │   ├── model/                       # Data models
│   │   │   ├── repository/                  # Data persistence
│   │   │   └── config/                      # Spring configuration
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── css/styles.css
│   │       ├── fxml/                        # FXML layout files
│   │       └── images/
│   └── test/
│       └── java/com/dbms/analyzer/          # Unit tests
└── .gitignore
```

## Installation & Setup

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- JavaFX SDK 21+

### Build Instructions

1. Clone the repository:
```bash
git clone <repository-url>
cd dbms-normalizer
```

2. Build the project:
```bash
mvn clean package
```

3. Run the application:
```bash
mvn javafx:run
```

## Usage Guide

### Creating a Relation

1. Click "Create Relation" button
2. Enter relation schema in format: `R(A,B,C,D)`
3. The system validates syntax and stores the relation

### Adding Functional Dependencies

1. Click "Add FD" button
2. Enter dependencies in format: `AB -> CD` or `A -> BC`
3. The system validates all attributes exist in the relation

### Computing Attribute Closure

1. Click "Compute Closure" button
2. Enter attributes to compute: `ABC`
3. View step-by-step closure computation
4. See final closure result

### Finding Candidate Keys

1. Click "Find Candidate Keys" button
2. System automatically finds all minimal superkeys
3. View prime and non-prime attributes

### Analyzing Normal Forms

1. Click "Analyze Normal Form" button
2. System checks 1NF, 2NF, 3NF, and BCNF
3. View the highest normal form satisfied
4. See any violations and explanations

### BCNF Decomposition

1. Click "BCNF Decomposition" button
2. System recursively decomposes the relation
3. View all resulting relations in BCNF
4. See decomposition details

## Algorithm Details

### Closure Computation
Uses iterative fixed-point algorithm to compute attribute closures by repeatedly applying functional dependencies until no new attributes can be added.

### Candidate Key Finding
Generates all subsets of attributes, computes closures, identifies superkeys, and filters to find minimal ones.

### Normal Form Checking
Implements checks for:
- **1NF**: All attributes are atomic
- **2NF**: No partial dependencies on candidate keys
- **3NF**: No transitive dependencies
- **BCNF**: Every FD's left side is a superkey

### BCNF Decomposition
Recursively identifies BCNF violations and decomposes relations until all satisfy BCNF.

## Core Classes

### Model Classes
- `Relation`: Represents a relation schema
- `FunctionalDependency`: Represents an FD
- `Attribute`: Individual attribute with prime status
- `CandidateKey`: Set of attributes forming a candidate key
- `NormalFormResult`: Result of normal form analysis
- `DecompositionStep`: Records a decomposition step

### Algorithm Classes
- `ClosureComputer`: Computes attribute closures
- `CandidateKeyFinder`: Finds all candidate keys
- `NormalFormChecker`: Checks normal forms
- `BcnfDecomposer`: Decomposes to BCNF
- `FdUtil`: FD parsing and validation utilities

### Service Classes
- `RelationService`: Manages relation state
- `FdService`: Manages functional dependencies
- `ClosureService`: Closure computation service
- `CandidateKeyService`: Candidate key operations
- `NormalFormService`: Normal form analysis
- `BcnfDecompositionService`: BCNF decomposition
- `ExplanationService`: Generates explanations

## Testing

Run unit tests:
```bash
mvn test
```

Test coverage includes:
- Algorithm correctness
- Service layer functionality
- Model validation
- FD parsing and validation

## Future Enhancements

- Save and load analysis sessions
- SQL schema export functionality
- Dependency preservation checker
- Minimal cover visualization
- Interactive decomposition graphs
- Report generation (PDF/Excel)
- Support for Multi-Valued Dependencies (MVDs)
- 4NF and 5NF normalization support
- Database import/export integration
- Advanced visualization features

## Documentation

For detailed documentation on DBMS concepts, see the included Use Case document:
- [UC.md](UC.md) - Detailed use cases and requirements

## Contributing

Contributions are welcome! Please ensure:
1. Code follows project structure
2. Unit tests are included for new features
3. Algorithm implementations are well-commented
4. JavaFX UI remains user-friendly

## License

[Specify your license here]

## Support

For issues, questions, or suggestions, please create an issue or contact the development team.

## Acknowledgments

This project is designed as an educational tool for learning DBMS normalization concepts. It implements algorithms from standard database theory textbooks.
