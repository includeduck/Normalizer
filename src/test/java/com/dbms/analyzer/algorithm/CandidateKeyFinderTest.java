package com.dbms.analyzer.algorithm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.model.CandidateKey;

public class CandidateKeyFinderTest {

    private CandidateKeyFinder candidateKeyFinder =
        new CandidateKeyFinder();

    @Test
    public void testSimpleCandidateKey() {
        Set<String> attributes = new HashSet<>();
        attributes.add("A");
        attributes.add("B");
        attributes.add("C");

        Set<FunctionalDependency> fds = new HashSet<>();
        fds.add(new FunctionalDependency(
            Set.of("A"), Set.of("B")));
        fds.add(new FunctionalDependency(
            Set.of("B"), Set.of("C")));

        Set<CandidateKey> keys =
            candidateKeyFinder.findAllCandidateKeys(attributes, fds);

        assertEquals(Set.of(new CandidateKey(Set.of("A"))), keys);
    }

    @Test
    public void testNoCandidateKeys() {
        Set<String> attributes = new HashSet<>();
        attributes.add("A");
        attributes.add("B");

        Set<FunctionalDependency> fds = new HashSet<>();

        Set<CandidateKey> keys =
            candidateKeyFinder.findAllCandidateKeys(attributes, fds);

        // The whole attribute set is the only candidate key
        assertEquals(Set.of(new CandidateKey(Set.of("A", "B"))), keys);
    }

    @Test
    public void testMultipleCandidateKeys() {
        // R(A,B,C) with A -> B, B -> A: keys {A,C} and {B,C}
        Set<FunctionalDependency> fds = Set.of(
            new FunctionalDependency(Set.of("A"), Set.of("B")),
            new FunctionalDependency(Set.of("B"), Set.of("A")));

        Set<CandidateKey> keys = candidateKeyFinder.findAllCandidateKeys(
            Set.of("A", "B", "C"), fds);

        assertEquals(Set.of(
            new CandidateKey(Set.of("A", "C")),
            new CandidateKey(Set.of("B", "C"))), keys);
    }

    @Test
    public void testRelationAboveLimitIsRejected() {
        // BUG-003: 32 attributes used to overflow `1 << n` and return no keys
        Set<String> attributes = numberedAttributes(32);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> candidateKeyFinder.findAllCandidateKeys(attributes, Set.of()));
        assertTrue(error.getMessage().contains(
            String.valueOf(AnalysisLimits.MAX_ATTRIBUTES)));
    }

    @Test
    public void testLargestAllowedRelationWithoutFds() {
        Set<String> attributes = numberedAttributes(AnalysisLimits.MAX_ATTRIBUTES);

        Set<CandidateKey> keys =
            candidateKeyFinder.findAllCandidateKeys(attributes, Set.of());

        assertEquals(Set.of(new CandidateKey(attributes)), keys);
    }

    @Test
    public void testWorstCaseAtLimitFinishesQuickly() {
        // BUG-004: 10 pairs a_i <-> b_i give 2^10 keys of size 10
        Set<String> attributes = new HashSet<>();
        Set<FunctionalDependency> fds = new HashSet<>();
        for (int i = 0; i < AnalysisLimits.MAX_ATTRIBUTES / 2; i++) {
            attributes.add("a" + i);
            attributes.add("b" + i);
            fds.add(new FunctionalDependency(Set.of("a" + i), Set.of("b" + i)));
            fds.add(new FunctionalDependency(Set.of("b" + i), Set.of("a" + i)));
        }

        Set<CandidateKey> keys = assertTimeoutPreemptively(
            Duration.ofSeconds(5),
            () -> candidateKeyFinder.findAllCandidateKeys(attributes, fds));

        assertEquals(1 << (AnalysisLimits.MAX_ATTRIBUTES / 2), keys.size());
    }

    @Test
    public void testMatchesBruteForceOnRandomRelations() {
        Random random = new Random(42);
        for (int trial = 0; trial < 500; trial++) {
            int attributeCount = 1 + random.nextInt(8);
            List<String> attributes = new ArrayList<>(
                numberedAttributes(attributeCount));
            Set<FunctionalDependency> fds = new HashSet<>();
            int fdCount = random.nextInt(7);
            for (int i = 0; i < fdCount; i++) {
                fds.add(new FunctionalDependency(
                    randomNonEmptySubset(attributes, random),
                    randomNonEmptySubset(attributes, random)));
            }

            Set<String> attributeSet = new HashSet<>(attributes);
            assertEquals(
                bruteForceKeys(attributeSet, fds),
                candidateKeyFinder.findAllCandidateKeys(attributeSet, fds),
                "attributes=" + attributeSet + " fds=" + fds);
        }
    }

    private static Set<String> numberedAttributes(int count) {
        Set<String> attributes = new HashSet<>();
        for (int i = 0; i < count; i++) {
            attributes.add("x" + i);
        }
        return attributes;
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

    /** Oracle: every minimal subset whose closure covers all attributes. */
    private static Set<CandidateKey> bruteForceKeys(
            Set<String> attributes, Set<FunctionalDependency> fds) {
        ClosureComputer closureComputer = new ClosureComputer();
        List<String> list = new ArrayList<>(attributes);
        List<Set<String>> superKeys = new ArrayList<>();
        for (int mask = 0; mask < (1 << list.size()); mask++) {
            Set<String> subset = new HashSet<>();
            for (int j = 0; j < list.size(); j++) {
                if ((mask & (1 << j)) != 0) {
                    subset.add(list.get(j));
                }
            }
            if (closureComputer.computeClosure(subset, fds).containsAll(attributes)) {
                superKeys.add(subset);
            }
        }

        Set<CandidateKey> keys = new HashSet<>();
        for (Set<String> superKey : superKeys) {
            boolean minimal = superKeys.stream().noneMatch(other ->
                !other.equals(superKey) && superKey.containsAll(other));
            if (minimal) {
                keys.add(new CandidateKey(superKey));
            }
        }
        return keys;
    }
}
