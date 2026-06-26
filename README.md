<div align="center">
  <h1>🗄️ DBMS Normalization & FD Analyzer</h1>
  <p>
    <i>An educational JavaFX application for analyzing relational database schemas using concepts from Database Management Systems (DBMS).</i>
  </p>
  <p>
    <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=oracle" alt="Java 21">
    <img src="https://img.shields.io/badge/JavaFX-Desktop-blue?style=flat-square&logo=java" alt="JavaFX">
    <img src="https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?style=flat-square&logo=spring-boot" alt="Spring Boot">
    <img src="https://img.shields.io/badge/Maven-Build-red?style=flat-square&logo=apache-maven" alt="Maven">
  </p>
</div>

---

## 🌟 Overview

This application helps students, instructors, and database learners analyze relation schemas and understand database normalization rules through an intuitive graphical interface.

## ✨ Features

- 🏗️ **Relation Schema Creation**: Define relations with multiple attributes.
- 🔗 **Functional Dependency Management**: Add and validate functional dependencies.
- 🧮 **Attribute Closure Computation**: Calculate closure of attribute sets with step-by-step explanations.
- 🔑 **Candidate Key Detection**: Find all candidate keys and identify prime attributes.
- 📊 **Normal Form Analysis**: Detect the highest normal form (1NF, 2NF, 3NF, BCNF) satisfied by a relation.
- ✂️ **BCNF Decomposition**: Recursively decompose relations to achieve BCNF.
- 🎓 **Educational Explanations**: View detailed algorithm steps and reasoning.

## 🛠️ Technology Stack

| Technology | Description |
|------------|-------------|
| **JavaFX** | Desktop GUI framework |
| **Spring Boot** | Backend architecture and dependency injection |
| **Maven** | Build and dependency management |
| **JUnit** | Unit testing framework |
| **Java 21** | Programming language and build target |

## 📁 Project Structure

```text
dbms-normalizer/
├── pom.xml                                   # Maven configuration
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/dbms/analyzer/
│   │   │   ├── Launcher.java                # Desktop application entry point
│   │   │   ├── DbmsApplication.java         # Spring Boot configuration
│   │   │   ├── javafx/                      # UI components & controllers
│   │   │   ├── service/                     # Business logic services
│   │   │   ├── algorithm/                   # Core DBMS algorithms
│   │   │   ├── model/                       # Data models
│   │   │   ├── repository/                  # Data persistence
│   │   │   └── config/                      # Spring configuration
│   │   └── resources/                       # FXML, CSS, Images, Properties
│   └── test/
│       └── java/com/dbms/analyzer/          # Unit tests
└── .gitignore
```

## 🚀 Installation & Setup

### 📋 Prerequisites

- **Java 21** or higher
- **Maven 3.6.3** or higher
> *Note: No separate JavaFX SDK is required; Maven resolves JavaFX modules automatically.*

### ⚙️ Build Instructions

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd dbms-normalizer
   ```

2. **Build the project:**
   ```bash
   mvn clean package
   ```

3. **Run the tests:**
   ```bash
   mvn test
   ```

4. **Run the application:**
   ```bash
   mvn javafx:run
   ```

## 📖 Usage Guide

### 📝 Creating a Relation
1. Enter a schema in the Relation field, e.g., `R(A,B,C,D)` or `Enrollment(student_id,course_id,grade)`
2. Click **Create / Replace** or press `Enter`.
3. The summary pane updates with the active relation.

### ➕ Adding Functional Dependencies
1. Enter a dependency in the Functional Dependency field.
2. Use compact form like `AB -> CD` or comma-separated multi-character attributes like `student_id, course_id -> grade`.
3. Click **Add FD** or press `Enter`.

### 🔍 Computing Attribute Closure
1. Enter attributes in the Closure field, e.g., `ABC` or `student_id, course_id`.
2. Click **Compute Closure**.
3. View step-by-step closure computation in the **Closure** tab.

### 🔑 Finding Candidate Keys
1. Click **Keys**.
2. System automatically finds all minimal superkeys.
3. View prime and non-prime attributes in the **Keys** tab and summary pane.

### 📏 Analyzing Normal Forms
1. Click **Normal Form**.
2. System checks 1NF, 2NF, 3NF, and BCNF.
3. View the highest normal form satisfied along with any violations and explanations.

### 🪓 BCNF Decomposition
1. Click the **BCNF Decomposition** button.
2. System recursively decomposes the relation.
3. View all resulting relations in BCNF and decomposition details.

## 🧠 Algorithm Details

<details>
<summary><b>Closure Computation</b></summary>
<br>
Uses iterative fixed-point algorithm to compute attribute closures by repeatedly applying functional dependencies until no new attributes can be added.
</details>

<details>
<summary><b>Candidate Key Finding</b></summary>
<br>
Generates all subsets of attributes, computes closures, identifies superkeys, and filters to find minimal ones.
</details>

<details>
<summary><b>Normal Form Checking</b></summary>
<br>
Implements checks for:
<ul>
  <li><b>1NF:</b> All attributes are atomic</li>
  <li><b>2NF:</b> No partial dependencies on candidate keys</li>
  <li><b>3NF:</b> No transitive dependencies</li>
  <li><b>BCNF:</b> Every FD's left side is a superkey</li>
</ul>
</details>

<details>
<summary><b>BCNF Decomposition</b></summary>
<br>
Recursively identifies BCNF violations and decomposes relations until all satisfy BCNF.
</details>

## 🧩 Core Classes

- **Model**: `Relation`, `FunctionalDependency`, `Attribute`, `CandidateKey`, `NormalFormResult`, `DecompositionStep`
- **Algorithm**: `ClosureComputer`, `CandidateKeyFinder`, `NormalFormChecker`, `BcnfDecomposer`, `FdUtil`
- **Service**: `RelationService`, `FdService`, `ClosureService`, `CandidateKeyService`, `NormalFormService`, `BcnfDecompositionService`, `ExplanationService`

## 🔮 Future Enhancements

- [ ] Save and load analysis sessions
- [ ] SQL schema export functionality
- [ ] Dependency preservation checker
- [ ] Minimal cover visualization
- [ ] Interactive decomposition graphs
- [ ] Report generation (PDF/Excel)
- [ ] Support for Multi-Valued Dependencies (MVDs)
- [ ] 4NF and 5NF normalization support
- [ ] Database import/export integration
- [ ] Advanced visualization features

## 📚 Documentation
For detailed documentation on DBMS concepts, see the included Use Case document: [UC.md](UC.md)

## 🤝 Contributing
Contributions are welcome! Please ensure:
1. Code follows project structure.
2. Unit tests are included for new features.
3. Algorithm implementations are well-commented.
4. JavaFX UI remains user-friendly.

## 📄 License
[Specify your license here]

## 💬 Support
For issues, questions, or suggestions, please [create an issue](#) or contact the development team.

---
*This project is designed as an educational tool for learning DBMS normalization concepts. It implements algorithms from standard database theory textbooks.*
