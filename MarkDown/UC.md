# DBMS Normalization & Functional Dependency Analyzer

## Detailed Use Case Document

## 1. Introduction

### Purpose
This application helps users analyze relational schemas using concepts from Database Management Systems (DBMS), including Functional Dependencies (FDs), Attribute Closure, Candidate Keys, Normal Forms, minimal covers, BCNF decomposition and 3NF synthesis.

### Technology Stack
- JavaFX for desktop GUI
- Spring Boot for backend architecture
- IntelliJ IDEA as the development IDE

### Core Features
- Functional Dependency Analysis
- Attribute Closure Computation
- Candidate Key Detection
- Normal Form Detection
- Minimal Cover Computation
- BCNF Decomposition
- 3NF Synthesis
- Dependency Preservation and Lossless-Join Checks
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
- Compute a minimal cover of the FDs
- Normalize relations to BCNF
- Synthesize a 3NF decomposition
- Check a decomposition for dependency preservation and lossless join
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
- Invalid relation or attribute name (names use letters, digits and underscores, and cannot start with a digit).
- More than 20 attributes.

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
- Undefined attribute detected (the message names it).
- Invalid dependency syntax.
- Ambiguous compact input such as `ABC` when both `A,B,C` and `AB,C` are possible: the user is asked to separate attributes with commas.

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
1. User selects 'Normal Form'.
2. System checks 2NF, 3NF, and BCNF. 1NF is assumed: every attribute is treated as atomic.
3. System detects normalization violations.
4. System explains why a violation occurs.
5. System displays the highest valid normal form.

**Alternative Flows:**
- Relation already satisfies BCNF.

---

### UC-6: Normalize Relation to BCNF

**Goal:** Perform recursive BCNF decomposition.

**Preconditions:**
- A relation exists

**Main Success Scenario:**
1. User selects 'BCNF'.
2. System finds a violating dependency, checking sub-relations against all implied FDs (F⁺).
3. System splits the relation into X⁺ and X ∪ (R − X⁺).
4. System repeats for each part until every relation satisfies BCNF.
5. System displays each numbered step and the final relations with their projected FDs.

**Alternative Flows:**
- Relation already satisfies BCNF: a single relation is returned.
- Some dependencies cannot be preserved in BCNF: the user sees this with 'Check Result' (UC-9).

---

### UC-7: View Step-by-Step Explanation

**Goal:** Provide educational reasoning behind computations.

**Preconditions:**
- A relation exists

**Main Success Scenario:**
1. User selects 'Explain'.
2. System summarizes the relation and its FDs.
3. System lists candidate keys and prime/non-prime attributes.
4. System shows which normal forms hold and lists every violation.

Every other action (closure, keys, minimal cover, BCNF, 3NF, Check Result) also shows its own step-by-step trace in its tab.

---

### UC-8: Synthesize a 3NF Decomposition

**Goal:** Produce a lossless, dependency-preserving 3NF decomposition.

**Preconditions:**
- A relation exists

**Main Success Scenario:**
1. User selects '3NF'.
2. System computes a minimal cover.
3. System creates one relation per left-hand side in the cover.
4. System adds a relation for a candidate key if none contains one.
5. System removes duplicate and subsumed relations.
6. System displays each step and the final relations with their FDs.

---

### UC-9: Check a Decomposition

**Goal:** Test the latest decomposition for dependency preservation and lossless join.

**Preconditions:**
- A BCNF or 3NF decomposition has been run

**Main Success Scenario:**
1. User selects 'Check Result'.
2. System tests every original FD for preservation and shows how each was derived.
3. System tests the lossless-join property (binary test or chase).
4. System shows both results in the Check Result tab.

**Alternative Flows:**
- No decomposition has been run: the user is asked to run BCNF or 3NF first.

## 5. Non-Functional Requirements

| Category        | Requirement                                                      |
|----------------|------------------------------------------------------------------|
| Performance    | Relations of up to 20 attributes are supported; analyses at that size complete in about a second or less. |
| Usability      | The application should remain beginner-friendly and educational. |
| Reliability    | Invalid input is reported with a message naming the problem; unexpected errors are shown in an error dialog. |
| Maintainability| UI logic and DBMS algorithms should remain separated.           |
| Scope          | Normalization up to BCNF today; 4NF and 5NF (multivalued and join dependencies) are planned, see [Roadmap.md](Roadmap.md). |

## 6. Future Enhancements

- Save and load analysis sessions
- SQL schema export
- Minimal cover visualization
- Interactive decomposition graphs
- Report generation
- 4NF and 5NF support: multivalued and join dependencies, checks and decompositions (see [Roadmap.md](Roadmap.md) and the planned use cases below)

## 7. Planned Use Cases (4NF and 5NF)

These are not implemented yet. Details, phases and open questions are in [Roadmap.md](Roadmap.md).

### UC-10: Enter Multivalued and Join Dependencies (planned)

**Goal:** Record MVDs and JDs alongside FDs.

**Main Success Scenario:**
1. User enters an MVD such as `course ->> teacher`, or a JD such as `*(agent,company | company,product | agent,product)`.
2. System validates the syntax and that every attribute belongs to the relation (a JD's components must cover the relation).
3. System lists the dependency with the others, grouped by kind.

**Alternative Flows:**
- Unknown attribute, invalid syntax, or JD components that do not cover the relation.

### UC-11: Check and Decompose to 4NF (planned)

**Goal:** Detect 4NF violations and produce a lossless 4NF decomposition.

**Main Success Scenario:**
1. User selects '4NF'.
2. System finds a non-trivial MVD `X ->> Y` whose left side is not a superkey, and explains why it violates 4NF.
3. System splits the relation into `X ∪ Y` and `X ∪ (R − Y)`, repeating until every part is in 4NF.
4. System displays each step and the final relations with their dependencies.

**Alternative Flows:**
- Relation already in 4NF.
- Some dependencies cannot be preserved: reported by 'Check Result'.

### UC-12: Check and Decompose to 5NF (planned)

**Goal:** Detect 5NF violations with respect to the given dependencies and decompose along them.

**Main Success Scenario:**
1. User selects '5NF'.
2. System tests whether each given JD (and each MVD) is implied by the candidate keys.
3. System reports any JD that is not implied, and decomposes along it into its components.
4. System displays each step, and states that the result is relative to the given dependencies.
