package com.dbms.analyzer.algorithm;

import com.dbms.analyzer.model.CandidateKey;
import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.model.Relation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Synthesizes a lossless-join, dependency-preserving 3NF decomposition
 * using the classical algorithm:
 *
 * 1. Compute the minimal cover of the FD set.
 * 2. For each FD X → A in the minimal cover, create a relation {X ∪ A}.
 * 3. If no created relation contains a candidate key of the original relation,
 *    add a relation consisting of a candidate key.
 * 4. Remove any relation whose attribute set is a subset of another relation's
 *    (or a duplicate of an earlier one).
 *
 * Each resulting relation carries a minimal cover of the projection of F⁺.
 */
public class ThreeNfSynthesizer {

    private final MinimalCoverComputer minimalCoverComputer;
    private final CandidateKeyFinder candidateKeyFinder;
    private final FdProjector fdProjector = new FdProjector();

    /** The 3NF relations together with the readable trace that produced them. */
    public record Synthesis(Set<Relation> relations, List<String> steps) {
    }

    public ThreeNfSynthesizer() {
        this(new MinimalCoverComputer(), new CandidateKeyFinder());
    }

    public ThreeNfSynthesizer(MinimalCoverComputer minimalCoverComputer,
                              CandidateKeyFinder candidateKeyFinder) {
        this.minimalCoverComputer = minimalCoverComputer;
        this.candidateKeyFinder = candidateKeyFinder;
    }

    /**
     * Decomposes a relation into 3NF using synthesis.
     *
     * @param relation the original relation
     * @return a set of relations in 3NF that together form a lossless-join,
     *         dependency-preserving decomposition
     */
    public Set<Relation> synthesize(Relation relation) {
        return synthesizeWithTrace(relation).relations();
    }

    /**
     * Synthesizes 3NF with a step-by-step trace.
     */
    public List<String> synthesizeWithSteps(Relation relation) {
        return synthesizeWithTrace(relation).steps();
    }

    /**
     * Synthesizes once and returns both the result and its trace, so callers
     * never display one run and use another.
     */
    public Synthesis synthesizeWithTrace(Relation relation) {
        List<String> steps = new ArrayList<>();
        Set<String> attributes = relation.getAttributes();
        Set<FunctionalDependency> fds = relation.getFunctionalDependencies();

        steps.add("=== 3NF Synthesis ===");
        steps.add("");

        // Step 1: minimal cover
        Set<FunctionalDependency> minCover =
                minimalCoverComputer.computeMinimalCover(fds);
        steps.add("Step 1: Compute minimal cover");
        minCover.forEach(fd -> steps.add("  " + fd));
        steps.add("");

        // Step 2: create a relation for each FD in the minimal cover
        steps.add("Step 2: Create a relation for each FD in the minimal cover");
        List<Set<String>> schemas = new ArrayList<>();
        for (FunctionalDependency fd : minCover) {
            Set<String> schema = new TreeSet<>();
            schema.addAll(fd.getLeftSide());
            schema.addAll(fd.getRightSide());
            schemas.add(schema);
            steps.add("  " + fd + "  →  R(" + String.join(",", schema) + ")");
        }
        steps.add("");

        // Step 3: ensure at least one relation contains a candidate key
        Set<CandidateKey> candidateKeys =
                candidateKeyFinder.findAllCandidateKeys(attributes, fds);
        boolean keyPresent = schemas.stream().anyMatch(schema ->
                candidateKeys.stream().anyMatch(
                        key -> schema.containsAll(key.getAttributes())));

        steps.add("Step 3: Check if any relation contains a candidate key");
        if (keyPresent) {
            steps.add("  A candidate key is already present. No extra relation needed.");
        } else if (!candidateKeys.isEmpty()) {
            // Add a relation for the smallest key (alphabetically first on ties)
            CandidateKey chosen = FdOrder.sortedKeys(candidateKeys).get(0);
            schemas.add(new TreeSet<>(chosen.getAttributes()));
            steps.add("  No relation contains a candidate key. Adding relation for key "
                    + chosen + " (smallest key, alphabetically first on ties).");
        }
        steps.add("");

        // Step 4: remove subsumed schemas (a strict subset of another, or a
        // duplicate of an earlier one)
        steps.add("Step 4: Remove subsumed (redundant) relations");
        List<Integer> toRemove = new ArrayList<>();
        for (int i = 0; i < schemas.size(); i++) {
            for (int j = 0; j < schemas.size(); j++) {
                if (i != j
                        && schemas.get(j).containsAll(schemas.get(i))
                        && (j < i
                            || !schemas.get(i).containsAll(schemas.get(j)))) {
                    toRemove.add(i);
                    steps.add("  Removing R(" + String.join(",", schemas.get(i))
                            + ") — subsumed by R("
                            + String.join(",", schemas.get(j)) + ")");
                    break;
                }
            }
        }
        if (toRemove.isEmpty()) {
            steps.add("  No subsumed relations found.");
        }
        steps.add("");

        // Build result relations, each with the projection of F⁺
        Set<Relation> result = new LinkedHashSet<>();
        int counter = 1;
        steps.add("Final 3NF decomposition:");
        for (int i = 0; i < schemas.size(); i++) {
            if (toRemove.contains(i)) continue;

            Set<String> schema = schemas.get(i);
            Relation r = new Relation(relation.getName() + "_3NF_" + counter,
                    schema, fdProjector.project(schema, fds));
            result.add(r);
            steps.add("  " + r.getName() + "(" + String.join(",", schema) + ")");
            for (FunctionalDependency fd : FdOrder.sorted(r.getFunctionalDependencies())) {
                steps.add("    FD: " + fd);
            }
            counter++;
        }

        return new Synthesis(result, steps);
    }
}
