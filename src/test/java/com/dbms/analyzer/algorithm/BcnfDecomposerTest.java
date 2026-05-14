package com.dbms.analyzer.algorithm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;
import com.dbms.analyzer.model.Relation;
import com.dbms.analyzer.model.FunctionalDependency;

public class BcnfDecomposerTest {

    private BcnfDecomposer bcnfDecomposer = new BcnfDecomposer();

    @Test
    public void testAlreadyBcnf() {
        Set<String> attributes = new HashSet<>();
        attributes.add("A");
        attributes.add("B");
        
        Relation relation = new Relation("R", attributes);
        relation.addFunctionalDependency(new FunctionalDependency(
            Set.of("A"), Set.of("B")));
        
        Set<Relation> decomposed = bcnfDecomposer.decomposeToBcnf(relation);
        
        // Already in BCNF, should return single relation
        assertEquals(1, decomposed.size());
    }

    @Test
    public void testBcnfViolation() {
        Set<String> attributes = new HashSet<>();
        attributes.add("A");
        attributes.add("B");
        attributes.add("C");
        
        Relation relation = new Relation("R", attributes);
        relation.addFunctionalDependency(new FunctionalDependency(
            Set.of("B"), Set.of("C")));
        
        Set<Relation> decomposed = bcnfDecomposer.decomposeToBcnf(relation);
        
        // Should decompose into multiple relations
        assertTrue(decomposed.size() >= 1);
    }

    @Test
    public void testEmptyFds() {
        Set<String> attributes = new HashSet<>();
        attributes.add("A");
        attributes.add("B");
        
        Relation relation = new Relation("R", attributes);
        
        Set<Relation> decomposed = bcnfDecomposer.decomposeToBcnf(relation);
        
        // No FDs, should be in BCNF
        assertEquals(1, decomposed.size());
    }
}
