# DBMS Normalization & Functional Dependency Analyzer

## Detailed Use Case Document

## 1. Introduction

### Purpose
This application helps users analyze relational schemas using concepts from Database Management Systems (DBMS), including Functional Dependencies (FDs), Attribute Closure, Candidate Keys, Normal Forms, and BCNF decomposition.

### Technology Stack
- JavaFX for desktop GUI
- Spring Boot for backend architecture
- IntelliJ IDEA as the development IDE

### Core Features
- Functional Dependency Analysis
- Attribute Closure Computation
- Candidate Key Detection
- Normal Form Detection
- BCNF Decomposition
- Step-by-step Educational Explanations

## 2. Actors

**Primary Actor: User**  
The user may be a student, instructor, or database learner using the system to analyze relation schemas and normalization rules.

## 3. Functional Requirements

The system shall allow users to:
- Create relation schemas
- Add functional dependencies
- Validate dependency syntax
- Compute attribute closures
- Find candidate keys
- Detect highest normal form
- Normalize relations to BCNF
- View detailed algorithm explanations

## 4. Use Cases

### UC-1: Create Relation Schema

**Goal:** Allow users to define a relation and its attributes.

**Preconditions:**
- Application is running
- User is on the main workspace

**Main Success Scenario:**
1. User selects 'Create Relation'.
2. System displays relation input field.
3. User enters relation schema such as R(A,B,C,D,E).
4. System validates syntax and duplicate attributes.
5. System stores relation internally.
6. System confirms successful creation.

**Alternative Flows:**
- Duplicate attributes detected.
- Invalid relation syntax.

---

### UC-2: Add Functional Dependencies

**Goal:** Allow users to define functional dependencies.

**Preconditions:**
- Relation schema already exists

**Main Success Scenario:**
1. User selects 'Add Functional Dependency'.
2. System displays FD input area.
3. User enters FDs such as A -> B and C -> D,E.
4. System parses and validates each FD.
5. System verifies all attributes exist in the relation.
6. System stores structured FD objects.
7. System displays formatted dependencies.

**Alternative Flows:**
- Undefined attribute detected.
- Invalid dependency syntax.

---

### UC-3: Compute Attribute Closure

**Goal:** Compute closure of an attribute set.

**Preconditions:**
- Relation exists
- Functional dependencies exist

**Main Success Scenario:**
1. User selects 'Compute Closure'.
2. System requests an attribute set.
3. User enters input such as (A,C)+.
4. System initializes closure with the selected attributes.
5. System repeatedly applies matching FDs.
6. System stops when no more attributes can be added.
7. System displays the final closure result.
8. System optionally displays derivation steps.

**Alternative Flows:**
- User enters invalid attributes.

---

### UC-4: Find Candidate Keys

**Goal:** Determine all candidate keys.

**Preconditions:**
- Relation exists
- Functional dependencies exist

**Main Success Scenario:**
1. User selects 'Find Candidate Keys'.
2. System generates attribute subsets.
3. System computes closures for subsets.
4. System identifies superkeys.
5. System eliminates non-minimal superkeys.
6. System displays all candidate keys.
7. System highlights prime attributes.

**Alternative Flows:**
- Only one candidate key exists.

---

### UC-5: Detect Highest Normal Form

**Goal:** Determine the highest normal form satisfied.

**Preconditions:**
- Candidate keys have been identified

**Main Success Scenario:**
1. User selects 'Analyze Normal Form'.
2. System checks 1NF, 2NF, 3NF, and BCNF.
3. System detects normalization violations.
4. System explains why a violation occurs.
5. System displays the highest valid normal form.

**Alternative Flows:**
- Relation already satisfies BCNF.

---

### UC-6: Normalize Relation to BCNF

**Goal:** Perform recursive BCNF decomposition.

**Preconditions:**
- A BCNF violation exists

**Main Success Scenario:**
1. User selects 'Normalize to BCNF'.
2. System identifies violating dependencies.
3. System computes decomposition relations.
4. System recursively analyzes decomposed schemas.
5. System stops once all relations satisfy BCNF.
6. System displays decomposition tree.

**Alternative Flows:**
- Dependency preservation warning.

---

### UC-7: View Step-by-Step Explanation

**Goal:** Provide educational reasoning behind computations.

**Preconditions:**
- Analysis has already been performed

**Main Success Scenario:**
1. User enables explanation mode.
2. System displays detailed reasoning.
3. System shows closure derivations and normalization logic.
4. System highlights violating dependencies and superkeys.

**Alternative Flows:**
- Explanation mode disabled.

## 5. Non-Functional Requirements

| Category        | Requirement                                                      |
|----------------|------------------------------------------------------------------|
| Performance    | Closure and normalization calculations should complete efficiently for moderate schemas. |
| Usability      | The application should remain beginner-friendly and educational. |
| Reliability    | Invalid input should never crash the application.               |
| Maintainability| UI logic and DBMS algorithms should remain separated.           |
| Scalability    | Architecture should support future additions such as 4NF and 5NF. |

## 6. Future Enhancements

- Save and load analysis sessions
- SQL schema export
- Dependency preservation checker
- Minimal cover visualization
- Interactive decomposition graphs
- Report generation
- Support for Multi-Valued Dependencies (MVDs)
- 4NF and 5NF normalization support