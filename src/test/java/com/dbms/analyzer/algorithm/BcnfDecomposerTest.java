package com.dbms.analyzer.algorithm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import com.dbms.analyzer.model.DecompositionStep;
import com.dbms.analyzer.model.Relation;
import com.dbms.analyzer.model.FunctionalDependency;

public class BcnfDecomposerTest {

    private BcnfDecomposer bcnfDecomposer = new BcnfDecomposer();
    private final ClosureComputer closureComputer = new ClosureComputer();

    @Test
    public void testAlreadyBcnf() {
        Set<String> attributes = new HashSet<>();
        attributes.add("A");
        attributes.add("B");

        Relation relation = new Relation("R", attributes);
        relation.addFunctionalDependency(new FunctionalDependency(
            Set.of("A"), Set.of("B")));

        Set<Relation> decomposed = bcnfDecomposer.decomposeToBcnf(relation);

        // Already in BCNF, should return single relation
        assertEquals(1, decomposed.size());
    }

    @Test
    public void testBcnfViolation() {
        Set<String> attributes = new HashSet<>();
        attributes.add("A");
        attributes.add("B");
        attributes.add("C");

        Relation relation = new Relation("R", attributes);
        relation.addFunctionalDependency(new FunctionalDependency(
            Set.of("B"), Set.of("C")));

        Set<Relation> decomposed = bcnfDecomposer.decomposeToBcnf(relation);

        // B -> C splits R into (B,C) and (A,B)
        assertEquals(Set.of(Set.of("B", "C"), Set.of("A", "B")),
            schemas(decomposed));
    }

    @Test
    public void testEmptyFds() {
        Set<String> attributes = new HashSet<>();
        attributes.add("A");
        attributes.add("B");

        Relation relation = new Relation("R", attributes);

        Set<Relation> decomposed = bcnfDecomposer.decomposeToBcnf(relation);

        // No FDs, should be in BCNF
        assertEquals(1, decomposed.size());
    }

    @Test
    public void testViolationImpliedThroughRemovedAttribute() {
        // BUG-001: R(A,B,C,D), A -> B, B -> A, C -> B. After splitting off
        // (A,B), C -> A still holds on (A,C,D) via B, so (A,C,D) is not BCNF.
        Relation relation = new Relation("R", Set.of("A", "B", "C", "D"));
        relation.addFunctionalDependency(fd("A", "B"));
        relation.addFunctionalDependency(fd("B", "A"));
        relation.addFunctionalDependency(fd("C", "B"));

        Set<Relation> decomposed = bcnfDecomposer.decomposeToBcnf(relation);

        Set<Set<String>> schemas = schemas(decomposed);
        assertFalse(schemas.contains(Set.of("A", "C", "D")),
            "(A,C,D) is not in BCNF: " + schemas);
        assertAllInBcnf(relation, decomposed);
        assertTrue(schemas.contains(Set.of("C", "D")), schemas.toString());
    }

    @Test
    public void testFinalRelationsCarryProjectedFds() {
        // R(A,B,C), A -> B, B -> C: (A,B) and (B,C); A -> C is not shown on
        // either, but each relation gets its true projected FDs.
        Relation relation = new Relation("R", Set.of("A", "B", "C"));
        relation.addFunctionalDependency(fd("A", "B"));
        relation.addFunctionalDependency(fd("B", "C"));

        for (Relation r : bcnfDecomposer.decomposeToBcnf(relation)) {
            assertProjectionEquivalent(relation, r);
        }
    }

    @Test
    public void testStepNumbersAreSequential() {
        // BUG-010: numbering used to repeat and skip
        Relation relation = new Relation("R",
            Set.of("A", "B", "C", "D", "E", "F", "G"));
        relation.addFunctionalDependency(fd("A", "B"));
        relation.addFunctionalDependency(fd("B", "C"));
        relation.addFunctionalDependency(fd("D", "E"));
        relation.addFunctionalDependency(fd("E", "F"));

        List<DecompositionStep> steps = new ArrayList<>();
        bcnfDecomposer.decomposeToBcnfWithSteps(relation, steps);

        assertTrue(steps.size() >= 3);
        for (int i = 0; i < steps.size(); i++) {
            assertEquals(i + 1, steps.get(i).getStepNumber());
        }
    }

