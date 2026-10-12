package com.dbms.analyzer.algorithm;

import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.model.Relation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DependencyPreservationCheckerTest {

    private final DependencyPreservationChecker checker =
            new DependencyPreservationChecker();

    @Test
    public void testPreservedWhenAllFdsCovered() {
        // R(A,B,C), FDs: A -> B, B -> C
        // Decompose into R1(A,B) and R2(B,C)
        // Both FDs are directly present in sub-relations
        Set<FunctionalDependency> fds = Set.of(
                new FunctionalDependency(Set.of("A"), Set.of("B")),
                new FunctionalDependency(Set.of("B"), Set.of("C")));

        Relation r1 = new Relation("R1", Set.of("A", "B"));
        r1 = r1.withFunctionalDependency(fds.iterator().next());

        Relation r2 = new Relation("R2", Set.of("B", "C"));

        assertTrue(checker.isPreserved(fds, Set.of(r1, r2)));
    }

    @Test
    public void testNotPreservedWhenFdLost() {
        // R(A,B,C), FD: A -> C
        // Decompose into R1(A,B) and R2(B,C)
        // A -> C is lost (A and C not in same relation, and B doesn't help)
        Set<FunctionalDependency> fds = Set.of(
                new FunctionalDependency(Set.of("A"), Set.of("C")));

        Relation r1 = new Relation("R1", Set.of("A", "B"));
        Relation r2 = new Relation("R2", Set.of("B", "C"));

        assertFalse(checker.isPreserved(fds, Set.of(r1, r2)));
    }

    @Test
    public void testFindLostDependencies() {
        Set<FunctionalDependency> fds = Set.of(
                new FunctionalDependency(Set.of("A"), Set.of("B")),
                new FunctionalDependency(Set.of("A"), Set.of("C")));

        Relation r1 = new Relation("R1", Set.of("A", "B"));
        Relation r2 = new Relation("R2", Set.of("B", "C"));

        List<FunctionalDependency> lost =
                checker.findLostDependencies(fds, Set.of(r1, r2));

        // A -> C is lost
        assertEquals(1, lost.size());
        assertEquals(Set.of("C"), lost.get(0).getRightSide());
    }

    @Test
    public void testPreservedThroughImpliedDependencies() {
        // BUG-002: F = {A->B, B->C, C->A}, decomposition {AB, BC}.
        // C->A is preserved: C->B holds on BC (C->A->B), B->A holds on AB.
        Set<FunctionalDependency> fds = Set.of(
                new FunctionalDependency(Set.of("A"), Set.of("B")),
                new FunctionalDependency(Set.of("B"), Set.of("C")),
                new FunctionalDependency(Set.of("C"), Set.of("A")));

        Relation r1 = new Relation("R1", Set.of("A", "B"));
        Relation r2 = new Relation("R2", Set.of("B", "C"));

        assertTrue(checker.findLostDependencies(fds, Set.of(r1, r2)).isEmpty());
        assertTrue(checker.isPreserved(fds, Set.of(r1, r2)));
    }

    @Test
    public void testPreservedWhenOriginalFdSpansRelations() {
        // BUG-002: F = {A->B, B->A, C->B}, decomposition {AB, ACD}.
        // C->B is preserved: C->A holds on ACD (C->B->A), A->B holds on AB.
        Set<FunctionalDependency> fds = Set.of(
                new FunctionalDependency(Set.of("A"), Set.of("B")),
                new FunctionalDependency(Set.of("B"), Set.of("A")),
                new FunctionalDependency(Set.of("C"), Set.of("B")));

        Relation r1 = new Relation("R1", Set.of("A", "B"));
        Relation r2 = new Relation("R2", Set.of("A", "C", "D"));

        assertTrue(checker.isPreserved(fds, Set.of(r1, r2)));
        List<String> steps = checker.checkWithSteps(fds, Set.of(r1, r2));
        assertTrue(steps.stream().noneMatch(s -> s.contains("LOST")));
    }

    @Test
    public void testCheckWithSteps() {
        Set<FunctionalDependency> fds = Set.of(
                new FunctionalDependency(Set.of("A"), Set.of("B")));

        Relation r1 = new Relation("R1", Set.of("A", "B"));

        List<String> steps = checker.checkWithSteps(fds, Set.of(r1));
        assertFalse(steps.isEmpty());
        assertTrue(steps.stream().anyMatch(s -> s.contains("PRESERVED")));
    }
}
