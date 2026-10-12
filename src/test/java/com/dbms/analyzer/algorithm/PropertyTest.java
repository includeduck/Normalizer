package com.dbms.analyzer.algorithm;

import com.dbms.analyzer.model.CandidateKey;
import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.model.NormalFormResult;
import com.dbms.analyzer.model.Relation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Invariants checked on seeded random relations against brute-force
 * oracles that share no code with the implementations under test
 * (only the simple fixed-point {@link ClosureComputer}).
 */
public class PropertyTest {

    private static final int TRIALS = 300;
    private final ClosureComputer closureComputer = new ClosureComputer();

    @Test
    public void testThreeNfSynthesisIsLosslessPreservingAndIn3nf() {
        ThreeNfSynthesizer synthesizer = new ThreeNfSynthesizer();
        Random random = new Random(3);
        for (int trial = 0; trial < TRIALS; trial++) {
            Relation relation = randomRelation(random);
            Set<FunctionalDependency> fds = relation.getFunctionalDependencies();
            Set<String> attributes = relation.getAttributes();
            Set<Relation> result = synthesizer.synthesize(relation);
            String context = relation + " " + fds + " -> " + result;

            Set<String> covered = new HashSet<>();
            result.forEach(r -> covered.addAll(r.getAttributes()));
            assertEquals(attributes, covered, context);

            // Lossless: synthesis guarantees some relation contains a key of R
            assertTrue(result.stream().anyMatch(r ->
                closureComputer.computeClosure(r.getAttributes(), fds)
                    .containsAll(attributes)), "no key relation: " + context);

            assertTrue(preservesDependencies(fds, result), "lost FDs: " + context);

            for (Relation r : result) {
                assertTrue(isIn3nf(r.getAttributes(), fds), r + " not 3NF: " + context);
            }

            // No schema is a duplicate of, or contained in, another
            List<Set<String>> schemas = result.stream()
                .map(Relation::getAttributes).toList();
            for (int i = 0; i < schemas.size(); i++) {
                for (int j = 0; j < schemas.size(); j++) {
                    assertFalse(i != j && schemas.get(j).containsAll(schemas.get(i)),
                        "subsumed schema " + schemas.get(i) + ": " + context);
                }
            }
        }
    }

    @Test
    public void testMinimalCoverIsEquivalentAndMinimal() {
        MinimalCoverComputer computer = new MinimalCoverComputer();
        Random random = new Random(5);
        for (int trial = 0; trial < TRIALS; trial++) {
            Set<FunctionalDependency> fds = randomRelation(random).getFunctionalDependencies();
            Set<FunctionalDependency> cover = computer.computeMinimalCover(fds);
            String context = fds + " -> " + cover;

            assertTrue(implies(cover, fds) && implies(fds, cover),
                "not equivalent: " + context);

            List<FunctionalDependency> singletons = new ArrayList<>();
            for (FunctionalDependency fd : cover) {
                assertFalse(fd.getLeftSide().containsAll(fd.getRightSide()),
                    "trivial FD in cover: " + context);
                for (String a : fd.getRightSide()) {
                    singletons.add(new FunctionalDependency(fd.getLeftSide(), Set.of(a)));
                }
            }

            Set<FunctionalDependency> all = new HashSet<>(singletons);
            for (FunctionalDependency fd : singletons) {
                Set<FunctionalDependency> others = new HashSet<>(all);
                others.remove(fd);
                assertFalse(implies(others, Set.of(fd)),
                    "redundant " + fd + ": " + context);

                if (fd.getLeftSide().size() > 1) {
                    for (String b : fd.getLeftSide()) {
                        Set<String> reduced = new HashSet<>(fd.getLeftSide());
                        reduced.remove(b);
                        assertFalse(closureComputer.computeClosure(reduced, all)
                                .containsAll(fd.getRightSide()),
                            b + " extraneous in " + fd + ": " + context);
                    }
                }
            }
        }
    }

    @Test
    public void testHighestNormalFormMatchesBruteForce() {
        NormalFormChecker checker = new NormalFormChecker();
        CandidateKeyFinder keyFinder = new CandidateKeyFinder();
        Random random = new Random(9);
        for (int trial = 0; trial < TRIALS; trial++) {
            Relation relation = randomRelation(random);
            Set<String> attributes = relation.getAttributes();
            Set<FunctionalDependency> fds = relation.getFunctionalDependencies();
            Set<CandidateKey> keys = keyFinder.findAllCandidateKeys(attributes, fds);

            assertEquals(bruteForceHighestNormalForm(attributes, fds),
                checker.determineHighestNormalForm(attributes, fds, keys)
                    .getHighestNormalForm(),
                relation + " " + fds);
        }
    }

    // ---------------------------------------------------------------
    // Oracles
    // ---------------------------------------------------------------

