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
 * Verifies that a decomposition is lossless-join (non-additive)
 * using the chase-based tabloid algorithm.
 *
 * For a binary decomposition {R1, R2}, the shortcut applies:
 *   The decomposition is lossless iff
 *       (R1 ∩ R2) → (R1 − R2)  or  (R1 ∩ R2) → (R2 − R1)
 *   holds under the FD set F (i.e. R1 ∩ R2 is a superkey for R1 or R2).
 *
 * For n-way decompositions, the general chase algorithm is used.
 *
 * Each check has a single implementation; a trace is recorded only when
 * a step list is passed in.
 */
public class LosslessJoinChecker {

    private final ClosureComputer closureComputer;

    public LosslessJoinChecker() {
        this(new ClosureComputer());
    }

    public LosslessJoinChecker(ClosureComputer closureComputer) {
        this.closureComputer = closureComputer;
    }

    /**
     * Checks if a decomposition is lossless-join.
     *
     * @param originalAttributes all attributes of the original relation
     * @param fds                the original FDs
     * @param decomposedRelations the decomposed relations
     * @return true if the decomposition is lossless-join
     */
    public boolean isLossless(
            Set<String> originalAttributes,
            Set<FunctionalDependency> fds,
            Set<Relation> decomposedRelations) {
        return check(originalAttributes, fds, decomposedRelations, null);
    }

    /**
     * Checks lossless-join and produces step-by-step explanation.
     */
    public List<String> checkWithSteps(
            Set<String> originalAttributes,
            Set<FunctionalDependency> fds,
            Set<Relation> decomposedRelations) {

        List<String> steps = new ArrayList<>();
        steps.add("=== Lossless Join Check ===");
        steps.add("");
        check(originalAttributes, fds, decomposedRelations, steps);
        return steps;
    }

    private boolean check(
            Set<String> originalAttributes,
            Set<FunctionalDependency> fds,
            Set<Relation> decomposedRelations,
            List<String> steps) {

        List<Relation> relations = new ArrayList<>(decomposedRelations);
        relations.sort(Comparator.comparing(Relation::getName));

        if (relations.size() == 2) {
            return binaryCheck(relations.get(0), relations.get(1), fds, steps);
        }
        return chase(originalAttributes, fds, relations, steps);
    }

    // ---------------------------------------------------------------
    // Binary decomposition shortcut
    // ---------------------------------------------------------------

    private boolean binaryCheck(Relation r1, Relation r2,
                                Set<FunctionalDependency> fds,
                                List<String> steps) {
        Set<String> attrs1 = r1.getAttributes();
        Set<String> attrs2 = r2.getAttributes();
        trace(steps, "Binary decomposition detected — using shortcut.");
        trace(steps, "R1 = " + formatSet(attrs1));
        trace(steps, "R2 = " + formatSet(attrs2));
        trace(steps, "");

        Set<String> intersection = new HashSet<>(attrs1);
        intersection.retainAll(attrs2);
        trace(steps, "R1 ∩ R2 = " + formatSet(intersection));

        if (intersection.isEmpty()) {
            trace(steps, "Intersection is empty → decomposition is LOSSY.");
            return false;
        }

        Set<String> closure = closureComputer.computeClosure(intersection, fds);
        trace(steps, "Closure of " + formatSet(intersection) + " = "
                + formatSet(closure));
        trace(steps, "");

        boolean determinesR1 = closure.containsAll(attrs1);
        boolean determinesR2 = closure.containsAll(attrs2);
        if (determinesR1) {
            trace(steps, formatSet(intersection) + " → "
                    + formatSet(attrs1) + " holds (superkey of R1).");
        }
        if (determinesR2) {
            trace(steps, formatSet(intersection) + " → "
                    + formatSet(attrs2) + " holds (superkey of R2).");
        }

        boolean lossless = determinesR1 || determinesR2;
        trace(steps, "");
        trace(steps, lossless
                ? "Result: Decomposition is LOSSLESS."
                : "Result: Decomposition is LOSSY.");
        return lossless;
    }

    // ---------------------------------------------------------------
    // General chase algorithm for n-way decomposition
    // ---------------------------------------------------------------

