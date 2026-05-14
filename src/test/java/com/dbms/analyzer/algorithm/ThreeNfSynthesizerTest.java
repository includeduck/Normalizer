package com.dbms.analyzer.algorithm;

import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.model.Relation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ThreeNfSynthesizerTest {

    private final ThreeNfSynthesizer synthesizer = new ThreeNfSynthesizer();

    @Test
    public void testAlreadyIn3NF() {
        Relation r = new Relation("R", Set.of("A", "B"));
        r.addFunctionalDependency(
                new FunctionalDependency(Set.of("A"), Set.of("B")));

        Set<Relation> result = synthesizer.synthesize(r);
        // Single-key, single FD — should stay as one relation
        assertEquals(1, result.size());
    }

    @Test
    public void testClassicDecomposition() {
        // R(A,B,C,D) with A -> B, C -> D
        // Should produce at least two relations
        Relation r = new Relation("R", Set.of("A", "B", "C", "D"));
        r.addFunctionalDependency(
                new FunctionalDependency(Set.of("A"), Set.of("B")));
        r.addFunctionalDependency(
                new FunctionalDependency(Set.of("C"), Set.of("D")));

        Set<Relation> result = synthesizer.synthesize(r);
        assertTrue(result.size() >= 2, "Should decompose into at least 2 relations");

        // All original attributes should be covered
        Set<String> allAttrs = new java.util.HashSet<>();
        result.forEach(rel -> allAttrs.addAll(rel.getAttributes()));
        assertTrue(allAttrs.containsAll(Set.of("A", "B", "C", "D")));
    }

    @Test
    public void testCandidateKeyRelationAdded() {
        // R(A,B,C) with B -> C
        // Candidate key is {A,B}, but the FD B -> C creates relation {B,C}
        // A candidate key relation {A,B} should be added
        Relation r = new Relation("R", Set.of("A", "B", "C"));
        r.addFunctionalDependency(
                new FunctionalDependency(Set.of("B"), Set.of("C")));

        Set<Relation> result = synthesizer.synthesize(r);
        assertTrue(result.size() >= 2);

        // Check that one relation contains a candidate key
        boolean hasKeyRelation = false;
        for (Relation rel : result) {
            if (rel.getAttributes().containsAll(Set.of("A", "B"))) {
                hasKeyRelation = true;
                break;
            }
        }
        assertTrue(hasKeyRelation, "A relation containing a candidate key should exist");
    }

    @Test
    public void testWithStepsReturnsNonEmpty() {
        Relation r = new Relation("R", Set.of("A", "B", "C"));
        r.addFunctionalDependency(
                new FunctionalDependency(Set.of("A"), Set.of("B")));

        List<String> steps = synthesizer.synthesizeWithSteps(r);
        assertFalse(steps.isEmpty());
        assertTrue(steps.get(0).contains("3NF"));
    }
}
