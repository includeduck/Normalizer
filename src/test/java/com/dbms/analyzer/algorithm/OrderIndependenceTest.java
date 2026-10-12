package com.dbms.analyzer.algorithm;

import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.model.Relation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BUG-008: results and traces must depend only on the FDs, never on the
 * order in which a set happens to iterate them.
 */
public class OrderIndependenceTest {

    @Test
    public void testMinimalCoverOfReportExampleIsOrderIndependent() {
        List<FunctionalDependency> fds = List.of(
            fd("A", "B"), fd("B", "A"), fd("A", "C"), fd("B", "C"));
        MinimalCoverComputer computer = new MinimalCoverComputer();

        Set<String> results = new HashSet<>();
        for (List<FunctionalDependency> order : permutations(fds)) {
            results.add(computer.computeMinimalCover(
                new LinkedHashSet<>(order)).toString());
        }

        assertEquals(Set.of("[A -> B,C, B -> A]"), results);
    }

    @Test
    public void testThreeNfAddsSmallestKeyAlphabetically() {
        // Keys {A,C} and {B,C}; no FD schema contains a key, so one is added
        Relation relation = new OrderedRelation("R", Set.of("A", "B", "C", "D"),
            List.of(fd("B", "A"), fd("A", "B"), fd("C", "D")));

        Set<Set<String>> schemas = new HashSet<>();
        new ThreeNfSynthesizer().synthesize(relation)
            .forEach(r -> schemas.add(r.getAttributes()));

        assertTrue(schemas.contains(Set.of("A", "C")), schemas.toString());
        assertFalse(schemas.contains(Set.of("B", "C")), schemas.toString());
    }

    @Test
    public void testAllOutputsAreOrderIndependentOnRandomRelations() {
        Random random = new Random(11);
        for (int trial = 0; trial < 150; trial++) {
            int attributeCount = 2 + random.nextInt(5);
            List<String> attributes = new ArrayList<>();
            for (int i = 0; i < attributeCount; i++) {
                attributes.add(String.valueOf((char) ('A' + i)));
            }
            List<FunctionalDependency> fds = new ArrayList<>();
            int fdCount = 1 + random.nextInt(5);
            for (int i = 0; i < fdCount; i++) {
                FunctionalDependency fd = new FunctionalDependency(
                    randomNonEmptySubset(attributes, random),
                    randomNonEmptySubset(attributes, random));
                if (!fds.contains(fd)) {
                    fds.add(fd);
                }
            }

            List<String> baseline = null;
            for (int shuffle = 0; shuffle < 6; shuffle++) {
                List<FunctionalDependency> order = new ArrayList<>(fds);
                Collections.shuffle(order, random);
                List<String> outputs = allOutputs(attributes, order);
                if (baseline == null) {
                    baseline = outputs;
                } else {
                    assertEquals(baseline, outputs, "FDs " + fds
                        + " gave different output in order " + order);
                }
            }
        }
    }

    /** Renders every algorithm's result and trace for one FD ordering. */
    private static List<String> allOutputs(
            List<String> attributes, List<FunctionalDependency> order) {
        Set<String> attributeSet = new HashSet<>(attributes);
        Set<FunctionalDependency> fdSet = new LinkedHashSet<>(order);
        Relation relation = new OrderedRelation("R", attributeSet, order);
        Function<Set<Relation>, String> render = relations -> {
            StringBuilder text = new StringBuilder();
            relations.forEach(r -> text.append(r).append(' ')
                .append(FdOrder.sorted(r.getFunctionalDependencies())).append("; "));
            return text.toString();
        };

        Set<Relation> bcnf = new BcnfDecomposer().decomposeToBcnf(relation);
        Set<Relation> threeNf = new ThreeNfSynthesizer().synthesize(relation);
        LosslessJoinChecker lossless = new LosslessJoinChecker();
        DependencyPreservationChecker preservation =
            new DependencyPreservationChecker();

        List<String> outputs = new ArrayList<>();
        outputs.add(new MinimalCoverComputer().computeMinimalCover(fdSet).toString());
        outputs.addAll(new MinimalCoverComputer().computeMinimalCoverWithSteps(fdSet));
        outputs.addAll(new ClosureComputer().computeClosureWithSteps(
            Set.of(attributes.get(0)), fdSet));
        outputs.add(render.apply(bcnf));
        outputs.addAll(new BcnfDecomposer().decomposeWithExplanation(relation));
        outputs.add(render.apply(threeNf));
        outputs.addAll(new ThreeNfSynthesizer().synthesizeWithSteps(relation));
        outputs.addAll(preservation.checkWithSteps(fdSet, new LinkedHashSet<>(bcnf)));
        outputs.addAll(lossless.checkWithSteps(attributeSet, fdSet, threeNf));
        return outputs;
    }

    /** A relation whose FDs iterate in a chosen order. */
    private static final class OrderedRelation extends Relation {
        private final List<FunctionalDependency> order;

        OrderedRelation(String name, Set<String> attributes,
                        List<FunctionalDependency> order) {
            super(name, attributes, new LinkedHashSet<>(order));
            this.order = order;
        }

        @Override
        public Set<FunctionalDependency> getFunctionalDependencies() {
            return new LinkedHashSet<>(order);
        }
    }

    private static List<List<FunctionalDependency>> permutations(
            List<FunctionalDependency> items) {
        if (items.size() <= 1) {
            return List.of(items);
        }
        List<List<FunctionalDependency>> result = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            List<FunctionalDependency> rest = new ArrayList<>(items);
            FunctionalDependency first = rest.remove(i);
            for (List<FunctionalDependency> tail : permutations(rest)) {
                List<FunctionalDependency> permutation = new ArrayList<>();
                permutation.add(first);
                permutation.addAll(tail);
                result.add(permutation);
            }
        }
        return result;
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
