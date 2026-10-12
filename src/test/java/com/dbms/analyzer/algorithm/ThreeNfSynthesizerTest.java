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
        r = r.withFunctionalDependency(
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
        r = r.withFunctionalDependency(
                new FunctionalDependency(Set.of("A"), Set.of("B")));
        r = r.withFunctionalDependency(
                new FunctionalDependency(Set.of("C"), Set.of("D")));

        Set<Relation> result = synthesizer.synthesize(r);
        // One relation per FD, plus the key {A,C} which neither contains
        assertEquals(Set.of(Set.of("A", "B"), Set.of("C", "D"), Set.of("A", "C")),
                schemas(result));

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
        r = r.withFunctionalDependency(
                new FunctionalDependency(Set.of("B"), Set.of("C")));

        Set<Relation> result = synthesizer.synthesize(r);
        assertEquals(Set.of(Set.of("B", "C"), Set.of("A", "B")), schemas(result));

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
    public void testWithStepsShowsEachStep() {
        Relation r = new Relation("R", Set.of("A", "B", "C"));
        r = r.withFunctionalDependency(
                new FunctionalDependency(Set.of("A"), Set.of("B")));

        List<String> steps = synthesizer.synthesizeWithSteps(r);
        assertEquals("=== 3NF Synthesis ===", steps.get(0));
        assertTrue(steps.contains("  A -> B  →  R(A,B)"), steps.toString());
        assertTrue(steps.contains("  No relation contains a candidate key. Adding relation"
                + " for key {A,C} (smallest key, alphabetically first on ties)."),
                steps.toString());
        int done = steps.indexOf("Final 3NF decomposition:");
        assertEquals(List.of("  R_3NF_1(A,B)", "    FD: A -> B", "  R_3NF_2(A,C)"),
                steps.subList(done + 1, steps.size()));
    }

    @Test
    public void testTraceDescribesExactlyTheReturnedRelations() {
        // BUG-009: the trace and the result used to come from separate copies
        Relation r = new Relation("R", Set.of("A", "B", "C", "D"), Set.of(
                new FunctionalDependency(Set.of("A"), Set.of("B")),
                new FunctionalDependency(Set.of("B"), Set.of("C"))));

        ThreeNfSynthesizer.Synthesis synthesis = synthesizer.synthesizeWithTrace(r);

        List<String> expectedFinal = new java.util.ArrayList<>();
        for (Relation rel : synthesis.relations()) {
            expectedFinal.add("  " + rel.getName() + "("
                    + String.join(",", new java.util.TreeSet<>(rel.getAttributes())) + ")");
            FdOrder.sorted(rel.getFunctionalDependencies())
                    .forEach(fd -> expectedFinal.add("    FD: " + fd));
        }
        List<String> steps = synthesis.steps();
        int start = steps.indexOf("Final 3NF decomposition:");
        assertEquals(expectedFinal, steps.subList(start + 1, steps.size()));

        assertEquals(synthesis.relations(), synthesizer.synthesize(r));
        assertEquals(steps, synthesizer.synthesizeWithSteps(r));
    }

    @Test
    public void testResultRelationsCarryProjectedFds() {
        // R(A,B,C,D), A -> B, B -> C, C -> A, A -> D: relation (A,B) gets
        // B -> A, which follows only via C.
        Relation r = new Relation("R", Set.of("A", "B", "C", "D"), Set.of(
                new FunctionalDependency(Set.of("A"), Set.of("B")),
                new FunctionalDependency(Set.of("B"), Set.of("C")),
                new FunctionalDependency(Set.of("C"), Set.of("A")),
                new FunctionalDependency(Set.of("A"), Set.of("D"))));
        ClosureComputer closureComputer = new ClosureComputer();

        for (Relation rel : synthesizer.synthesize(r)) {
            for (String attribute : rel.getAttributes()) {
                Set<String> expected = closureComputer.computeClosure(
                        Set.of(attribute), r.getFunctionalDependencies());
                expected.retainAll(rel.getAttributes());
                Set<String> actual = closureComputer.computeClosure(
                        Set.of(attribute), rel.getFunctionalDependencies());
                actual.retainAll(rel.getAttributes());
                assertEquals(expected, actual, rel + " " + rel.getFunctionalDependencies());
            }
        }
    }

    @Test
    public void testIdenticalSchemasAreMergedIntoOne() {
        // BUG-006: A -> B and B -> A both produce schema {A,B}
        Relation r = new Relation("R", Set.of("A", "B"));
        r = r.withFunctionalDependency(
                new FunctionalDependency(Set.of("A"), Set.of("B")));
        r = r.withFunctionalDependency(
                new FunctionalDependency(Set.of("B"), Set.of("A")));

        Set<Relation> result = synthesizer.synthesize(r);
        assertEquals(1, result.size());
        assertEquals(Set.of("A", "B"),
                result.iterator().next().getAttributes());

        List<String> steps = synthesizer.synthesizeWithSteps(r);
        long finalRelations = steps.stream()
                .dropWhile(s -> !s.startsWith("Final 3NF decomposition"))
                .filter(s -> s.startsWith("  R_3NF_"))
                .count();
        assertEquals(1, finalRelations);
    }

    private static Set<Set<String>> schemas(Set<Relation> relations) {
        Set<Set<String>> schemas = new java.util.HashSet<>();
        relations.forEach(rel -> schemas.add(rel.getAttributes()));
        return schemas;
    }
}