    private NormalFormResult.NormalForm bruteForceHighestNormalForm(
            Set<String> attributes, Set<FunctionalDependency> fds) {
        if (isInBcnf(attributes, fds)) {
            return NormalFormResult.NormalForm.BCNF;
        }
        if (isIn3nf(attributes, fds)) {
            return NormalFormResult.NormalForm.THIRD_NF;
        }
        // 2NF: no non-prime attribute depends on a proper subset of a key
        List<Set<String>> keys = keysOf(attributes, fds);
        Set<String> prime = new HashSet<>();
        keys.forEach(prime::addAll);
        for (Set<String> key : keys) {
            for (Set<String> subset : subsets(key)) {
                if (subset.size() == key.size()) {
                    continue;
                }
                Set<String> closure = closureComputer.computeClosure(subset, fds);
                for (String a : closure) {
                    if (attributes.contains(a) && !prime.contains(a)
                            && !subset.contains(a)) {
                        return NormalFormResult.NormalForm.FIRST_NF;
                    }
                }
            }
        }
        return NormalFormResult.NormalForm.SECOND_NF;
    }

    /** X⁺ ∩ schema adds something but is not all of schema, for some X. */
    private boolean isInBcnf(Set<String> schema, Set<FunctionalDependency> fds) {
        for (Set<String> x : subsets(schema)) {
            Set<String> closure = restrictedClosure(x, schema, fds);
            if (!x.containsAll(closure) && !closure.containsAll(schema)) {
                return false;
            }
        }
        return true;
    }

    /** Every X → A on the schema has X a superkey or A prime (in the schema). */
    private boolean isIn3nf(Set<String> schema, Set<FunctionalDependency> fds) {
        Set<String> prime = new HashSet<>();
        keysOf(schema, fds).forEach(prime::addAll);
        for (Set<String> x : subsets(schema)) {
            Set<String> closure = restrictedClosure(x, schema, fds);
            if (closure.containsAll(schema)) {
                continue;
            }
            for (String a : closure) {
                if (!x.contains(a) && !prime.contains(a)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Minimal X ⊆ schema with X⁺ ⊇ schema. */
    private List<Set<String>> keysOf(Set<String> schema, Set<FunctionalDependency> fds) {
        List<Set<String>> superKeys = new ArrayList<>();
        for (Set<String> x : subsets(schema)) {
            if (restrictedClosure(x, schema, fds).containsAll(schema)) {
                superKeys.add(x);
            }
        }
        List<Set<String>> keys = new ArrayList<>();
        for (Set<String> k : superKeys) {
            if (superKeys.stream().noneMatch(o -> !o.equals(k) && k.containsAll(o))) {
                keys.add(k);
            }
        }
        return keys;
    }

    /** Union of the brute-force projections of F⁺ implies every FD of F. */
    private boolean preservesDependencies(
            Set<FunctionalDependency> fds, Set<Relation> decomposition) {
        Set<FunctionalDependency> projected = new HashSet<>();
        for (Relation r : decomposition) {
            for (Set<String> x : subsets(r.getAttributes())) {
                projected.add(new FunctionalDependency(
                    x, restrictedClosure(x, r.getAttributes(), fds)));
            }
        }
        return implies(projected, fds);
    }

    private boolean implies(Set<FunctionalDependency> g, Set<FunctionalDependency> f) {
        for (FunctionalDependency fd : f) {
            if (!closureComputer.computeClosure(fd.getLeftSide(), g)
                    .containsAll(fd.getRightSide())) {
                return false;
            }
        }
        return true;
    }

    private Set<String> restrictedClosure(
            Set<String> x, Set<String> schema, Set<FunctionalDependency> fds) {
        Set<String> closure = closureComputer.computeClosure(x, fds);
        closure.retainAll(schema);
        return closure;
    }

    /** All non-empty subsets. */
    private static List<Set<String>> subsets(Set<String> attributes) {
        List<String> list = new ArrayList<>(attributes);
        List<Set<String>> subsets = new ArrayList<>();
        for (int mask = 1; mask < (1 << list.size()); mask++) {
            Set<String> subset = new HashSet<>();
            for (int i = 0; i < list.size(); i++) {
                if ((mask & (1 << i)) != 0) {
                    subset.add(list.get(i));
                }
            }
            subsets.add(subset);
        }
        return subsets;
    }

    private static Relation randomRelation(Random random) {
        int attributeCount = 2 + random.nextInt(5);
        List<String> attributes = new ArrayList<>();
        for (int i = 0; i < attributeCount; i++) {
            attributes.add(String.valueOf((char) ('A' + i)));
        }
        Set<FunctionalDependency> fds = new HashSet<>();
        int fdCount = random.nextInt(6);
        for (int i = 0; i < fdCount; i++) {
            fds.add(new FunctionalDependency(
                randomNonEmptySubset(attributes, random),
                randomNonEmptySubset(attributes, random)));
        }
        return new Relation("R", new HashSet<>(attributes), fds);
    }

    private static Set<String> randomNonEmptySubset(
            List<String> attributes, Random random) {
        Set<String> subset = new HashSet<>();
        while (subset.isEmpty()) {
            for (String attribute : attributes) {
                if (random.nextInt(3) == 0) {
                    subset.add(attribute);
                }
            }
        }
        return subset;
    }
}
