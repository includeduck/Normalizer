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
- 📊 **Normal Form Analysis**: Detect the highest normal form (2NF, 3NF, BCNF) satisfied by a relation. 1NF is assumed: every attribute is treated as atomic.
- 🧹 **Minimal Cover**: Reduce the FDs to a minimal (canonical) cover, step by step.
- ✂️ **BCNF Decomposition**: Recursively decompose relations to achieve BCNF.
- 🧱 **3NF Synthesis**: Build a lossless, dependency-preserving 3NF decomposition.
- ✅ **Decomposition Checks**: Test the latest decomposition for dependency preservation and lossless join.
- 🎓 **Educational Explanations**: View detailed algorithm steps and reasoning.
- 📚 **Built-in Examples**: Load ready-made relations (student/course, employee/department, library loans).

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
Normalizer/
├── pom.xml                                   # Maven configuration
├── README.md
├── bugreport.md                              # Known issues and their status
├── AGENTS.md                                 # Instructions for AI agents
├── MarkDown/                                 # Use cases, progress, project structure
├── build-exe.bat / build-exe.ps1             # Windows .exe packaging
├── src/
│   ├── main/
│   │   ├── java/com/dbms/analyzer/
│   │   │   ├── Launcher.java                # Desktop application entry point
│   │   │   ├── DbmsApplication.java         # Spring Boot configuration
│   │   │   ├── javafx/                      # JavaFX app, main controller, error handler
│   │   │   ├── service/                     # Business logic services
│   │   │   ├── algorithm/                   # Core DBMS algorithms
│   │   │   ├── model/                       # Data models
│   │   │   └── config/                      # Spring configuration
│   │   └── resources/                       # FXML, CSS, tutorial text, properties
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
   git clone https://github.com/includeduck/Normalizer.git
   cd Normalizer
   ```

2. **Build the project and run the tests:**
   ```bash
   mvn -B verify
   ```
   `MainControllerUiTest` opens the real window, so it runs only where a display is available and is skipped otherwise (for example in CI).

3. **Run the tests only:**
   ```bash
   mvn test
   ```

4. **Run the application:**
   ```bash
   mvn javafx:run
   ```

5. **Build a Windows executable** (optional, needs a JDK 21+ with `jpackage`):
   ```bash
   build-exe.bat
   ```
   The app is written to `target/dist/Normalizer/Normalizer.exe`; keep it inside that folder.

## 📖 Usage Guide

### 📝 Creating a Relation
1. Enter a schema in the Relation field, e.g., `R(A,B,C,D)` or `Enrollment(student_id,course_id,grade)`
2. Click **Create / Replace** or press `Enter`.
3. The summary pane updates with the active relation.

> Relations are limited to **20 attributes**: key finding and decomposition take exponential time in the number of attributes.

### ➕ Adding Functional Dependencies
1. Enter a dependency in the Functional Dependency field.
2. Use compact form like `AB -> CD` or comma-separated multi-character attributes like `student_id, course_id -> grade`.
   - Compact input is split into the relation's attribute names (`ab` on `R(a,b)`, `A1B` on `R(A1,B)`). If it could be split more than one way, you'll be asked to use commas.
   - Relation and attribute names must start with a letter or underscore and contain only letters, digits and underscores.
3. Click **Add FD** or press `Enter`. Duplicates are rejected.
4. To remove one, select it in the summary list and click **Remove Selected**.

### 📚 Loading an Example
1. Pick an example from the drop-down and click **Load Example**.
2. The relation, its FDs and a closure input are filled in; the current relation is only replaced if the example loads completely.

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
2. System checks 2NF, 3NF, and BCNF (1NF is assumed).
3. View the highest normal form satisfied along with any violations and explanations.

### 🧹 Minimal Cover
1. Click **Minimal Cover**.
2. View each step (singleton right sides, extraneous attributes, redundant FDs) in the **Minimal Cover** tab.

### 🪓 BCNF Decomposition
1. Click **BCNF**.
2. System recursively decomposes the relation.
3. View each step and the resulting relations, with their FDs, in the **Decomposition** tab.

### 🧱 3NF Synthesis
1. Click **3NF**.
2. View the minimal cover, the relations built from it and the final 3NF relations in the **Decomposition** tab.

### ✅ Checking a Decomposition
1. Run **BCNF** or **3NF** first.
2. Click **Check Result**.
3. The **Check Result** tab shows which FDs are preserved or lost and whether the join is lossless.

### 💡 Explanation and Reset
- **Explain** summarizes the relation, its keys and which normal forms hold, in the **Explanation** tab.
- **Clear** resets the session; the **App Tutorial** tab stays available.

## 🧠 Algorithm Details

<details>
<summary><b>Closure Computation</b></summary>
<br>
Uses iterative fixed-point algorithm to compute attribute closures by repeatedly applying functional dependencies until no new attributes can be added.
</details>

<details>
<summary><b>Candidate Key Finding</b></summary>
<br>
Attributes that never appear on a right side are in every key; attributes that appear only on right sides are in none. Only the remaining attributes are searched, smallest subsets first, using bitmask closures, so every key found is minimal.
</details>

<details>
<summary><b>Normal Form Checking</b></summary>
<br>
Implements checks for:
<ul>
  <li><b>1NF:</b> Assumed, not checked: the tool treats every attribute as atomic</li>
  <li><b>2NF:</b> No partial dependencies on candidate keys</li>
  <li><b>3NF:</b> For every FD X → A, X is a superkey or A is prime</li>
  <li><b>BCNF:</b> Every non-trivial FD's left side is a superkey</li>
</ul>
</details>

<details>
<summary><b>BCNF Decomposition</b></summary>
<br>
Recursively identifies BCNF violations and decomposes relations until all satisfy BCNF. Sub-relations are checked against every implied FD (F⁺), and each final relation shows a minimal cover of its projected FDs.
</details>

<details>
<summary><b>3NF Synthesis and Decomposition Checks</b></summary>
<br>
3NF synthesis builds one relation per left side of the minimal cover, adds a key relation if needed and removes subsumed relations. Dependency preservation uses the standard fixed-point test; the lossless join uses the binary shortcut or the chase.
</details>

## 🧩 Core Classes

- **Model**: `Relation`, `FunctionalDependency`, `CandidateKey`, `NormalFormResult`, `DecompositionStep`
- **Algorithm**: `ClosureComputer`, `CandidateKeyFinder`, `NormalFormChecker`, `MinimalCoverComputer`, `BcnfDecomposer`, `ThreeNfSynthesizer`, `DependencyPreservationChecker`, `LosslessJoinChecker`, `FdProjector`, `FdUtil`, `FdOrder`
- **Service**: `RelationService`, `FdService`, `ClosureService`, `CandidateKeyService`, `NormalFormService`, `MinimalCoverService`, `BcnfDecompositionService`, `ThreeNfService`, `DecompositionAnalysisService`, `ExplanationService`
- **JavaFX**: `MainApp`, `MainController`, `ResultFormatter`, `SampleRelations`, `UnexpectedErrorHandler`

See [MarkDown/PROJECT_STRUCTURE.md](MarkDown/PROJECT_STRUCTURE.md) for the full layout.

## 🔮 Future Enhancements

- [ ] Save and load analysis sessions
- [ ] SQL schema export
- [ ] Minimal cover visualization
- [ ] Interactive decomposition graphs
- [ ] Report generation

The project's scope is normalization up to BCNF; 4NF/5NF and multi-valued dependencies are not planned.

## 📚 Documentation
- [MarkDown/UC.md](MarkDown/UC.md): use cases and requirements
- [MarkDown/Progress.md](MarkDown/Progress.md): what is implemented and what remains
- [MarkDown/PROJECT_STRUCTURE.md](MarkDown/PROJECT_STRUCTURE.md): project layout and class relationships
- [bugreport.md](bugreport.md): known issues and their status

## 🤝 Contributing
Contributions are welcome! Please ensure:
1. Code follows project structure.
2. Unit tests are included for new features; algorithm changes need tests that check correctness, not just that a result exists.
3. `mvn -B verify` passes.
4. `bugreport.md` is updated in the same commit when a change fixes, finds or affects an issue (see [AGENTS.md](AGENTS.md)).
5. JavaFX UI remains user-friendly.

## 💬 Support
For issues, questions, or suggestions, please [create an issue](https://github.com/includeduck/Normalizer/issues).

---
*This project is designed as an educational tool for learning DBMS normalization concepts. It implements algorithms from standard database theory textbooks.*
