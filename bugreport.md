# Bug Report — DBMS Normalization & FD Analyzer

This file is the running bug and issue log for the project, and it is meant to be kept up to date.
Whoever finds, fixes or re-triages an issue (human or AI agent) updates it **in the same commit** as the change.

| | |
|---|---|
| **Last updated** | 2026-10-12 |
| **Last full review at commit** | `6b6ad65` (Add CI Pipeline) |
| **Test suite at review** | 78 tests, all passing — none of them catch the bugs below |
| **Open issues** | 37 (4 critical, 2 high, 9 medium, 22 low) |

---

## How to maintain this file

- **Status values:** `Open` · `In progress` · `Fixed (<commit>)` · `Won't fix (<reason>)` · `Cannot reproduce`
- **When you fix an issue:** set its status to `Fixed (<short-sha>)`, name the regression test that now covers it, and update the summary table and the counts above. Don't delete fixed entries; they record what used to be broken.
- **When you find an issue:** give it the next free ID in its category, add a row to the summary table, and include a reproduction that someone else can run.
- **After a full review:** update *Last full review at commit* and add a line to the [Changelog](#changelog).
- Every entry should say how it was established: **Verified** (reproduced by running code) or **By inspection** (found by reading or tracing the code, not run).

### Severity scale

| Severity | Meaning |
|---|---|
| **Critical** | Silently produces a wrong answer that a user would trust (this is a teaching tool, so wrong answers are the worst failure) |
| **High** | Hangs, crashes, or makes a feature unusable for realistic input |
| **Medium** | Misleading output, inconsistent behaviour, or a defect likely to cause a future bug |
| **Low** | Cosmetic, hygiene, performance waste, or maintainability |

---

## Summary

| ID | Severity | Title | Status |
|---|---|---|---|
| [BUG-001](#bug-001) | Critical | Naive FD projection: BCNF decomposition can return relations that are **not** in BCNF | Fixed |
| [BUG-002](#bug-002) | Critical | Dependency-preservation check reports preserved FDs as **LOST** | Fixed |
| [BUG-003](#bug-003) | Critical | Candidate-key search overflows at ≥ 31 attributes and returns **no keys** | Fixed |
| [BUG-004](#bug-004) | High | Exponential key search, repeated ~7× on the UI thread: 13 s freeze at 16 attributes, out of memory at 23 | Fixed |
| [BUG-005](#bug-005) | High | `OutOfMemoryError` / `StackOverflowError` are not caught; the user gets no feedback | Open |
| [BUG-006](#bug-006) | Critical | 3NF synthesis emits duplicate relations | Fixed |
| [BUG-007](#bug-007) | Medium | 1NF is hard-coded `true` but presented as a real check | Open |
| [BUG-008](#bug-008) | Medium | Results depend on `HashSet` iteration order (BCNF split, minimal cover, chosen 3NF key) | Open |
| [BUG-009](#bug-009) | Medium | `synthesize()` and `synthesizeWithSteps()` are separate copies that have already drifted | Open |
| [BUG-010](#bug-010) | Low | BCNF step numbering can produce duplicate or skipped step numbers | Fixed |
| [BUG-011](#bug-011) | Medium | Relation schema parser accepts attribute names that can never be used in an FD | Open |
| [BUG-012](#bug-012) | Medium | FD attribute parsing is inconsistent between upper- and lower-case and gives vague errors | Open |
| [BUG-013](#bug-013) | Medium | Mutable model objects are stored in hash-based collections | Open |
| [BUG-014](#bug-014) | Low | `FunctionalDependency.hashCode()` is additive, so `X→Y` and `Y→X` always collide | Open |
| [BUG-015](#bug-015) | Low | Removing an FD in the UI matches it by `toString()` | Open |
| [BUG-016](#bug-016) | Low | A failed "Load Example" leaves a half-built relation and a stale UI | Open |
| [BUG-017](#bug-017) | Low | "Check Result" overwrites the Explanation tab | Open |
| [BUG-018](#bug-018) | Low | Closure and decompositions are computed twice per click | Open |
| [BLD-001](#bld-001) | Low | `build-exe.ps1` hard-codes the artifact name and version | Open |
| [BLD-002](#bld-002) | Low | `jlink --add-modules ALL-MODULE-PATH` bundles the whole JDK | Open |
| [BLD-003](#bld-003) | Low | CI runs the test suite twice; PR branches build twice | Open |
| [BLD-004](#bld-004) | Low | Shipped config has DEBUG logging and meaningless properties | Open |
| [BLD-005](#bld-005) | Low | `.gitignore` ignores every `*.ini`; no `.gitattributes` (mixed line endings) | Open |
| [CQ-001](#cq-001) | Medium | ~20 dead source/resource files (old controllers, FXML, components, repository) | Open |
| [CQ-002](#cq-002) | Low | Dependency injection is only half applied | Open |
| [CQ-003](#cq-003) | Low | Every algorithm is written twice (plain + "with steps") | Open |
| [CQ-004](#cq-004) | Low | `MainController` is a 904-line god class; the service layer is mostly pass-through | Open |
| [CQ-005](#cq-005) | Low | Defensive copies in getters run inside the hot closure loop | Open |
| [CQ-006](#cq-006) | Low | Stale "backward compatibility" APIs with no callers | Open |
| [TEST-001](#test-001) | Medium | Several tests have assertions that can never fail | Open |
| [TEST-002](#test-002) | Medium | No invariant/property-based tests for the decomposition algorithms | Open |
| [TEST-003](#test-003) | Low | Normal-form tests use hand-written candidate keys instead of computed ones | Open |
| [TEST-004](#test-004) | Low | No tests for large inputs, performance limits, or the UI | Open |
| [DOC-001](#doc-001) | Low | `Progress.md`, `UC.md`, `PROJECT_STRUCTURE.md` duplicated in root and `MarkDown/` | Open |
| [DOC-002](#doc-002) | Low | README overstates 1NF and omits implemented features | Open |
| [DOC-003](#doc-003) | Low | `UC.md` is out of date with the implementation | Open |
| [DOC-004](#doc-004) | Low | `PROJECT_STRUCTURE.md` describes dead code as the live architecture | Open |

---

## 1. Correctness bugs

<a id="bug-001"></a>
### BUG-001 — Naive FD projection: BCNF decomposition can return relations that are not in BCNF

- **Severity:** Critical · **Status:** Fixed · **Verified**
- **Regression test:** `BcnfDecomposerTest.testViolationImpliedThroughRemovedAttribute`, `testInvariantsOnRandomRelations` (300 seeded random relations checked against brute-force BCNF, projection, and lossless-join oracles), `testFinalRelationsCarryProjectedFds`
- **Resolution:** All closures are now taken under the original F. On the original relation the given FDs are checked, which is sufficient. A sub-relation Ri is checked with given FDs inside Ri first, then every X ⊆ Ri, smallest first (`X⁺ ∩ Ri ⊋ X` and `≠ Ri`). The reported violating FD is `X → (X⁺ ∩ Ri) − X`. Final relations carry a minimal cover of the true projection of F⁺, computed from minimal determinants only. FDs are processed in sorted order, so results are deterministic. Uses the bitmask `FdBitIndex`; a 20-attribute chain decomposes in well under a second. Not yet fixed: the 3NF synthesizer still attaches only the minimal-cover FDs that fit inside each relation (display only; BUG-009 tracks the duplicated 3NF code).
- **Location:** [BcnfDecomposer.java:198](src/main/java/com/dbms/analyzer/algorithm/BcnfDecomposer.java:198) (`projectFdsOnto`); the same logic is in [ThreeNfSynthesizer.java](src/main/java/com/dbms/analyzer/algorithm/ThreeNfSynthesizer.java) (result FD attachment)

**Description.** When a relation is split, `projectFdsOnto` keeps only the FDs whose LHS and RHS both lie entirely inside the sub-schema. The projection of F onto Ri is really `{X → (X⁺ ∩ Ri) : X ⊆ Ri}`, which is computed over **F⁺**, not F. FDs implied through an attribute that is no longer in the sub-schema get lost. Sub-relations then look as if they have fewer (or no) FDs than they really do, so they are wrongly declared to be in BCNF.

**Reproduction.**
```
R(A,B,C,D)
A -> B
B -> A
C -> B
```
Click **BCNF**. Observed output:
```
R_1(A,B)    FDs: A -> B, B -> A
R_2(A,C,D)  FDs: (none)
```
**Expected:** `R_2(A,C,D)` is *not* in BCNF. `C → A` holds on it (C → B → A), and `C⁺ ∩ {A,C,D} = {A,C}`, so C is not a superkey. A correct result is `{A,B}`, `{A,C}`, `{C,D}`.

**Fix.**
1. Decide whether Ri is in BCNF without materialising the projection: Ri violates BCNF iff some X ⊆ Ri has `X⁺ ∩ Ri` ⊋ X and `X⁺ ∩ Ri` ≠ Ri, with closures taken under the **original** F. Use that X as the split point. (This is exponential in |Ri|, which is acceptable for classroom-sized schemas once BUG-004's limits are in place.)
2. If you want projected FDs for display, compute them as above and then minimise them with `MinimalCoverComputer`.

**Regression test to add:** the example above, asserting that every output relation passes an independent BCNF check (see TEST-002).

---

<a id="bug-002"></a>
### BUG-002 — Dependency-preservation check reports preserved FDs as LOST

- **Severity:** Critical · **Status:** Fixed · **Verified**
- **Regression test:** `DependencyPreservationCheckerTest.testPreservedThroughImpliedDependencies`, `testPreservedWhenOriginalFdSpansRelations`
- **Resolution:** Replaced the projection with the standard `res ∪= (res ∩ Ri)⁺ ∩ Ri` fixed-point test, using closures under the original F. The step-by-step view now shows how `res` grows through each Ri. Removed the naive public `projectFds` method, which had no other callers. FDs and relations are processed in sorted order, so the trace is deterministic.
- **Location:** [DependencyPreservationChecker.java:126](src/main/java/com/dbms/analyzer/algorithm/DependencyPreservationChecker.java:126) (`projectFds`), used by `findLostDependencies`, `isPreserved` and `checkWithSteps`

**Description.** This uses the same naive projection as BUG-001, so it never sees FDs that hold on a sub-schema only through F⁺.

**Reproduction 1.**
```
F = { A -> B, B -> C, C -> A },  decomposition { R1(A,B), R2(B,C) }
```
Observed: `C -> A` reported **LOST**.
Expected: preserved. C → B holds on R2 (C→A→B), B → A holds on R1, so C → A follows.

**Reproduction 2.** Run the BUG-001 example, then **Check Result**. Observed: `C -> B` reported **LOST**. With the true projections (`C→A` on ACD and `A→B` on AB) it is preserved. The checker and the decomposer are wrong in different directions on the same input.

**Fix.** Use the standard polynomial test, which needs no projection at all:
```
for each X -> Y in F:
    res = X
    repeat until res stops changing:
        for each Ri:
            res = res ∪ ((res ∩ Ri)⁺ ∩ Ri)      // closure under F
    preserved iff Y ⊆ res
```
For the step-by-step view, show the `res` growth per Ri instead of "projected FDs".

---

<a id="bug-003"></a>
### BUG-003 — Candidate-key search overflows at ≥ 31 attributes and returns no keys

- **Severity:** Critical · **Status:** Fixed · **Verified**
- **Regression test:** `CandidateKeyFinderTest.testRelationAboveLimitIsRejected`, `testLargestAllowedRelationWithoutFds`, `RelationServiceTest.testRelationAboveAttributeLimitIsRejected`
- **Resolution:** Added `AnalysisLimits.MAX_ATTRIBUTES` (20), enforced in `RelationService.createRelation` (with a clear message) and in `CandidateKeyFinder`. Subset masks are now `long` values in the new `FdBitIndex` helper, which rejects universes over 63 attributes. The `1 << n` loop in `NormalFormChecker` is gone (see BUG-004).
- **Location:** [CandidateKeyFinder.java:68](src/main/java/com/dbms/analyzer/algorithm/CandidateKeyFinder.java:68)

**Description.** `for (int i = 0; i < (1 << n); i++)` uses 32-bit `int`. At n = 31, `1 << 31` is negative, so the loop body never runs. At n = 32, `1 << 32 == 1`, so only the empty set is tried. Either way **zero candidate keys** come back without any error. Everything downstream (prime attributes, 2NF/3NF checks, 3NF synthesis's key step) is then silently wrong; for example, every attribute is classified as non-prime.

**Reproduction.** 32 attributes, no FDs → `findAllCandidateKeys` returns `[]`. Expected: one key, the full attribute set.

**Fix.** Enforce an explicit attribute limit with a clear error message (see BUG-004). Inputs this large are infeasible anyway, so failing loudly beats looping over `long` masks. The [NormalFormChecker.java](src/main/java/com/dbms/analyzer/algorithm/NormalFormChecker.java) `properNonEmptySubsets` method has the same `1 <<` pattern and needs the same guard.

---

<a id="bug-006"></a>
### BUG-006 — 3NF synthesis emits duplicate relations

- **Severity:** Critical · **Status:** Fixed · **Verified**
- **Regression test:** `ThreeNfSynthesizerTest.testIdenticalSchemasAreMergedIntoOne`
- **Location:** [ThreeNfSynthesizer.java:92](src/main/java/com/dbms/analyzer/algorithm/ThreeNfSynthesizer.java:92) (step 4), and again in `synthesizeWithSteps`

**Description.** Step 4 only removes a schema that is a **strict** subset of another (`containsAll && !reverse containsAll`). Two identical schemas never subsume each other, so both survive.

**Reproduction.** `R(A,B)` with `A -> B`, `B -> A`. The minimal cover is `{A→B, B→A}` (different LHSs, so they are not merged). Observed: `[R_3NF_1(A,B), R_3NF_2(A,B)]`. Expected: one relation `(A,B)`.

**Fix.** Deduplicate schemas first (for example, collect them into a `LinkedHashSet<Set<String>>`), then remove strict subsets.

---

<a id="bug-007"></a>
### BUG-007 — 1NF is hard-coded `true` but presented as a real check

- **Severity:** Medium · **Status:** Open · **By inspection**
- **Location:** [NormalFormChecker.java:23](src/main/java/com/dbms/analyzer/algorithm/NormalFormChecker.java:23); README "1NF: All attributes are atomic"

**Description.** `is1NF` always returns `true`, and `NormalForm.NONE` is never produced. The UI prints `1NF: true` as if it had been checked. The tool has no way to model non-atomic attributes, so this is a modelling assumption, not a check.

**Fix.** Label it in the UI and README as "1NF: assumed (attributes are treated as atomic)", or drop it from the output. Remove the unused `NONE` enum constant, or document what it means.

---

<a id="bug-008"></a>
### BUG-008 — Results depend on `HashSet` iteration order

- **Severity:** Medium · **Status:** Open · **Verified** (minimal cover); **By inspection** (BCNF, 3NF)
- **Location:**
  - `findBcnfViolation` in [BcnfDecomposer.java:169](src/main/java/com/dbms/analyzer/algorithm/BcnfDecomposer.java:169)
  - `candidateKeys.iterator().next()` in [ThreeNfSynthesizer.java](src/main/java/com/dbms/analyzer/algorithm/ThreeNfSynthesizer.java)
  - [MinimalCoverComputer.java](src/main/java/com/dbms/analyzer/algorithm/MinimalCoverComputer.java)

**Description.** The algorithms make choices (which violating FD to split on first, which candidate key to add, which redundant FD to drop) by taking whatever comes first out of a `HashSet`. Several answers can be valid, but the one shown depends on string hash codes rather than on anything the user can see or control. Two students entering the same FDs in a different order, or with different attribute names, can get different decompositions with no explanation why.

**Reproduction.** Minimal cover of `{A→B, B→A, A→C, B→C}`: one insertion order gives `[A→B,C ; B→A]`, the reverse order gives `[B→A,C ; A→B]`.

**Fix.** Use a canonical order everywhere: sort FDs (by LHS size, then lexicographically) and keys before iterating. Use `LinkedHashSet`/`TreeSet` in the models. Say in the trace why a particular FD or key was picked.

---

<a id="bug-009"></a>
### BUG-009 — `synthesize()` and `synthesizeWithSteps()` are separate copies that have already drifted

- **Severity:** Medium · **Status:** Open · **By inspection**
- **Location:** [ThreeNfSynthesizer.java:133](src/main/java/com/dbms/analyzer/algorithm/ThreeNfSynthesizer.java:133); [MainController.java:488](src/main/java/com/dbms/analyzer/javafx/controllers/MainController.java:488)

**Description.** These are two hand-copied implementations of the same algorithm. The `WithSteps` copy does not attach FDs to the resulting relations, and the two could start disagreeing on other details at any time. The UI shows the trace from one run (`synthesizeWithSteps`) and then stores the result of a separate run (`synthesize`) for **Check Result**. The same split exists for BCNF ([MainController.java:469](src/main/java/com/dbms/analyzer/javafx/controllers/MainController.java:469)). Combined with BUG-008, nothing guarantees that the decomposition being checked is the one on screen.

**Fix.** Write each algorithm once, returning a result object that holds both the relations and the trace. See CQ-003.

---

<a id="bug-010"></a>
### BUG-010 — BCNF step numbering can produce duplicate or skipped step numbers

- **Severity:** Low · **Status:** Fixed · **By inspection** (traced, not reproduced)
- **Regression test:** `BcnfDecomposerTest.testStepNumbersAreSequential`
- **Resolution:** Fixed as part of the BUG-001 rewrite: each step is numbered `stepLog.size() + 1` when it is recorded, so numbers are sequential by construction. The regression test asserts 1..n; it also passed on the old code for its input, so it guards the invariant rather than reproducing the original collision.
- **Location:** [BcnfDecomposer.java:155](src/main/java/com/dbms/analyzer/algorithm/BcnfDecomposer.java:155)

**Description.** The left child is numbered `stepNumber + 1` and the right child `stepNumber + stepLog.size()`; neither is the next free number. Trace: R(step 1) → R_1(2) → R_1_1 in BCNF → R_1_2 gets 2+2 = **4** → back at the root, R_2 gets 1+3 = **4**. That is a duplicate "Step 4", and step 3 never appears.

**Fix.** Number steps by `stepLog.size() + 1` at the moment each step is recorded, and drop the `stepNumber` parameter.

---

<a id="bug-011"></a>
### BUG-011 — Relation schema parser accepts attribute names that can never be used in an FD

- **Severity:** Medium · **Status:** Open · **Verified**
- **Location:** [RelationService.java:35](src/main/java/com/dbms/analyzer/service/RelationService.java:35)

**Description.** Attribute names are only checked for being non-empty and free of whitespace. `R(A,(B,C->D)` is accepted and gives attributes `A`, `(B` and `C->D`. An FD can never reference `C->D`, because `->` is the FD separator. The relation name is not validated either. The schema grammar and the FD grammar in `FdUtil` disagree.

**Fix.** Use one identifier grammar (for example `[A-Za-z_][A-Za-z0-9_]*`) and enforce it both when creating relations and when parsing FDs. Reject anything else with a message that names the bad token.

---

<a id="bug-012"></a>
### BUG-012 — FD attribute parsing is inconsistent between upper- and lower-case and gives vague errors

- **Severity:** Medium · **Status:** Open · **Verified**
- **Location:** [FdUtil.java:12](src/main/java/com/dbms/analyzer/algorithm/FdUtil.java:12) (`attributeTokens`)

**Description.**
- `AB -> C` on `R(A,B,C)` is split into `{A,B}`, but `ab -> c` on `R(a,b,c)` is read as the single attribute `ab` and fails with the generic "FD references undefined attributes".
- Compact input containing digits, such as `A1B` on `R(A1,B)`, is split character by character into `A`, `1`, `B`.
- If a relation has `A`, `B` and `AB`, then `AB` always means the attribute `AB`, and the only way to write `{A,B}` is `A,B`. That is fine, but nothing tells the user.
- `ATTRIBUTE_NAME` and `ATTRIBUTE_TOKEN` are identical patterns.

**Fix.** Make compact splitting depend on the relation: only split a compact token when every resulting character is an attribute of the relation, and apply this regardless of case. When an attribute is unknown, name it in the error, e.g. `Unknown attribute 'ab' — did you mean a,b?`. Merge the duplicate patterns.

---

<a id="bug-013"></a>
### BUG-013 — Mutable model objects are stored in hash-based collections

- **Severity:** Medium · **Status:** Open · **By inspection**
- **Location:** [FunctionalDependency.java](src/main/java/com/dbms/analyzer/model/FunctionalDependency.java), [Relation.java](src/main/java/com/dbms/analyzer/model/Relation.java), [CandidateKey.java](src/main/java/com/dbms/analyzer/model/CandidateKey.java)

**Description.** `setLeftSide`, `setRightSide`, `setAttributes` and `setName` change fields that feed `equals`/`hashCode`, while instances live in `HashSet`s throughout the code. No caller uses the setters today, but the first one that does will corrupt a set without any error. Also, `Relation.equals` ignores FDs, so two relations with the same schema and different FDs count as equal inside the `LinkedHashSet` results.

**Fix.** Turn these into `record`s holding `Set.copyOf(...)` immutable sets and remove the setters. Decide on purpose whether FDs belong in `Relation` equality.

---

<a id="bug-014"></a>
### BUG-014 — `FunctionalDependency.hashCode()` is additive

- **Severity:** Low · **Status:** Open · **By inspection**
- **Location:** [FunctionalDependency.java:51](src/main/java/com/dbms/analyzer/model/FunctionalDependency.java:51)

`leftSide.hashCode() + rightSide.hashCode()` is symmetric, so `X→Y` and `Y→X` always collide. Results are still correct; only hashing performance suffers. **Fix:** `Objects.hash(leftSide, rightSide)`, or let a `record` generate it.

---

<a id="bug-015"></a>
### BUG-015 — Removing an FD in the UI matches it by `toString()`

- **Severity:** Low · **Status:** Open · **By inspection**
- **Location:** [MainController.java:289](src/main/java/com/dbms/analyzer/javafx/controllers/MainController.java:289)

The list holds `String`s, and removal searches for the FD whose `toString()` equals the selected text. This is fragile: it breaks as soon as the display format changes or two FDs render the same. **Fix:** use `ListView<FunctionalDependency>` with a cell factory for display.

---

<a id="bug-016"></a>
### BUG-016 — A failed "Load Example" leaves a half-built relation and a stale UI

- **Severity:** Low · **Status:** Open · **By inspection**
- **Location:** [MainController.java:317](src/main/java/com/dbms/analyzer/javafx/controllers/MainController.java:317)

The relation is created first and the FDs are added one at a time. If an FD fails, the `catch` neither rolls back nor calls `updateRelationDisplay()`, so the service holds the new partial relation while the UI still shows the old one. The built-in samples are valid today, so this is latent. **Fix:** build the relation and its FDs off to the side and commit them only if every step succeeds, or always refresh the UI in `finally`.

---

<a id="bug-017"></a>
### BUG-017 — "Check Result" overwrites the Explanation tab

- **Severity:** Low · **Status:** Open · **By inspection**
- **Location:** [MainController.java:527](src/main/java/com/dbms/analyzer/javafx/controllers/MainController.java:527)

Output from the decomposition check goes into `explanationResultArea`, replacing whatever **Explain** last produced. **Fix:** give it its own tab, or append it to the Decomposition tab.

---

<a id="bug-018"></a>
### BUG-018 — Closure and decompositions are computed twice per click

- **Severity:** Low · **Status:** Open · **By inspection**
- **Location:** [MainController.java:739](src/main/java/com/dbms/analyzer/javafx/controllers/MainController.java:739), [MainController.java:469](src/main/java/com/dbms/analyzer/javafx/controllers/MainController.java:469), [MainController.java:488](src/main/java/com/dbms/analyzer/javafx/controllers/MainController.java:488)

`computeClosure` and `computeClosureWithSteps` both run for one click, and so do the plain and traced versions of BCNF and 3NF. This wastes work and is the cause of BUG-009's mismatch risk. The fix is the same as for CQ-003.

---

## 2. Robustness and performance

<a id="bug-004"></a>
### BUG-004 — Exponential key search, repeated ~7× on the UI thread

- **Severity:** High · **Status:** Fixed · **Verified**
- **Regression test:** `CandidateKeyFinderTest.testWorstCaseAtLimitFinishesQuickly`, `testMatchesBruteForceOnRandomRelations`, `NormalFormServiceTest.testNormalFormClickAtAttributeLimitIsFast`
- **Resolution:** Rewrote `CandidateKeyFinder`: attributes are classified (core / never-in-key / optional), only optional subsets are searched with bitmask closures, smallest first, and supersets of found keys are skipped. `CandidateKeyService` caches keys until the attributes or FDs change. The 2NF check now tests only the maximal proper subsets K − {a} (closure is monotone, so this is equivalent) instead of all 2^|K| subsets, and reports a minimal determinant. Measured: one Normal Form click at 20 attributes takes ~32 ms, versus 13.4 s at 16 attributes before; the worst case at the limit (2^10 keys) takes under 1 s. The work still runs on the JavaFX thread, but the attribute limit now bounds it; moving it into a background `Task` remains a possible follow-up.
- **Location:** [CandidateKeyFinder.java](src/main/java/com/dbms/analyzer/algorithm/CandidateKeyFinder.java); callers in [MainController.java:405](src/main/java/com/dbms/analyzer/javafx/controllers/MainController.java:405) and [MainController.java:631](src/main/java/com/dbms/analyzer/javafx/controllers/MainController.java:631)

**Description.** Key finding builds all 2ⁿ subsets as `HashSet`s in memory, takes the closure of each (copying FD sets on every getter call), and then checks minimality pairwise, which is O(S²) over the superkeys found. The cost is multiplied by the UI:
- `refreshSummary()` runs on **every** FD add or remove and computes keys twice: once directly and once inside `analyzeNormalForm`.
- `handleAnalyzeNormalForm` calls analyze, is2NF, is3NF, isBCNF and then refresh, which is about **7 full key computations**.
- `CandidateKeyService.getPrimeAttributes()` and `getNonPrimeAttributes()` each recompute the keys again.
- Everything runs on the JavaFX Application Thread.

**Measurements** (this machine, JDK 25):

| Attributes | Operation | Result |
|---|---|---|
| 16 | One **Normal Form** click (service calls replayed) | **13.4 s** UI freeze |
| 18 | One `findAllCandidateKeys` call | 5.4 s |
| 23 | One `findAllCandidateKeys` call, `-Xmx2g` | **`OutOfMemoryError`** |
| ≥ 31 | — | wrong answer (BUG-003) |

`UC.md` has a non-functional requirement that calculations "complete efficiently for moderate schemas"; 16 attributes is a moderate schema.

**Fix.**
1. Prune the search. Attributes that appear in no RHS must be in every key; attributes that appear in an RHS but in no LHS are in no key. Search only the remaining "middle" attributes, smallest subsets first, and skip supersets of keys already found. This makes typical textbook inputs nearly instant.
2. Use bitmasks (`long`/`BitSet`) instead of `HashSet<String>` for subsets.
3. Compute keys **once** per relation/FD change and cache them in a session-state object that every service reads.
4. Run analysis in a `javafx.concurrent.Task` with a progress indicator.
5. Enforce a documented maximum attribute count with a clear message.

---

<a id="bug-005"></a>
### BUG-005 — `OutOfMemoryError` / `StackOverflowError` are not caught; the user gets no feedback

- **Severity:** High · **Status:** Open · **By inspection** (OOM itself verified in BUG-004)
- **Location:** every `catch (RuntimeException e)` in [MainController.java](src/main/java/com/dbms/analyzer/javafx/controllers/MainController.java); app start-up in [MainApp.java](src/main/java/com/dbms/analyzer/javafx/MainApp.java)

The handlers catch only `RuntimeException`. An `OutOfMemoryError` from BUG-004 escapes to the JavaFX thread's default handler: the stack trace goes to a console that the packaged `.exe` doesn't show, the UI is left part-updated, and the user sees nothing. If the Spring context fails in `MainApp.init()`, the app also dies without a dialog. This breaks the `UC.md` requirement "Invalid input should never crash the application". **Fix:** prevent the condition in the first place (BUG-004's limits). Also install `Thread.setDefaultUncaughtExceptionHandler` and show an error dialog for both start-up and runtime failures.

---

## 3. Build, packaging and CI

<a id="bld-001"></a>
### BLD-001 — `build-exe.ps1` hard-codes the artifact name and version
- **Severity:** Low · **Status:** Open · **By inspection** · [build-exe.ps1](build-exe.ps1)

`$artifactName = "dbms-normalizer-1.0.0-SNAPSHOT"` and `--app-version 1.0.0` duplicate `pom.xml`. The first version bump will make the script fail with "Expected thin application jar was not found". **Fix:** read the version via `mvn help:evaluate -Dexpression=project.version -q -DforceStdout`, or pin `<finalName>` in the POM.

<a id="bld-002"></a>
### BLD-002 — `jlink --add-modules ALL-MODULE-PATH` bundles the whole JDK
- **Severity:** Low · **Status:** Open · **By inspection** · [build-exe.ps1](build-exe.ps1)

No `--module-path` is passed, so `ALL-MODULE-PATH` resolves to every JDK module. The runtime image ends up much larger than needed. JavaFX is loaded from the classpath (unnamed module), which JavaFX officially does not support and warns about at start-up. **Fix:** run `jdeps --print-module-deps` on the staged jars and pass that module list. Optionally put the JavaFX jars on `--module-path`.

<a id="bld-003"></a>
### BLD-003 — CI runs the test suite twice; PR branches build twice
- **Severity:** Low · **Status:** Open · **By inspection** · [.github/workflows/ci.yml](.github/workflows/ci.yml)

`mvn test` followed by `mvn clean package` runs every test twice. The triggers `on: push` + `on: pull_request` both fire for a PR's branch. `-B` (batch mode) is missing, so logs are noisy. **Fix:** a single `mvn -B verify` step, with `push` limited to `branches: [main]`. Optionally upload the jar as an artifact.

<a id="bld-004"></a>
### BLD-004 — Shipped config has DEBUG logging and meaningless properties
- **Severity:** Low · **Status:** Open · **By inspection** · [application.properties](src/main/resources/application.properties)

`logging.level.com.dbms.analyzer=DEBUG` ships in the release build. `spring.application.version`, `javafx.preloader` and `javafx.launch.noPreloaderStage` are not real Spring or JavaFX properties and do nothing. `app.title`/`app.version` are never read; the title is hard-coded again in `MainApp`. **Fix:** set INFO by default, delete the dead keys, and either read `app.title` or delete it.

<a id="bld-005"></a>
### BLD-005 — `.gitignore` ignores every `*.ini`; no `.gitattributes`
- **Severity:** Low · **Status:** Open · **By inspection** · [.gitignore](.gitignore)

A blanket `*.ini` rule will silently hide any future config file. There is no `.gitattributes`: the root and `MarkDown/` copies of the docs have identical content and differ only in line endings (see DOC-001). **Fix:** narrow the ignore rule (e.g. `Desktop.ini`, which is already listed) and add `* text=auto`.

---

## 4. Code quality and maintainability

<a id="cq-001"></a>
### CQ-001 — ~20 dead source/resource files
- **Severity:** Medium · **Status:** Open · **Verified** (grep: no references outside the files themselves)

`main.fxml` is the only FXML file loaded, and nothing references the following:

| Dead item | Notes |
|---|---|
| `javafx/controllers/{Bcnf,CandidateKey,Closure,Explanation,Fd,NormalForm,Relation}Controller.java` | Use field `@Autowired`, unlike the rest of the code |
| `resources/fxml/{bcnf,candidateKeys,closure,explanation,fd,normalForm,relation}.fxml` | Never loaded |
| `javafx/components/{AttributeChip,DecompositionTreeView,FdListView}.java` | `DecompositionTreeView` is what UC-6 promises, but it isn't wired in |
| `javafx/utils/AlertHelper.java` | Unused |
| `javafx/utils/JavaFxSpringInjector.java` | Written in `MainApp`, never read (a static service locator) |
| `repository/InMemorySessionRepository.java` | Unused `Map<String,Object>` |
| `model/Attribute.java` | Unused |

Medium because dead code misleads future maintainers, AI agents especially, into "fixing" or extending code that never runs. **Fix:** delete it, or move `DecompositionTreeView` into the real UI if you want the tree.

<a id="cq-002"></a>
### CQ-002 — Dependency injection is only half applied
- **Severity:** Low · **Status:** Open · **By inspection** · [AppConfig.java](src/main/java/com/dbms/analyzer/config/AppConfig.java)

`AppConfig` registers a `ClosureComputer` bean, but `BcnfDecomposer`, `CandidateKeyFinder`, `NormalFormChecker`, `ThreeNfSynthesizer` and others each create their own with `new ClosureComputer()` / `new MinimalCoverComputer()`, so the bean graph is a fiction. More broadly, Spring Boot buys little in a single-window desktop app with ~10 stateless helpers, while costing start-up time and package size. **Fix:** either inject through constructors everywhere, or drop Spring and wire the objects in `MainApp`.

<a id="cq-003"></a>
### CQ-003 — Every algorithm is written twice
- **Severity:** Low · **Status:** Open · **By inspection**

`computeClosure`/`computeClosureWithSteps`, `chaseAlgorithm`/`chaseAlgorithmWithSteps`, `binaryCheck`/`binaryCheckWithSteps`, `synthesize`/`synthesizeWithSteps`, plus the minimal cover and dependency preservation pairs. They have already diverged (BUG-009). **Fix:** one implementation per algorithm that returns a result together with a list of structured trace events; the UI renders the trace.

<a id="cq-004"></a>
### CQ-004 — `MainController` is a 904-line god class; the service layer is mostly pass-through
- **Severity:** Low · **Status:** Open · **By inspection**

The controller takes 10 injected services, handles formatting, holds state (`lastDecomposition`) and contains the tutorial text. Most `*Service` methods are just `if (!hasRelation()) throw …; return algorithm.x(...)`. **Fix:** add one `AnalysisSession` holding the relation, FDs and cached keys/normal form/decomposition, invalidated on change. Formatting goes into presenter classes, and the tutorial text into a resource file.

<a id="cq-005"></a>
### CQ-005 — Defensive copies in getters run inside the hot closure loop
- **Severity:** Low · **Status:** Open · **By inspection**

`getLeftSide()` and `getRightSide()` allocate a new `HashSet` on every call, and `ClosureComputer` calls them O(|F|) times per fixed-point pass, for each of 2ⁿ subsets. This is a large constant factor in BUG-004. Making the models immutable (BUG-013) removes the need to copy.

<a id="cq-006"></a>
### CQ-006 — Stale "backward compatibility" APIs with no callers
- **Severity:** Low · **Status:** Open · **Verified**

- `BcnfDecomposer.createDecompositionStep` ("kept for backward compatibility with existing callers"): there are no callers.
- `BcnfDecompositionService.getDecompositionSteps()`: returns the *final result* wrapped in a list and calls it "steps".
- `BcnfDecompositionService.isAlreadyBcnf()`: runs a full decomposition just to compare its size to 1, and is only used by a dead controller.
- `ClosureService.isSuperKey`, `FdUtil.hasDuplicates`: unused in production code.

Delete them.

---

## 5. Test-suite gaps

<a id="test-001"></a>
### TEST-001 — Several tests have assertions that can never fail
- **Severity:** Medium · **Status:** Open · **Verified**

| Test | Problem |
|---|---|
| `BcnfDecomposerTest.testBcnfViolation` | `assertTrue(decomposed.size() >= 1)`: always true |
| `CandidateKeyFinderTest.testSimpleCandidateKey` | Only checks that the result is non-empty; never checks the key is `{A}` |
| `CandidateKeyFinderTest.testNoCandidateKeys` | The comment says the key should be the full set, but only non-emptiness is asserted |
| `ThreeNfSynthesizerTest.testClassicDecomposition` | `size() >= 2` and attribute coverage only; doesn't check 3NF, losslessness or preservation |
| `*WithSteps` tests | Assert only that the trace is non-empty |

<a id="test-002"></a>
### TEST-002 — No invariant/property-based tests for the decomposition algorithms
- **Severity:** Medium · **Status:** Open

None of the bugs BUG-001, -002, -003 or -006 would survive tests like these. Add jqwik (or a seeded random loop in JUnit) over random schemas with 2–7 attributes and 0–6 FDs, and assert:
1. Every BCNF output relation is in BCNF under the **true** projection (check with a brute-force F⁺ oracle in test code).
2. BCNF and 3NF decompositions are lossless (chase).
3. 3NF synthesis preserves dependencies (brute-force oracle), and each output relation is in 3NF.
4. Every reported candidate key is a superkey, no proper subset of it is, and no other minimal superkey is missing (brute force).
5. The minimal cover is equivalent to F, every RHS is a single attribute, nothing is redundant, and no LHS attribute is extraneous.
6. The plain and `WithSteps` variants return identical results.
7. Output does not depend on FD insertion order (BUG-008).

Also add pinned textbook examples (Elmasri/Navathe, Silberschatz) with exact expected outputs.

<a id="test-003"></a>
### TEST-003 — Normal-form tests use hand-written candidate keys
- **Severity:** Low · **Status:** Open

`NormalFormCheckerTest` passes `candidateKeys` built by hand, so it never exercises the real key finder together with the checker. Add at least a few end-to-end tests through `NormalFormService`.

<a id="test-004"></a>
### TEST-004 — No tests for large inputs, performance limits, or the UI
- **Severity:** Low · **Status:** Open

Nothing pins the behaviour at the attribute limit (BUG-003/004). There are no TestFX tests, so the controller's 904 lines are covered only by a context-load test.

---

## 6. Documentation

<a id="doc-001"></a>
### DOC-001 — Docs duplicated in root and `MarkDown/`
- **Severity:** Low · **Status:** Open · **Verified**

Commit `41f9fc5` "Repo reordered" *copied* `Progress.md`, `UC.md` and `PROJECT_STRUCTURE.md` into `MarkDown/` and left the originals in place. The contents are identical apart from line endings, and they will drift. **Fix:** keep a single copy (`git mv`).

<a id="doc-002"></a>
### DOC-002 — README overstates 1NF and omits implemented features
- **Severity:** Low · **Status:** Open · **By inspection** · [README.md](README.md)

The README claims a 1NF atomicity check (BUG-007). It does not mention minimal cover, 3NF synthesis or the lossless/dependency-preservation check (**Check Result**), and it gives no attribute limit. It uses `git clone <repository-url>` as a placeholder.

<a id="doc-003"></a>
### DOC-003 — `UC.md` is out of date with the implementation
- **Severity:** Low · **Status:** Open · **By inspection** · [UC.md](UC.md)

- "Future Enhancements" lists the dependency preservation checker and minimal cover, both of which are already implemented.
- UC-6 promises a "decomposition tree" and a "dependency preservation warning"; neither exists.
- UC-7's "explanation mode" toggle doesn't exist.
- The non-functional requirements on performance and "never crash" are violated by BUG-004/005.

<a id="doc-004"></a>
### DOC-004 — `PROJECT_STRUCTURE.md` describes dead code as the live architecture
- **Severity:** Low · **Status:** Open · **By inspection** · [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)

It describes "controllers/ (7 files)", "components/ (3 files)" and "repository/ — Data Persistence" as active parts of the design (CQ-001). Update it after the dead code is removed.

---

## 7. Reviewed and found correct

These were checked during the review, so later reviews can skip them unless the code changes:

- `ClosureComputer`: fixed-point closure is correct and terminates.
- `NormalFormChecker`: on the **original** relation, the 2NF (partial dependency via closures of key subsets), 3NF (X superkey or A prime, per RHS attribute) and BCNF checks are correct. Checking only the given F is sufficient for the full relation. The problems come only from projected sub-relations (BUG-001).
- `MinimalCoverComputer`: splitting the RHS, removing extraneous LHS attributes (tested under the full current F), and removing redundant FDs (including duplicates and trivial FDs) are all correct. Only the order of choices is an issue (BUG-008).
- `LosslessJoinChecker`: the binary shortcut and the chase are correct. Rows are equated pairwise rather than by global symbol replacement, but repeated passes reach the same fixed point. The `"b"+i+j` symbol names cannot collide within a column.
- `BcnfDecomposer`: the recursion terminates (both children are strictly smaller), and the split `X⁺` / `X ∪ (R − X⁺)` is lossless.
- 3NF synthesis steps 1–3: the minimal cover, one schema per LHS group, and adding a key relation when none contains a key are correct, apart from BUG-006.

---

## Changelog

| Date | Commit | Change |
|---|---|---|
| 2026-10-12 | `6b6ad65` | Initial full review. 37 issues logged. Reproductions verified with throwaway probe tests (not committed). |
