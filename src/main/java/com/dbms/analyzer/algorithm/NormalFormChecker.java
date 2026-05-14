package com.dbms.analyzer.algorithm;

import java.util.HashSet;
import java.util.Set;
import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.model.CandidateKey;
import com.dbms.analyzer.model.NormalFormResult;

public class NormalFormChecker {

    private ClosureComputer closureComputer;

    public NormalFormChecker() {
        this.closureComputer = new ClosureComputer();
    }

    /**
     * Checks if relation satisfies 1NF
     * Simplified: Always true for our purposes (atomic attributes)
     */
    public boolean is1NF(Set<String> attributes, 
                        Set<FunctionalDependency> functionalDependencies) {
        return true;
    }

    /**
     * Checks if relation satisfies 2NF
     * No non-prime attribute is partially dependent on a candidate key
     */
    public boolean is2NF(Set<String> attributes,
                        Set<FunctionalDependency> functionalDependencies,
                        Set<CandidateKey> candidateKeys) {
        // TODO: Implement 2NF check
        return true;
    }

    /**
     * Checks if relation satisfies 3NF
     * Every non-prime attribute is fully functionally dependent on candidate keys
     * and there are no transitive dependencies
     */
    public boolean is3NF(Set<String> attributes,
                        Set<FunctionalDependency> functionalDependencies,
                        Set<CandidateKey> candidateKeys) {
        // TODO: Implement 3NF check
        return true;
    }

    /**
     * Checks if relation satisfies BCNF
     * For every FD X -> Y, X must be a superkey
     */
    public boolean isBCNF(Set<String> attributes,
                         Set<FunctionalDependency> functionalDependencies,
                         Set<CandidateKey> candidateKeys) {
        
        for (FunctionalDependency fd : functionalDependencies) {
            // Check if left side is a superkey
            Set<String> closure = closureComputer.computeClosure(
                fd.getLeftSide(), functionalDependencies);
            
            if (!closure.equals(attributes)) {
                return false; // Left side is not a superkey
            }
        }
        return true;
    }

    /**
     * Determines the highest normal form satisfied
     */
    public NormalFormResult determineHighestNormalForm(
            Set<String> attributes,
            Set<FunctionalDependency> functionalDependencies,
            Set<CandidateKey> candidateKeys) {
        
        Set<String> violations = new HashSet<>();
        String explanation = "";

        if (!isBCNF(attributes, functionalDependencies, candidateKeys)) {
            violations.add("BCNF violation: Some FD's left side is not a superkey");
            explanation = "BCNF Violation Detected";
            return new NormalFormResult(
                NormalFormResult.NormalForm.THIRD_NF, violations, explanation);
        }

        if (!is3NF(attributes, functionalDependencies, candidateKeys)) {
            violations.add("3NF violation: Transitive dependency detected");
            explanation = "3NF Violation Detected";
            return new NormalFormResult(
                NormalFormResult.NormalForm.SECOND_NF, violations, explanation);
        }

        if (!is2NF(attributes, functionalDependencies, candidateKeys)) {
            violations.add("2NF violation: Partial dependency detected");
            explanation = "2NF Violation Detected";
            return new NormalFormResult(
                NormalFormResult.NormalForm.FIRST_NF, violations, explanation);
        }

        return new NormalFormResult(
            NormalFormResult.NormalForm.BCNF, new HashSet<>(), 
            "Relation satisfies BCNF");
    }
}
