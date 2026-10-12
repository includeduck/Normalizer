package com.dbms.analyzer.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class RelationTest {

    private static final FunctionalDependency A_TO_B =
        new FunctionalDependency(Set.of("A"), Set.of("B"));

    @Test
    public void testWithFunctionalDependencyLeavesOriginalUnchanged() {
        Relation original = new Relation("R", Set.of("A", "B"));
        Relation changed = original.withFunctionalDependency(A_TO_B);

        assertTrue(original.getFunctionalDependencies().isEmpty());
        assertEquals(Set.of(A_TO_B), changed.getFunctionalDependencies());
        assertEquals(original,
            changed.withoutFunctionalDependency(A_TO_B));
    }

    @Test
    public void testReturnedSetsAreCopies() {
        Relation relation = new Relation("R", Set.of("A", "B"), Set.of(A_TO_B));

        relation.getAttributes().add("C");
        relation.getFunctionalDependencies().clear();

        assertEquals(Set.of("A", "B"), relation.getAttributes());
        assertEquals(Set.of(A_TO_B), relation.getFunctionalDependencies());
    }

    @Test
    public void testEqualityIncludesFunctionalDependencies() {
        // BUG-013: equal schemas with different FDs used to be equal
        Relation withoutFds = new Relation("R", Set.of("A", "B"));
        Relation withFd = withoutFds.withFunctionalDependency(A_TO_B);

        assertNotEquals(withoutFds, withFd);

        Set<Relation> relations = new HashSet<>();
        relations.add(withoutFds);
        relations.add(withFd);
        assertEquals(2, relations.size());
        assertTrue(relations.contains(new Relation("R", Set.of("A", "B"), Set.of(A_TO_B))));
    }
}
