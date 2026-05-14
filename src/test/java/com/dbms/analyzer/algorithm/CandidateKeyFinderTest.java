package com.dbms.analyzer.algorithm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
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
        
        assertFalse(keys.isEmpty());
    }

    @Test
    public void testNoCandidateKeys() {
        Set<String> attributes = new HashSet<>();
        attributes.add("A");
        attributes.add("B");
        
        Set<FunctionalDependency> fds = new HashSet<>();
        
        Set<CandidateKey> keys = 
            candidateKeyFinder.findAllCandidateKeys(attributes, fds);
        
        // Should find all attributes as a candidate key
        assertFalse(keys.isEmpty());
    }
}
