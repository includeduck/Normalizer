package com.dbms.analyzer.algorithm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;
import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.model.CandidateKey;
import com.dbms.analyzer.model.NormalFormResult;

public class NormalFormCheckerTest {

    private NormalFormChecker normalFormChecker = 
        new NormalFormChecker();

    @Test
    public void test1NF() {
        Set<String> attributes = new HashSet<>();
        attributes.add("A");
        attributes.add("B");
        
        Set<FunctionalDependency> fds = new HashSet<>();
        
        assertTrue(normalFormChecker.is1NF(attributes, fds));
    }

    @Test
    public void testBCNFViolation() {
        Set<String> attributes = new HashSet<>();
        attributes.add("A");
        attributes.add("B");
        attributes.add("C");
        
        Set<FunctionalDependency> fds = new HashSet<>();
        fds.add(new FunctionalDependency(
            Set.of("B"), Set.of("C")));
        
        Set<CandidateKey> candidateKeys = new HashSet<>();
        candidateKeys.add(new CandidateKey(Set.of("A", "B")));
        
        // B is not a superkey, so BCNF is violated
        assertFalse(normalFormChecker.isBCNF(
            attributes, fds, candidateKeys));
    }

    @Test
    public void testBCNFSatisfied() {
        Set<String> attributes = new HashSet<>();
        attributes.add("A");
        attributes.add("B");
        
        Set<FunctionalDependency> fds = new HashSet<>();
        fds.add(new FunctionalDependency(
            Set.of("A"), Set.of("B")));
        
        Set<CandidateKey> candidateKeys = new HashSet<>();
        candidateKeys.add(new CandidateKey(Set.of("A")));
        
        // A is a superkey, so BCNF is satisfied
        assertTrue(normalFormChecker.isBCNF(
            attributes, fds, candidateKeys));
    }

    @Test
    public void test2NFViolationForPartialDependency() {
        Set<String> attributes = Set.of("A", "B", "C");

        Set<FunctionalDependency> fds = new HashSet<>();
        fds.add(new FunctionalDependency(
            Set.of("A"), Set.of("C")));

        Set<CandidateKey> candidateKeys = new HashSet<>();
        candidateKeys.add(new CandidateKey(Set.of("A", "B")));

        assertFalse(normalFormChecker.is2NF(
            attributes, fds, candidateKeys));

        var result = normalFormChecker.determineHighestNormalForm(
            attributes, fds, candidateKeys);

        assertEquals(
            NormalFormResult.NormalForm.FIRST_NF,
            result.getHighestNormalForm());
        assertFalse(result.getViolations().isEmpty());
    }

    @Test
    public void test3NFViolationForTransitiveDependency() {
        Set<String> attributes = Set.of("A", "B", "C");

        Set<FunctionalDependency> fds = new HashSet<>();
        fds.add(new FunctionalDependency(
            Set.of("A"), Set.of("B")));
        fds.add(new FunctionalDependency(
            Set.of("B"), Set.of("C")));

        Set<CandidateKey> candidateKeys = new HashSet<>();
        candidateKeys.add(new CandidateKey(Set.of("A")));

        assertTrue(normalFormChecker.is2NF(
            attributes, fds, candidateKeys));
        assertFalse(normalFormChecker.is3NF(
            attributes, fds, candidateKeys));

        var result = normalFormChecker.determineHighestNormalForm(
            attributes, fds, candidateKeys);

        assertEquals(
            NormalFormResult.NormalForm.SECOND_NF,
            result.getHighestNormalForm());
    }

    @Test
    public void test3NFSatisfiedWhileBCNFViolated() {
        Set<String> attributes = Set.of("A", "B", "C");

        Set<FunctionalDependency> fds = new HashSet<>();
        fds.add(new FunctionalDependency(
            Set.of("A"), Set.of("B")));
        fds.add(new FunctionalDependency(
            Set.of("B"), Set.of("A")));

        Set<CandidateKey> candidateKeys = new HashSet<>();
        candidateKeys.add(new CandidateKey(Set.of("A", "C")));
        candidateKeys.add(new CandidateKey(Set.of("B", "C")));

        assertTrue(normalFormChecker.is2NF(
            attributes, fds, candidateKeys));
        assertTrue(normalFormChecker.is3NF(
            attributes, fds, candidateKeys));
        assertFalse(normalFormChecker.isBCNF(
            attributes, fds, candidateKeys));

        var result = normalFormChecker.determineHighestNormalForm(
            attributes, fds, candidateKeys);

        assertEquals(
            NormalFormResult.NormalForm.THIRD_NF,
            result.getHighestNormalForm());
    }

    @Test
    public void testTrivialDependencyDoesNotViolateBCNF() {
        Set<String> attributes = Set.of("A", "B");

        Set<FunctionalDependency> fds = new HashSet<>();
        fds.add(new FunctionalDependency(
            Set.of("A"), Set.of("A")));

        Set<CandidateKey> candidateKeys = new HashSet<>();
        candidateKeys.add(new CandidateKey(Set.of("A", "B")));

        assertTrue(normalFormChecker.isBCNF(
            attributes, fds, candidateKeys));
    }
}
