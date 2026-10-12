package com.dbms.analyzer.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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
        return compute(attributeSet, functionalDependencies, null);
    }

    /**
     * Computes attribute closure with step-by-step trace
     * @param attributeSet The initial set of attributes
     * @param functionalDependencies The set of functional dependencies
     * @return A list of closure steps, ending with the final closure
     */
    public List<String> computeClosureWithSteps(
            Set<String> attributeSet,
            Set<FunctionalDependency> functionalDependencies) {
        List<String> steps = new ArrayList<>();
        compute(attributeSet, functionalDependencies, steps);
        return steps;
    }

    /**
     * Fixed-point closure. When {@code steps} is non-null, FDs are applied
     * in canonical order and every application is recorded.
     */
    private Set<String> compute(Set<String> attributeSet,
                                Set<FunctionalDependency> functionalDependencies,
                                List<String> steps) {
        Set<String> closure = new HashSet<>(attributeSet);
        Collection<FunctionalDependency> fds = steps == null
            ? functionalDependencies
            : FdOrder.sorted(functionalDependencies);
        if (steps != null) {
            steps.add("Initial closure: " + formatSet(closure));
        }

        boolean changed = true;
        int iteration = 0;
        while (changed) {
            changed = false;
            iteration++;
            for (FunctionalDependency fd : fds) {
                if (closure.containsAll(fd.getLeftSide())
                        && !closure.containsAll(fd.getRightSide())) {
                    if (steps != null) {
                        steps.add("Iteration " + iteration + ": Apply FD " + fd
                            + " -> Add " + formatSet(fd.getRightSide()));
                    }
                    closure.addAll(fd.getRightSide());
                    changed = true;
                }
            }
        }

        if (steps != null) {
            steps.add("Final closure: " + formatSet(closure));
        }
        return closure;
    }

    private String formatSet(Set<String> values) {
        return "{" + String.join(", ", new TreeSet<>(values)) + "}";
    }
}