    @Test
    public void testInvariantsOnRandomRelations() {
        Random random = new Random(7);
        LosslessJoinChecker losslessJoinChecker = new LosslessJoinChecker();

        for (int trial = 0; trial < 300; trial++) {
            int attributeCount = 2 + random.nextInt(5);
            List<String> attributes = new ArrayList<>();
            for (int i = 0; i < attributeCount; i++) {
                attributes.add(String.valueOf((char) ('A' + i)));
            }
            Relation relation = new Relation("R", new HashSet<>(attributes));
            int fdCount = random.nextInt(6);
            for (int i = 0; i < fdCount; i++) {
                relation.addFunctionalDependency(new FunctionalDependency(
                    randomNonEmptySubset(attributes, random),
                    randomNonEmptySubset(attributes, random)));
            }

            Set<Relation> decomposed = bcnfDecomposer.decomposeToBcnf(relation);
            String context = relation + " " + relation.getFunctionalDependencies();

            Set<String> covered = new HashSet<>();
            decomposed.forEach(r -> covered.addAll(r.getAttributes()));
            assertEquals(relation.getAttributes(), covered, context);

            assertAllInBcnf(relation, decomposed);
            assertTrue(losslessJoinChecker.isLossless(
                relation.getAttributes(),
                relation.getFunctionalDependencies(),
                decomposed), context);
            for (Relation r : decomposed) {
                assertProjectionEquivalent(relation, r);
            }

            List<DecompositionStep> steps = new ArrayList<>();
            assertEquals(schemas(decomposed), schemas(
                bcnfDecomposer.decomposeToBcnfWithSteps(relation, steps)),
                context);
        }
    }

    @Test
    public void testLargestAllowedRelationFinishesQuickly() {
        // Chain x0 -> x1 -> ... -> x19 forces many sub-relation checks
        Set<String> attributes = new HashSet<>();
        Relation relation;
        for (int i = 0; i < AnalysisLimits.MAX_ATTRIBUTES; i++) {
            attributes.add("x" + i);
        }
        relation = new Relation("R", attributes);
        for (int i = 0; i + 1 < AnalysisLimits.MAX_ATTRIBUTES; i++) {
            relation.addFunctionalDependency(fd("x" + i, "x" + (i + 1)));
        }

        Set<Relation> decomposed = assertTimeoutPreemptively(
            Duration.ofSeconds(10),
            () -> bcnfDecomposer.decomposeToBcnf(relation));

        assertEquals(AnalysisLimits.MAX_ATTRIBUTES - 1, decomposed.size());
    }

    // ---------------------------------------------------------------
    // Oracles (set-based, brute force, independent of the implementation)
    // ---------------------------------------------------------------

    /** Ri is in BCNF iff no X ⊆ Ri has X⁺ ∩ Ri ⊋ X and X⁺ ∩ Ri ≠ Ri. */
    private void assertAllInBcnf(Relation original, Set<Relation> decomposed) {
        Set<FunctionalDependency> fds = original.getFunctionalDependencies();
        for (Relation r : decomposed) {
            Set<String> schema = r.getAttributes();
            for (Set<String> x : subsets(schema)) {
                Set<String> closure = closureComputer.computeClosure(x, fds);
                closure.retainAll(schema);
                boolean violates = !x.containsAll(closure)
                    && !closure.containsAll(schema);
                assertFalse(violates, r + " is not in BCNF: " + x
                    + "+ = " + closure + " under " + fds);
            }
        }
    }

    /** The FDs attached to r must generate exactly the projection of F⁺. */
    private void assertProjectionEquivalent(Relation original, Relation r) {
        Set<String> schema = r.getAttributes();
        for (Set<String> x : subsets(schema)) {
            Set<String> expected = closureComputer.computeClosure(
                x, original.getFunctionalDependencies());
            expected.retainAll(schema);
            Set<String> actual = closureComputer.computeClosure(
                x, r.getFunctionalDependencies());
            actual.retainAll(schema);
            assertEquals(expected, actual, "projection onto " + r
                + " for X=" + x + ", FDs shown: " + r.getFunctionalDependencies());
        }
    }

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

    private static Set<Set<String>> schemas(Set<Relation> relations) {
        return relations.stream()
            .map(Relation::getAttributes)
            .collect(Collectors.toSet());
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

    private static FunctionalDependency fd(String left, String right) {
        return new FunctionalDependency(Set.of(left), Set.of(right));
    }
}
