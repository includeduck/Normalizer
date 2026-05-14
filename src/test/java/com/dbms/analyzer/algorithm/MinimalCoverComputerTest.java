package com.dbms.analyzer.algorithm;

import com.dbms.analyzer.model.FunctionalDependency;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class MinimalCoverComputerTest {

    private final MinimalCoverComputer computer = new MinimalCoverComputer();

    @Test
    public void testEmptyFds() {
        Set<FunctionalDependency> result =
                computer.computeMinimalCover(Set.of());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSingletonAlreadyMinimal() {
        Set<FunctionalDependency> fds = Set.of(
                new FunctionalDependency(Set.of("A"), Set.of("B")));

        Set<FunctionalDependency> result = computer.computeMinimalCover(fds);
        assertEquals(1, result.size());
        assertTrue(result.contains(
                new FunctionalDependency(Set.of("A"), Set.of("B"))));
    }

    @Test
    public void testDecomposeRightSide() {
        // A -> BC should remain as A -> BC (recombined) in minimal cover
        Set<FunctionalDependency> fds = Set.of(
                new FunctionalDependency(Set.of("A"), Set.of("B", "C")));

        Set<FunctionalDependency> result = computer.computeMinimalCover(fds);
        assertEquals(1, result.size());

        FunctionalDependency fd = result.iterator().next();
        assertEquals(Set.of("A"), fd.getLeftSide());
        assertTrue(fd.getRightSide().containsAll(Set.of("B", "C")));
    }

    @Test
    public void testRemoveExtraneousLhsAttribute() {
        // AB -> C where A -> C also holds means B is extraneous in AB -> C.
        // After extraneous removal both become A -> C, then redundancy
        // removes the duplicate, leaving a single A -> C.
        Set<FunctionalDependency> fds = Set.of(
                new FunctionalDependency(Set.of("A", "B"), Set.of("C")),
                new FunctionalDependency(Set.of("A"), Set.of("C")));

        Set<FunctionalDependency> result = computer.computeMinimalCover(fds);
        // Should reduce to just A -> C
        assertEquals(1, result.size());
        FunctionalDependency fd = result.iterator().next();
        assertTrue(fd.getLeftSide().contains("A"),
                "LHS should contain A");
        assertEquals(Set.of("C"), fd.getRightSide());
    }

    @Test
    public void testRemoveRedundantFd() {
        // A -> B, B -> C, A -> C  — the last FD is redundant
        Set<FunctionalDependency> fds = Set.of(
                new FunctionalDependency(Set.of("A"), Set.of("B")),
                new FunctionalDependency(Set.of("B"), Set.of("C")),
                new FunctionalDependency(Set.of("A"), Set.of("C")));

        Set<FunctionalDependency> result = computer.computeMinimalCover(fds);
        // Should be A -> B, B -> C
        assertEquals(2, result.size());
    }

    @Test
    public void testWithStepsReturnsNonEmpty() {
        Set<FunctionalDependency> fds = Set.of(
                new FunctionalDependency(Set.of("A"), Set.of("B")),
                new FunctionalDependency(Set.of("B"), Set.of("C")));

        List<String> steps = computer.computeMinimalCoverWithSteps(fds);
        assertFalse(steps.isEmpty());
        assertTrue(steps.get(0).contains("Minimal Cover"));
    }
}
