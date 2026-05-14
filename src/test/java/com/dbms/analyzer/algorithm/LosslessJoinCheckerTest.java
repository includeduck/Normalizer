package com.dbms.analyzer.algorithm;

import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.model.Relation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class LosslessJoinCheckerTest {

    private final LosslessJoinChecker checker = new LosslessJoinChecker();

    @Test
    public void testBinaryLossless() {
        // R(A,B,C), FD: A -> B
        // Decompose into R1(A,B) and R2(A,C)
        // Intersection {A} → determines {A,B} = R1 — lossless
        Set<String> attrs = Set.of("A", "B", "C");
        Set<FunctionalDependency> fds = Set.of(
                new FunctionalDependency(Set.of("A"), Set.of("B")));

        Relation r1 = new Relation("R1", Set.of("A", "B"));
        Relation r2 = new Relation("R2", Set.of("A", "C"));

        assertTrue(checker.isLossless(attrs, fds, Set.of(r1, r2)));
    }

    @Test
    public void testBinaryLossy() {
        // R(A,B,C), no FDs
        // Decompose into R1(A,B) and R2(B,C)
        // Intersection {B}, closure of {B} = {B} — not a superkey of either
        Set<String> attrs = Set.of("A", "B", "C");
        Set<FunctionalDependency> fds = Set.of();

        Relation r1 = new Relation("R1", Set.of("A", "B"));
        Relation r2 = new Relation("R2", Set.of("B", "C"));

        assertFalse(checker.isLossless(attrs, fds, Set.of(r1, r2)));
    }

    @Test
    public void testBinaryLosslessWithTransitiveClosure() {
        // R(A,B,C), FDs: A -> B, B -> C
        // Decompose into R1(A,B) and R2(A,C)
        // Intersection {A}, closure = {A,B,C} — superkey of both
        Set<String> attrs = Set.of("A", "B", "C");
        Set<FunctionalDependency> fds = Set.of(
                new FunctionalDependency(Set.of("A"), Set.of("B")),
                new FunctionalDependency(Set.of("B"), Set.of("C")));

        Relation r1 = new Relation("R1", Set.of("A", "B"));
        Relation r2 = new Relation("R2", Set.of("A", "C"));

        assertTrue(checker.isLossless(attrs, fds, Set.of(r1, r2)));
    }

    @Test
    public void testThreeWayChase() {
        // R(A,B,C,D), FDs: A -> B, B -> C
        // Decompose into R1(A,B), R2(B,C), R3(A,D)
        Set<String> attrs = Set.of("A", "B", "C", "D");
        Set<FunctionalDependency> fds = Set.of(
                new FunctionalDependency(Set.of("A"), Set.of("B")),
                new FunctionalDependency(Set.of("B"), Set.of("C")));

        Relation r1 = new Relation("R1", Set.of("A", "B"));
        Relation r2 = new Relation("R2", Set.of("B", "C"));
        Relation r3 = new Relation("R3", Set.of("A", "D"));

        boolean lossless = checker.isLossless(attrs, fds, Set.of(r1, r2, r3));
        assertTrue(lossless);
    }

    @Test
    public void testCheckWithSteps() {
        Set<String> attrs = Set.of("A", "B");
        Set<FunctionalDependency> fds = Set.of(
                new FunctionalDependency(Set.of("A"), Set.of("B")));

        Relation r1 = new Relation("R1", Set.of("A", "B"));

        List<String> steps = checker.checkWithSteps(attrs, fds, Set.of(r1));
        assertFalse(steps.isEmpty());
    }
}
