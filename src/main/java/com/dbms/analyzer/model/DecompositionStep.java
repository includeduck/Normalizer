package com.dbms.analyzer.model;

import java.util.HashSet;
import java.util.Set;

public class DecompositionStep {
    private Relation originalRelation;
    private Set<Relation> decomposedRelations;
    private FunctionalDependency violatingDependency;
    private int stepNumber;

    public DecompositionStep(Relation originalRelation, 
                           FunctionalDependency violatingDependency,
                           int stepNumber) {
        this.originalRelation = originalRelation;
        this.violatingDependency = violatingDependency;
        this.stepNumber = stepNumber;
        this.decomposedRelations = new HashSet<>();
    }

    public Relation getOriginalRelation() {
        return originalRelation;
    }

    public Set<Relation> getDecomposedRelations() {
        return new HashSet<>(decomposedRelations);
    }

    public void setDecomposedRelations(Set<Relation> relations) {
        this.decomposedRelations = new HashSet<>(relations);
    }

    public FunctionalDependency getViolatingDependency() {
        return violatingDependency;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    @Override
    public String toString() {
        return "Step " + stepNumber + ": Decompose " + originalRelation.getName();
    }
}
