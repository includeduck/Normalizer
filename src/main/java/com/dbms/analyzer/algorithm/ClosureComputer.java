package com.dbms.analyzer.algorithm;

import java.util.HashSet;
import java.util.Set;
import com.dbms.analyzer.model.FunctionalDependency;

public class ClosureComputer {

    /**
     * Computes attribute closure for a given set of attributes
     * @param attributeSet The initial set of attributes
     * @param functionalDependencies The set of functional dependencies
     * @return The closure of the attribute set
     */
    public Set<String> computeClosure(Set<String> attributeSet, 
                                     Set<FunctionalDependency> functionalDependencies) {
        Set<String> closure = new HashSet<>(attributeSet);
        boolean changed = true;

        while (changed) {
            changed = false;
            for (FunctionalDependency fd : functionalDependencies) {
                if (closure.containsAll(fd.getLeftSide()) && 
                    !closure.containsAll(fd.getRightSide())) {
                    closure.addAll(fd.getRightSide());
                    changed = true;
                }
            }
        }

        return closure;
    }

    /**
     * Computes attribute closure with step-by-step trace
     * @param attributeSet The initial set of attributes
     * @param functionalDependencies The set of functional dependencies
     * @return A list of closure steps
     */
    public java.util.List<String> computeClosureWithSteps(
            Set<String> attributeSet,
            Set<FunctionalDependency> functionalDependencies) {
        java.util.List<String> steps = new java.util.ArrayList<>();
        Set<String> closure = new HashSet<>(attributeSet);
        steps.add("Initial closure: " + closure);
        
        boolean changed = true;
        int iteration = 0;

        while (changed) {
            changed = false;
            iteration++;
            for (FunctionalDependency fd : functionalDependencies) {
                if (closure.containsAll(fd.getLeftSide()) && 
                    !closure.containsAll(fd.getRightSide())) {
                    steps.add("Iteration " + iteration + ": Apply FD " + fd + 
                             " -> Add " + fd.getRightSide());
                    closure.addAll(fd.getRightSide());
                    changed = true;
                }
            }
        }
        
        steps.add("Final closure: " + closure);
        return steps;
    }
}
