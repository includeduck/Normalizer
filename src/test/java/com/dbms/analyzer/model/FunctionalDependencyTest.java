package com.dbms.analyzer.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FunctionalDependencyTest {

    @Test
    public void testReversedDependencyHasDifferentHash() {
        // BUG-014: the additive hash made X -> Y and Y -> X always collide
        FunctionalDependency forward = new FunctionalDependency(Set.of("A"), Set.of("B"));
        FunctionalDependency reverse = new FunctionalDependency(Set.of("B"), Set.of("A"));

        assertNotEquals(forward, reverse);
        assertNotEquals(forward.hashCode(), reverse.hashCode());
    }

    @Test
    public void testGettersAreReadOnlyViewsWithoutCopying() {
        // CQ-005: getters used to allocate a new HashSet on every call
        Set<String> left = new HashSet<>(Set.of("A"));
        FunctionalDependency fd = new FunctionalDependency(left, Set.of("B"));
        left.add("Z");

        assertEquals(Set.of("A"), fd.getLeftSide());
        assertSame(fd.getLeftSide(), fd.getLeftSide());
        assertThrows(UnsupportedOperationException.class,
            () -> fd.getRightSide().add("C"));

        CandidateKey key = new CandidateKey(Set.of("A"));
        assertSame(key.getAttributes(), key.getAttributes());
        assertThrows(UnsupportedOperationException.class,
            () -> key.getAttributes().clear());
    }
}
