package com.dbms.analyzer.algorithm;

import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.model.Relation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Checks whether a decomposition preserves all the original functional
 * dependencies.
 *
 * A decomposition {R1, R2, …, Rn} preserves a set F of FDs if the union
 * of the projected FDs (F_Ri) is equivalent to F.  Equivalence is checked
 * by verifying that for every FD X → Y in F, Y is contained in the closure
 * of X under the projected FD union.
 */
public class DependencyPreservationChecker {

    private final ClosureComputer closureComputer;

    public DependencyPreservationChecker() {
        this.closureComputer = new ClosureComputer();
    }

    public DependencyPreservationChecker(ClosureComputer closureComputer) {
        this.closureComputer = closureComputer;
    }

    /**
     * Checks if a decomposition preserves all original FDs.
     *
     * @param originalFds      the original set of FDs
     * @param decomposedRelations the decomposed relations (each carrying its own FDs)
     * @return true if all original FDs are logically implied by the projected FD union
     */
    public boolean isPreserved(
            Set<FunctionalDependency> originalFds,
            Set<Relation> decomposedRelations) {

        return findLostDependencies(originalFds, decomposedRelations).isEmpty();
    }

    /**
     * Returns the list of original FDs that are NOT preserved by the decomposition.
     */
    public List<FunctionalDependency> findLostDependencies(
            Set<FunctionalDependency> originalFds,
            Set<Relation> decomposedRelations) {

        // Collect all projected FDs from decomposed relations
        Set<FunctionalDependency> projectedFds = new LinkedHashSet<>();
        for (Relation r : decomposedRelations) {
            projectedFds.addAll(
                    projectFds(originalFds, r.getAttributes()));
        }

        List<FunctionalDependency> lost = new ArrayList<>();
        for (FunctionalDependency fd : originalFds) {
            Set<String> closure = closureComputer.computeClosure(
                    fd.getLeftSide(), projectedFds);
            if (!closure.containsAll(fd.getRightSide())) {
                lost.add(fd);
            }
        }
        return lost;
    }

    /**
     * Checks preservation and produces a step-by-step explanation.
     */
    public List<String> checkWithSteps(
            Set<FunctionalDependency> originalFds,
            Set<Relation> decomposedRelations) {

        List<String> steps = new ArrayList<>();
        steps.add("=== Dependency Preservation Check ===");
        steps.add("");

        // Collect projected FDs
        Set<FunctionalDependency> projectedFds = new LinkedHashSet<>();
        for (Relation r : decomposedRelations) {
            Set<FunctionalDependency> projected =
                    projectFds(originalFds, r.getAttributes());
            steps.add("Projected FDs on " + r.getName()
                    + "(" + String.join(",", new TreeSet<>(r.getAttributes())) + "):");
            if (projected.isEmpty()) {
                steps.add("  (none)");
            } else {
                projected.forEach(fd -> steps.add("  " + fd));
            }
            projectedFds.addAll(projected);
        }
        steps.add("");

        // Check each original FD
        boolean allPreserved = true;
        for (FunctionalDependency fd : originalFds) {
            Set<String> closure = closureComputer.computeClosure(
                    fd.getLeftSide(), projectedFds);
            boolean preserved = closure.containsAll(fd.getRightSide());
            steps.add("Check " + fd + ": closure of "
                    + formatSet(fd.getLeftSide()) + " = "
                    + formatSet(closure)
                    + " → " + (preserved ? "PRESERVED" : "LOST"));
            if (!preserved) allPreserved = false;
        }
        steps.add("");

        steps.add(allPreserved
                ? "Result: All dependencies are preserved."
                : "Result: Some dependencies are NOT preserved.");

        return steps;
    }

    /**
     * Projects a set of FDs onto a subset of attributes.
     * An FD X → Y is included in the projection when both X and Y
     * are subsets of the given attribute set.
     */
    public Set<FunctionalDependency> projectFds(
            Set<FunctionalDependency> fds,
            Set<String> attributes) {

        Set<FunctionalDependency> result = new LinkedHashSet<>();
        for (FunctionalDependency fd : fds) {
            if (attributes.containsAll(fd.getLeftSide())
                    && attributes.containsAll(fd.getRightSide())) {
                result.add(fd);
            }
        }
        return result;
    }

    private String formatSet(Set<String> values) {
        return "{" + String.join(",", new TreeSet<>(values)) + "}";
    }
}
