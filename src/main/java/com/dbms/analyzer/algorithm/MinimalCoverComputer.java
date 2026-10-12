package com.dbms.analyzer.algorithm;

import com.dbms.analyzer.model.FunctionalDependency;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Computes the minimal (canonical) cover of a set of functional dependencies.
 *
 * Steps:
 * 1. Decompose all FDs so each has a single attribute on the right-hand side.
 * 2. Remove extraneous attributes from every left-hand side.
 * 3. Remove redundant FDs.
 */
public class MinimalCoverComputer {

    private final ClosureComputer closureComputer;

    public MinimalCoverComputer() {
        this(new ClosureComputer());
    }

    public MinimalCoverComputer(ClosureComputer closureComputer) {
        this.closureComputer = closureComputer;
    }

    /**
     * Computes the minimal cover for a given set of functional dependencies.
     *
     * @param fds the original set of functional dependencies
     * @return a minimal/canonical cover as a new set
     */
    public Set<FunctionalDependency> computeMinimalCover(
            Set<FunctionalDependency> fds) {
        return compute(fds, null);
    }

    /**
     * Computes the minimal cover with step-by-step trace.
     */
    public List<String> computeMinimalCoverWithSteps(
            Set<FunctionalDependency> fds) {

        List<String> steps = new ArrayList<>();
        steps.add("=== Minimal Cover Computation ===");
        steps.add("FDs are processed in a fixed order: smaller left sides first,");
        steps.add("then alphabetically. Redundant FDs are tested from the end of");
        steps.add("the list, so when several minimal covers exist, the one shown");
        steps.add("keeps the FDs that come earlier.");
        steps.add("");
        compute(fds, steps);
        return steps;
    }

    /** The single implementation; records a trace when {@code steps} is non-null. */
    private Set<FunctionalDependency> compute(
            Set<FunctionalDependency> fds, List<String> steps) {

        List<FunctionalDependency> decomposed = decomposeRightSides(fds);
        traceStep(steps, "Step 1: Decompose RHS to singletons", decomposed);

        List<FunctionalDependency> reduced = removeExtraneousAttributes(decomposed);
        traceStep(steps, "Step 2: Remove extraneous LHS attributes", reduced);

        List<FunctionalDependency> minimal = removeRedundantFds(reduced);
        traceStep(steps, "Step 3: Remove redundant FDs", minimal);

        // Recombine FDs with the same LHS for cleaner output
        Set<FunctionalDependency> result = recombine(minimal);
        if (steps != null) {
            steps.add("Final minimal cover:");
            result.forEach(fd -> steps.add("  " + formatFd(fd)));
        }
        return result;
    }

    private void traceStep(List<String> steps, String title,
                           List<FunctionalDependency> fds) {
        if (steps != null) {
            steps.add(title);
            fds.forEach(fd -> steps.add("  " + formatFd(fd)));
            steps.add("");
        }
    }

    // ---------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------

    private List<FunctionalDependency> decomposeRightSides(
            Set<FunctionalDependency> fds) {

        List<FunctionalDependency> result = new ArrayList<>();
        for (FunctionalDependency fd : fds) {
            for (String attr : fd.getRightSide()) {
                result.add(new FunctionalDependency(
                        fd.getLeftSide(), Set.of(attr)));
            }
        }
        // Later steps keep or drop FDs in list order; fix that order so the
        // cover chosen depends only on the FDs, not on hash-set iteration.
        result.sort(FdOrder.CANONICAL);
        return result;
    }

    private List<FunctionalDependency> removeExtraneousAttributes(
            List<FunctionalDependency> fds) {

        List<FunctionalDependency> result = new ArrayList<>(fds);
        Set<FunctionalDependency> allFds = new LinkedHashSet<>(fds);

        for (int i = 0; i < result.size(); i++) {
            FunctionalDependency fd = result.get(i);
            Set<String> lhs = new TreeSet<>(fd.getLeftSide());

            if (lhs.size() <= 1) {
                continue;
            }

            for (String attr : new ArrayList<>(lhs)) {
                Set<String> reduced = new TreeSet<>(lhs);
                reduced.remove(attr);

                if (reduced.isEmpty()) {
                    continue;
                }

                // Check if the reduced LHS can still determine the RHS
                // under the FULL current FD set
                Set<String> closure = closureComputer.computeClosure(
                        reduced, allFds);

                if (closure.containsAll(fd.getRightSide())) {
                    lhs.remove(attr);
                    FunctionalDependency newFd = new FunctionalDependency(
                            new HashSet<>(lhs), fd.getRightSide());
                    result.set(i, newFd);
                    // Update the allFds set to reflect the change
                    allFds = new LinkedHashSet<>(result);
                }
            }
        }

        return result;
    }


    private List<FunctionalDependency> removeRedundantFds(
            List<FunctionalDependency> fds) {

        List<FunctionalDependency> result = new ArrayList<>(fds);

        for (int i = result.size() - 1; i >= 0; i--) {
            FunctionalDependency fd = result.get(i);

            // Build FD set without this FD
            Set<FunctionalDependency> remaining = new LinkedHashSet<>();
            for (int j = 0; j < result.size(); j++) {
                if (j != i) {
                    remaining.add(result.get(j));
                }
            }

            Set<String> closure = closureComputer.computeClosure(
                    fd.getLeftSide(), remaining);

            if (closure.containsAll(fd.getRightSide())) {
                result.remove(i);
            }
        }

        return result;
    }

    /**
     * Re-combines singleton-RHS FDs that share the same LHS.
     */
    private Set<FunctionalDependency> recombine(
            List<FunctionalDependency> fds) {

        java.util.Map<Set<String>, Set<String>> map =
                new java.util.TreeMap<>(FdOrder.ATTRIBUTE_SETS);

        for (FunctionalDependency fd : fds) {
            map.computeIfAbsent(fd.getLeftSide(), k -> new TreeSet<>())
                    .addAll(fd.getRightSide());
        }

        Set<FunctionalDependency> result = new LinkedHashSet<>();
        map.forEach((lhs, rhs) -> result.add(new FunctionalDependency(lhs, rhs)));
        return result;
    }

    private String formatFd(FunctionalDependency fd) {
        return formatSet(fd.getLeftSide()) + " → " + formatSet(fd.getRightSide());
    }

    private String formatSet(Set<String> values) {
        return String.join(",", new TreeSet<>(values));
    }
}
