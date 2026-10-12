# Roadmap: 4NF and 5NF

Status: **planned, not started** (2026-10-12).

The app currently supports normalization up to BCNF using functional dependencies (FDs) only. This plan extends it with multivalued dependencies (MVDs), join dependencies (JDs), Fourth Normal Form (4NF) and Fifth Normal Form (5NF, also called project-join normal form). Progress is tracked in [Progress.md](Progress.md), and defects found along the way go into [bugreport.md](../bugreport.md) as usual.

## Goals

- Users can enter MVDs and JDs alongside FDs.
- The app checks 4NF and 5NF and explains each violation step by step, like the existing checks.
- The app decomposes a relation into 4NF and into 5NF without losing information, with a step trace.
- The existing guarantees still hold: results checked against brute-force tests, deterministic output, explicit size limits, and errors that name the problem.

## Background

These are the definitions the implementation will follow.

- **MVD** `X ->> Y` on R: whenever two tuples agree on X, swapping their Y values gives two tuples that are also in R.
  - Every FD `X -> Y` is also an MVD `X ->> Y`.
  - Complementation: `X ->> Y` implies `X ->> R − X − Y`.
  - An MVD is trivial if `Y ⊆ X` or `X ∪ Y = R`.
- **4NF**: for every non-trivial MVD `X ->> Y` implied by the dependencies, X is a superkey. 4NF implies BCNF.
- **JD** `*(R1, …, Rn)`: R always equals the join of its projections onto R1, …, Rn.
  - The MVD `X ->> Y` is exactly the JD `*(X ∪ Y, X ∪ (R − Y))`.
  - A JD is trivial if one of its components is R itself.
- **5NF**: every non-trivial JD implied by the dependencies is implied by the candidate keys alone. 5NF implies 4NF.

## Design

### 1. Model and input

- New immutable model classes `MultivaluedDependency(lhs, rhs)` and `JoinDependency(components)`, following `FunctionalDependency`: read-only views, canonical ordering in `FdOrder`, and deterministic `toString()`.
- `Relation` carries MVDs and JDs next to its FDs and stays immutable (`withMultivaluedDependency(...)` and so on).
- Input syntax, using the existing name grammar and attribute-list rules:
  - MVD: `X ->> Y`. The parser must test for `->>` before `->`.
  - JD: `*(A,B | B,C | A,C)`. The components must together cover every attribute of the relation.
- The dependency input field accepts all three kinds. The summary list shows them in groups (FDs, MVDs, JDs).

### 2. Implication engine (the core)

Everything else depends on deciding whether a set of dependencies D implies a given dependency.

- **Reference implementation: the chase.** It generalizes the chase already in `LosslessJoinChecker`:
  - FD rules equate symbols (as today).
  - MVD and JD rules add rows. No new symbols are created, so the chase always terminates.
  - D implies a JD if chasing the JD's tableau with D produces a row of all distinguished symbols.
  - D implies an MVD `X ->> Y` if it implies the JD `*(X ∪ Y, X ∪ (R − Y))`.
  - D implies an FD `X -> Y` if chasing two rows that agree on X equates their Y columns.
