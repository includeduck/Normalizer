package com.dbms.analyzer.algorithm;

import com.dbms.analyzer.model.CandidateKey;
import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.model.Relation;

import java.util.ArrayList;
import java.util.HashSet;
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
 * 4. Remove any relation whose attribute set is a subset of another relation's.
 */
public class ThreeNfSynthesizer {

    private final MinimalCoverComputer minimalCoverComputer;
    private final CandidateKeyFinder candidateKeyFinder;

    public ThreeNfSynthesizer() {
        this.minimalCoverComputer = new MinimalCoverComputer();
        this.candidateKeyFinder = new CandidateKeyFinder();
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
        Set<String> attributes = relation.getAttributes();
        Set<FunctionalDependency> fds = relation.getFunctionalDependencies();

        // Step 1: minimal cover
        Set<FunctionalDependency> minCover =
                minimalCoverComputer.computeMinimalCover(fds);

        // Step 2: create a relation for each FD in the minimal cover
        List<Set<String>> schemas = new ArrayList<>();
        List<Set<FunctionalDependency>> schemaFds = new ArrayList<>();

        for (FunctionalDependency fd : minCover) {
            Set<String> schema = new TreeSet<>();
            schema.addAll(fd.getLeftSide());
            schema.addAll(fd.getRightSide());
            schemas.add(schema);
            // Attach the originating FD
            Set<FunctionalDependency> fdSet = new LinkedHashSet<>();
            fdSet.add(fd);
            schemaFds.add(fdSet);
        }

        // Step 3: ensure at least one relation contains a candidate key
        Set<CandidateKey> candidateKeys =
                candidateKeyFinder.findAllCandidateKeys(attributes, fds);

        boolean keyPresent = false;
        for (Set<String> schema : schemas) {
            for (CandidateKey ck : candidateKeys) {
                if (schema.containsAll(ck.getAttributes())) {
                    keyPresent = true;
                    break;
                }
            }
            if (keyPresent) break;
        }

        if (!keyPresent && !candidateKeys.isEmpty()) {
            // Pick one candidate key and add a relation for it
            CandidateKey chosen = candidateKeys.iterator().next();
            schemas.add(new TreeSet<>(chosen.getAttributes()));
            schemaFds.add(new LinkedHashSet<>());
        }

        // Step 4: remove subsumed schemas
        List<Integer> toRemove = new ArrayList<>();
        for (int i = 0; i < schemas.size(); i++) {
            for (int j = 0; j < schemas.size(); j++) {
                if (i != j
                        && schemas.get(j).containsAll(schemas.get(i))
                        && !schemas.get(i).containsAll(schemas.get(j))) {
                    toRemove.add(i);
                    break;
                }
            }
        }

        // Build result relations
        Set<Relation> result = new LinkedHashSet<>();
        int counter = 1;
        for (int i = 0; i < schemas.size(); i++) {
            if (toRemove.contains(i)) continue;

            Set<String> schema = schemas.get(i);
            Relation r = new Relation(
                    relation.getName() + "_3NF_" + counter, schema);

            // Project FDs onto this sub-schema
            for (FunctionalDependency fd : minCover) {
                if (schema.containsAll(fd.getLeftSide())
                        && schema.containsAll(fd.getRightSide())) {
                    r.addFunctionalDependency(fd);
                }
            }

            result.add(r);
            counter++;
        }

        return result;
    }

    /**
     * Synthesizes 3NF with a step-by-step trace.
     */
    public List<String> synthesizeWithSteps(Relation relation) {
        List<String> steps = new ArrayList<>();
        Set<String> attributes = relation.getAttributes();
        Set<FunctionalDependency> fds = relation.getFunctionalDependencies();

        steps.add("=== 3NF Synthesis ===");
        steps.add("");

        // Step 1
        Set<FunctionalDependency> minCover =
                minimalCoverComputer.computeMinimalCover(fds);
        steps.add("Step 1: Compute minimal cover");
        minCover.forEach(fd -> steps.add("  " + fd));
        steps.add("");

        // Step 2
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

        // Step 3
        Set<CandidateKey> candidateKeys =
                candidateKeyFinder.findAllCandidateKeys(attributes, fds);

        boolean keyPresent = false;
        for (Set<String> schema : schemas) {
            for (CandidateKey ck : candidateKeys) {
                if (schema.containsAll(ck.getAttributes())) {
                    keyPresent = true;
                    break;
                }
            }
            if (keyPresent) break;
        }

        steps.add("Step 3: Check if any relation contains a candidate key");
        if (keyPresent) {
            steps.add("  A candidate key is already present. No extra relation needed.");
        } else if (!candidateKeys.isEmpty()) {
            CandidateKey chosen = candidateKeys.iterator().next();
            schemas.add(new TreeSet<>(chosen.getAttributes()));
            steps.add("  No candidate key found. Adding relation for key: " + chosen);
        }
        steps.add("");

        // Step 4
        steps.add("Step 4: Remove subsumed (redundant) relations");
        Set<Relation> finalResult = new LinkedHashSet<>();
        List<Integer> toRemove = new ArrayList<>();
        for (int i = 0; i < schemas.size(); i++) {
            for (int j = 0; j < schemas.size(); j++) {
                if (i != j
                        && schemas.get(j).containsAll(schemas.get(i))
                        && !schemas.get(i).containsAll(schemas.get(j))) {
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

        int counter = 1;
        steps.add("Final 3NF decomposition:");
        for (int i = 0; i < schemas.size(); i++) {
            if (toRemove.contains(i)) continue;
            String name = relation.getName() + "_3NF_" + counter;
            steps.add("  " + name + "(" + String.join(",", schemas.get(i)) + ")");
            counter++;
        }

        return steps;
    }
}
