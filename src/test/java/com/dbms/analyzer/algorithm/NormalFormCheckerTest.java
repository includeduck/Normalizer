package com.dbms.analyzer.algorithm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;
import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.model.CandidateKey;

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
}