- **Optimization: the dependency basis** `DEP(X)` (Beeri's algorithm). This is a partition of `R − X` such that `X ->> Y` holds exactly when `Y − X` is a union of its blocks. It is much faster than the chase for 4NF searches. It is only used once it agrees with the chase on every test.
- **MVDs can imply new FDs.** By the coalescence rule, `X ->> Y` and `W -> Z` with `Z ⊆ Y` and `W ∩ Y = ∅` imply `X -> Z`. Closures and candidate keys must therefore be computed with the full dependency set once MVDs are present. See open question 2 for how this affects the existing BCNF/3NF features.

### 3. 4NF check

- Search attribute subsets X of the relation, smallest first, in canonical order. Compute `DEP(X)`. A violation is a non-trivial `X ->> Y` built from its blocks where X is not a superkey.
- This search is exponential, like the BCNF sub-relation check. It gets its own attribute limit (see Limits).
- Possible shortcut: checking only the left-hand sides of the given dependencies. It is not adopted unless a published result justifies it **and** the brute-force tests confirm it.

### 4. 4NF decomposition

- A violation `X ->> Y` on Ri splits it into `X ∪ Y` and `X ∪ (Ri − Y)`. The MVD itself guarantees the split is lossless. Repeat until every part is in 4NF, recording numbered steps as in BCNF.
- Dependencies on each part:
  - FDs: projected with `FdProjector`, using closures under the full dependency set.
  - MVDs: `X ->> (Y ∩ Ri)` for `X ⊆ Ri`. This rule is sound, but MVDs that hold only on part of a relation (embedded MVDs) cannot always be expressed this way. Such dependencies are reported as not preserved rather than dropped silently.
- Like BCNF, 4NF does not guarantee dependency preservation. "Check Result" is extended to report lost MVDs and JDs.

### 5. 5NF check

- Each given JD, and each MVD as a two-part JD, must be implied by the candidate keys. To test this, chase the JD's tableau using only the key FDs `K -> R`. This reuses the existing chase.
- Deciding this for **every** implied JD is expensive in general. So the app reports **"5NF with respect to the given dependencies"** and says so in the output, just as 1NF is reported as an assumption today.

### 6. 5NF decomposition

- Split along a JD that the keys don't imply, into that JD's components. The split is lossless by construction. Remove components contained in other components, then repeat on each component with its projected dependencies.
- Projecting JDs onto components has the same embedded-dependency limitation as MVDs, and is reported the same way.

### 7. UI and output

- `NormalFormResult.NormalForm` gains `FOURTH_NF` and `FIFTH_NF`. `satisfies(...)` extends to them naturally.
- New **4NF** and **5NF** buttons next to BCNF and 3NF. Their traces go to the Decomposition tab, and Check Result covers MVDs and JDs.
- Normal-form output always says which dependencies it is relative to.
- New built-in examples:
  - a course/teacher/book relation with an MVD (4NF);
  - an agent/company/product relation with a JD (5NF).

### 8. Limits and performance

- A chase with MVDs and JDs can add many rows, so 4NF/5NF features get their own attribute limit, starting at about 10–12. Large JDs are capped too.
- The limits are measured and pinned with performance tests, as was done for BUG-004.

### 9. Testing strategy

- **Two independent implementations**: the chase and the dependency basis must agree on MVD implication for seeded random dependency sets.
- **Brute-force instance oracle for tiny schemas** (≤ 4 attributes, domain of 2 values): enumerate relation instances, keep those that satisfy D, and confirm that an implied dependency holds in all of them.
- **Invariants on random relations**:
  - every 4NF and 5NF part passes the 4NF/5NF check;
  - every decomposition is lossless (chase under D);
  - attributes are covered;
  - output doesn't depend on input order.
- **Textbook examples** with exact expected answers.
- **Mutation checks** (break a step on purpose and confirm the tests fail), as was done for `PropertyTest`.

## Phases

Each phase ends with `./mvnw -B verify` passing, tests for the new behaviour, and `Progress.md` / `bugreport.md` updated.

1. **Model and input**: MVD/JD classes, parser, immutable `Relation` support, UI input and listing. No analysis yet.
2. **Implication engine**: generalized chase for FDs, MVDs and JDs, then the dependency basis, cross-checked against the chase.
3. **Mixed closures, keys, 4NF check**: FD closure and candidate keys under FDs + MVDs (coalescence), then the 4NF check with traces.
4. **4NF decomposition**: decomposition, dependency projection, and Check Result reporting lost MVDs/JDs.
5. **5NF**: check with respect to the given dependencies, then 5NF decomposition.
6. **Polish**: examples, limits and performance tests, README / `UC.md` (UC-10 to UC-12) / tutorial text.

## Open questions (owner decisions)

1. **JD input syntax**: is `*(A,B | B,C | A,C)` acceptable?
2. **Effect on existing features**: once MVDs are present they can imply new FDs. Should keys, normal forms, BCNF and 3NF use those implied FDs, or should the FD-only features keep ignoring MVDs, and say so?
3. **Limits**: what attribute limit is acceptable for 4NF/5NF features, once measured?
4. **5NF scope**: is "5NF with respect to the given dependencies" enough, or is a complete check over all implied JDs required (much more expensive)?
