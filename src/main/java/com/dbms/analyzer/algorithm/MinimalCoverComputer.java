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
        this.closureComputer = new ClosureComputer();
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

        // Step 1: Decompose RHS to singletons
        List<FunctionalDependency> decomposed = decomposeRightSides(fds);

        // Step 2: Remove extraneous LHS attributes
        List<FunctionalDependency> reduced = removeExtraneousAttributes(decomposed);

        // Step 3: Remove redundant FDs
        List<FunctionalDependency> minimal = removeRedundantFds(reduced);

        // Recombine FDs with the same LHS for cleaner output
        return recombine(minimal);
    }

    /**
     * Computes the minimal cover with step-by-step trace.
     */
    public List<String> computeMinimalCoverWithSteps(
            Set<FunctionalDependency> fds) {

        List<String> steps = new ArrayList<>();
        steps.add("=== Minimal Cover Computation ===");
        steps.add("");

        // Step 1
        List<FunctionalDependency> decomposed = decomposeRightSides(fds);
        steps.add("Step 1: Decompose RHS to singletons");
        decomposed.forEach(fd -> steps.add("  " + formatFd(fd)));
        steps.add("");

        // Step 2
        List<FunctionalDependency> reduced = removeExtraneousAttributes(decomposed);
        steps.add("Step 2: Remove extraneous LHS attributes");
        reduced.forEach(fd -> steps.add("  " + formatFd(fd)));
        steps.add("");

        // Step 3
        List<FunctionalDependency> minimal = removeRedundantFds(reduced);
        steps.add("Step 3: Remove redundant FDs");
        minimal.forEach(fd -> steps.add("  " + formatFd(fd)));
        steps.add("");

        // Recombine
        Set<FunctionalDependency> result = recombine(minimal);
        steps.add("Final minimal cover:");
        result.forEach(fd -> steps.add("  " + formatFd(fd)));

        return steps;
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

        // Use a list to collect unique LHS groups while preserving encounter order
        List<Set<String>> lhsOrder = new ArrayList<>();
        java.util.Map<Set<String>, Set<String>> map = new java.util.LinkedHashMap<>();

        for (FunctionalDependency fd : fds) {
            Set<String> key = fd.getLeftSide();
            map.computeIfAbsent(key, k -> {
                lhsOrder.add(k);
                return new TreeSet<>();
            }).addAll(fd.getRightSide());
        }

        Set<FunctionalDependency> result = new LinkedHashSet<>();
        for (Set<String> lhs : lhsOrder) {
            result.add(new FunctionalDependency(lhs, map.get(lhs)));
        }
        return result;
    }

    private String formatFd(FunctionalDependency fd) {
        return formatSet(fd.getLeftSide()) + " → " + formatSet(fd.getRightSide());
    }

    private String formatSet(Set<String> values) {
        return String.join("", new TreeSet<>(values));
    }
}
