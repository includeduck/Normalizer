package com.dbms.analyzer.algorithm;

import java.util.HashSet;
import java.util.Set;
import com.dbms.analyzer.model.Relation;
import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.model.DecompositionStep;

public class BcnfDecomposer {

    private ClosureComputer closureComputer;
    private NormalFormChecker normalFormChecker;

    public BcnfDecomposer() {
        this.closureComputer = new ClosureComputer();
        this.normalFormChecker = new NormalFormChecker();
    }

    /**
     * Decomposes a relation to BCNF recursively
     * @param relation The relation to decompose
     * @return Set of relations in BCNF
     */
    public Set<Relation> decomposeToBcnf(Relation relation) {
        Set<Relation> bcnfRelations = new HashSet<>();
        
        // Check for BCNF violations
        FunctionalDependency violation = findBcnfViolation(
            relation.getAttributes(), 
            relation.getFunctionalDependencies());

        if (violation == null) {
            // No violation, relation is in BCNF
            bcnfRelations.add(relation);
            return bcnfRelations;
        }

        // Decompose based on violation
        Set<String> leftSideClosure = closureComputer.computeClosure(
            violation.getLeftSide(), 
            relation.getFunctionalDependencies());

        // Create first relation from closure
        Relation r1 = new Relation(
            relation.getName() + "_1", 
            leftSideClosure);

        // Create second relation from left side + remaining attributes
        Set<String> r2Attributes = new HashSet<>(relation.getAttributes());
        r2Attributes.removeAll(leftSideClosure);
        r2Attributes.addAll(violation.getLeftSide());
        
        Relation r2 = new Relation(
            relation.getName() + "_2", 
            r2Attributes);

        // Recursively decompose both relations
        bcnfRelations.addAll(decomposeToBcnf(r1));
        bcnfRelations.addAll(decomposeToBcnf(r2));

        return bcnfRelations;
    }

    /**
     * Finds a BCNF violation if it exists
     * @return A violating FD, or null if no violation exists
     */
    private FunctionalDependency findBcnfViolation(
            Set<String> attributes,
            Set<FunctionalDependency> functionalDependencies) {

        for (FunctionalDependency fd : functionalDependencies) {
            Set<String> closure = closureComputer.computeClosure(
                fd.getLeftSide(), 
                functionalDependencies);

            if (!closure.equals(attributes) && !fd.getLeftSide().isEmpty()) {
                return fd;
            }
        }
        return null;
    }

    /**
     * Creates a decomposition step record
     */
    public DecompositionStep createDecompositionStep(
            Relation original,
            FunctionalDependency violation,
            Set<Relation> decomposed,
            int stepNumber) {
        
        DecompositionStep step = new DecompositionStep(
            original, violation, stepNumber);
        step.setDecomposedRelations(decomposed);
        return step;
    }
}