    private boolean chase(
            Set<String> originalAttributes,
            Set<FunctionalDependency> fds,
            List<Relation> relations,
            List<String> steps) {

        List<String> attrList = new ArrayList<>(new TreeSet<>(originalAttributes));
        List<FunctionalDependency> orderedFds = FdOrder.sorted(fds);
        int rows = relations.size();
        int cols = attrList.size();

        trace(steps, "Using chase algorithm for " + rows + "-way decomposition.");
        trace(steps, "Attributes: " + attrList);
        trace(steps, "");

        // table[i][j] = "a_j" if attribute j is in Ri, else "b_ij"
        String[][] table = initTable(attrList, relations);
        if (steps != null) {
            steps.add("Initial tabloid:");
            appendTable(steps, table, attrList, rows, cols);
        }

        boolean changed = true;
        int iteration = 0;
        while (changed) {
            changed = false;
            iteration++;
            for (FunctionalDependency fd : orderedFds) {
                if (applyFd(table, attrList, fd, rows)) {
                    changed = true;
                    trace(steps, "Iteration " + iteration + ": Applied " + fd);
                }
            }
        }

        boolean lossless = hasDistinguishedRow(table, cols, rows);
        if (steps != null) {
            steps.add("");
            steps.add("Final tabloid:");
            appendTable(steps, table, attrList, rows, cols);
            steps.add(lossless
                    ? "Result: Found a row with all distinguished symbols → LOSSLESS."
                    : "Result: No distinguished row found → LOSSY.");
        }
        return lossless;
    }

    private String[][] initTable(List<String> attrList,
                                 List<Relation> relations) {
        int rows = relations.size();
        int cols = attrList.size();
        String[][] table = new String[rows][cols];

        for (int i = 0; i < rows; i++) {
            Set<String> rAttrs = relations.get(i).getAttributes();
            for (int j = 0; j < cols; j++) {
                if (rAttrs.contains(attrList.get(j))) {
                    table[i][j] = "a" + j;
                } else {
                    table[i][j] = "b" + i + j;
                }
            }
        }
        return table;
    }

    private boolean applyFd(String[][] table, List<String> attrList,
                            FunctionalDependency fd, int rows) {
        boolean changed = false;

        // Find column indices for LHS and RHS
        List<Integer> lhsCols = new ArrayList<>();
        for (String attr : fd.getLeftSide()) {
            int idx = attrList.indexOf(attr);
            if (idx >= 0) lhsCols.add(idx);
        }

        List<Integer> rhsCols = new ArrayList<>();
        for (String attr : fd.getRightSide()) {
            int idx = attrList.indexOf(attr);
            if (idx >= 0) rhsCols.add(idx);
        }

        // Compare all row pairs
        for (int i = 0; i < rows; i++) {
            for (int j = i + 1; j < rows; j++) {
                // Check if rows i and j agree on all LHS columns
                boolean agree = true;
                for (int col : lhsCols) {
                    if (!table[i][col].equals(table[j][col])) {
                        agree = false;
                        break;
                    }
                }
                if (!agree) continue;

                // Equate RHS columns, preferring distinguished "a" symbols
                for (int col : rhsCols) {
                    if (!table[i][col].equals(table[j][col])) {
                        if (table[j][col].startsWith("a")) {
                            table[i][col] = table[j][col];
                        } else {
                            table[j][col] = table[i][col];
                        }
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    private boolean hasDistinguishedRow(String[][] table, int cols, int rows) {
        for (int i = 0; i < rows; i++) {
            boolean allA = true;
            for (int j = 0; j < cols; j++) {
                if (!table[i][j].startsWith("a")) {
                    allA = false;
                    break;
                }
            }
            if (allA) return true;
        }
        return false;
    }

    private void appendTable(List<String> steps, String[][] table,
                             List<String> attrList, int rows, int cols) {
        StringBuilder header = new StringBuilder("  ");
        for (String attr : attrList) {
            header.append(String.format("%-8s", attr));
        }
        steps.add(header.toString());

        for (int i = 0; i < rows; i++) {
            StringBuilder row = new StringBuilder("  ");
            for (int j = 0; j < cols; j++) {
                row.append(String.format("%-8s", table[i][j]));
            }
            steps.add(row.toString());
        }
    }

    private static void trace(List<String> steps, String line) {
        if (steps != null) {
            steps.add(line);
        }
    }

    private String formatSet(Set<String> values) {
        return "{" + String.join(",", new TreeSet<>(values)) + "}";
    }
}
