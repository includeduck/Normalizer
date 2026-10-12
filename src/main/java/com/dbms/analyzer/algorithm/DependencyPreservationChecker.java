package com.dbms.analyzer.algorithm;

import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.model.Relation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Checks whether a decomposition preserves all the original functional
 * dependencies.
 *
 * A decomposition {R1, R2, …, Rn} preserves a set F of FDs if the union
 * of the projections of F⁺ onto each Ri is equivalent to F. The projections
 * are never materialised; instead the standard polynomial test is used for
 * every X → Y in F:
 *
 * <pre>
 *   res = X
 *   repeat until res stops changing:
 *       for each Ri: res = res ∪ ((res ∩ Ri)⁺ ∩ Ri)     (closure under F)
 *   X → Y is preserved iff Y ⊆ res
 * </pre>
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
     * @param decomposedRelations the decomposed relations
     * @return true if all original FDs are implied by the union of the
     *         projections of F⁺ onto the decomposed relations
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

        List<Relation> relations = sortedRelations(decomposedRelations);
        List<FunctionalDependency> lost = new ArrayList<>();
        for (FunctionalDependency fd : sortedFds(originalFds)) {
            Set<String> reachable = reachableAcross(
                    fd.getLeftSide(), originalFds, relations, null);
            if (!reachable.containsAll(fd.getRightSide())) {
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
        steps.add("For each FD X -> Y: start with res = X, then repeatedly add");
        steps.add("(res ∩ Ri)+ ∩ Ri for every relation Ri until nothing changes.");
        steps.add("X -> Y is preserved iff Y ⊆ res.");
        steps.add("");

        List<Relation> relations = sortedRelations(decomposedRelations);
        boolean allPreserved = true;
        for (FunctionalDependency fd : sortedFds(originalFds)) {
            List<String> trace = new ArrayList<>();
            Set<String> reachable = reachableAcross(
                    fd.getLeftSide(), originalFds, relations, trace);
            boolean preserved = reachable.containsAll(fd.getRightSide());

            steps.add("Check " + fd + ": res = " + formatSet(fd.getLeftSide()));
            trace.forEach(line -> steps.add("  " + line));
            steps.add("  Final res = " + formatSet(reachable)
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
     * Computes the attributes reachable from {@code start} using only
     * dependencies that hold within individual relations of the decomposition.
     */
    private Set<String> reachableAcross(
            Set<String> start,
            Set<FunctionalDependency> fds,
            List<Relation> relations,
            List<String> trace) {

        Set<String> result = new HashSet<>(start);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Relation relation : relations) {
                Set<String> attributes = relation.getAttributes();
                Set<String> local = new HashSet<>(result);
                local.retainAll(attributes);
                if (local.isEmpty()) {
                    continue;
                }

                Set<String> gained = closureComputer.computeClosure(local, fds);
                gained.retainAll(attributes);
                gained.removeAll(result);
                if (!gained.isEmpty()) {
                    result.addAll(gained);
                    changed = true;
                    if (trace != null) {
                        trace.add("via " + relation.getName()
                                + ": " + formatSet(local) + "+ adds "
                                + formatSet(gained));
                    }
                }
            }
        }
        return result;
    }

    private List<Relation> sortedRelations(Set<Relation> relations) {
        List<Relation> sorted = new ArrayList<>(relations);
        sorted.sort(Comparator.comparing(Relation::getName));
        return sorted;
    }

    private List<FunctionalDependency> sortedFds(Set<FunctionalDependency> fds) {
        List<FunctionalDependency> sorted = new ArrayList<>(fds);
        sorted.sort(Comparator.comparing(FunctionalDependency::toString));
        return sorted;
    }

    private String formatSet(Set<String> values) {
        return "{" + String.join(",", new TreeSet<>(values)) + "}";
    }
}
