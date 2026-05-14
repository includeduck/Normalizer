package com.dbms.analyzer.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import com.dbms.analyzer.model.Relation;
import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.model.DecompositionStep;

/**
 * Decomposes a relation into BCNF using the standard recursive algorithm.
 *
 * Improvements over the original implementation:
 * - Trivial FDs are ignored when searching for violations.
 * - The exact violating determinant is reported.
 * - FDs are properly projected onto each sub-relation during decomposition.
 * - Decomposition steps are recorded for educational display.
 */
public class BcnfDecomposer {

    private final ClosureComputer closureComputer;
    private final NormalFormChecker normalFormChecker;

    public BcnfDecomposer() {
        this.closureComputer = new ClosureComputer();
        this.normalFormChecker = new NormalFormChecker();
    }

    /**
     * Decomposes a relation to BCNF recursively.
     *
     * @param relation The relation to decompose
     * @return Set of relations in BCNF
     */
    public Set<Relation> decomposeToBcnf(Relation relation) {
        List<DecompositionStep> stepLog = new ArrayList<>();
        return decomposeToBcnf(relation, stepLog, 1);
    }

    /**
     * Decomposes and also collects the step-by-step decomposition log.
     *
     * @param relation The relation to decompose
     * @param stepLog  Mutable list that will be filled with each decomposition step
     * @return Set of relations in BCNF
     */
    public Set<Relation> decomposeToBcnfWithSteps(
            Relation relation, List<DecompositionStep> stepLog) {
        return decomposeToBcnf(relation, stepLog, 1);
    }

    /**
     * Produces a readable step-by-step trace.
     */
    public List<String> decomposeWithExplanation(Relation relation) {
        List<String> explanation = new ArrayList<>();
        List<DecompositionStep> stepLog = new ArrayList<>();

        explanation.add("=== BCNF Decomposition ===");
        explanation.add("Original relation: " + formatRelation(relation));
        explanation.add("Original FDs:");
        for (FunctionalDependency fd : relation.getFunctionalDependencies()) {
            explanation.add("  " + fd);
        }
        explanation.add("");

        Set<Relation> result = decomposeToBcnf(relation, stepLog, 1);

        for (DecompositionStep step : stepLog) {
            explanation.add("Step " + step.getStepNumber() + ":");
            explanation.add("  Decompose: "
                    + formatRelation(step.getOriginalRelation()));
            explanation.add("  Violating FD: " + step.getViolatingDependency());
            explanation.add("  Into:");
            for (Relation r : step.getDecomposedRelations()) {
                explanation.add("    " + formatRelation(r));
            }
            explanation.add("");
        }

        explanation.add("Final BCNF relations:");
        for (Relation r : result) {
            explanation.add("  " + formatRelation(r));
            Set<FunctionalDependency> fds = r.getFunctionalDependencies();
            if (!fds.isEmpty()) {
                fds.forEach(fd -> explanation.add("    FD: " + fd));
            }
        }

        return explanation;
    }

    // ---------------------------------------------------------------
    // Core recursive decomposition
    // ---------------------------------------------------------------

    private Set<Relation> decomposeToBcnf(
            Relation relation,
            List<DecompositionStep> stepLog,
            int stepNumber) {

        Set<Relation> bcnfRelations = new LinkedHashSet<>();
        Set<String> attributes = relation.getAttributes();
        Set<FunctionalDependency> fds = relation.getFunctionalDependencies();

        // Find a BCNF violation (ignoring trivial FDs)
        FunctionalDependency violation = findBcnfViolation(attributes, fds);

        if (violation == null) {
            // No violation — relation is already in BCNF
            bcnfRelations.add(relation);
            return bcnfRelations;
        }

        // Compute closure of the violating determinant
        Set<String> leftClosure = closureComputer.computeClosure(
                violation.getLeftSide(), fds);

        // R1 = attributes in the closure (X⁺)
        Set<String> r1Attrs = new TreeSet<>(leftClosure);
        r1Attrs.retainAll(attributes); // restrict to current schema

        Relation r1 = new Relation(
                relation.getName() + "_1", r1Attrs);

        // R2 = X ∪ (R − X⁺)
        Set<String> r2Attrs = new TreeSet<>(attributes);
        r2Attrs.removeAll(leftClosure);
        r2Attrs.addAll(violation.getLeftSide());

        Relation r2 = new Relation(
                relation.getName() + "_2", r2Attrs);

        // Project FDs onto each sub-relation
        projectFdsOnto(r1, fds);
        projectFdsOnto(r2, fds);

        // Record the step
        Set<Relation> stepRelations = new LinkedHashSet<>();
        stepRelations.add(r1);
        stepRelations.add(r2);

        DecompositionStep step = new DecompositionStep(
                relation, violation, stepNumber);
        step.setDecomposedRelations(stepRelations);
        stepLog.add(step);

        // Recurse
        bcnfRelations.addAll(
                decomposeToBcnf(r1, stepLog, stepNumber + 1));
        bcnfRelations.addAll(
                decomposeToBcnf(r2, stepLog, stepNumber + stepLog.size()));

        return bcnfRelations;
    }

    // ---------------------------------------------------------------
    // Violation finding
    // ---------------------------------------------------------------

    /**
     * Finds a BCNF violation if one exists, ignoring trivial FDs.
     *
     * @return A violating FD whose LHS is not a superkey, or null
     */
    private FunctionalDependency findBcnfViolation(
            Set<String> attributes,
            Set<FunctionalDependency> functionalDependencies) {

        for (FunctionalDependency fd : functionalDependencies) {
            // Skip trivial FDs (LHS contains all of RHS)
            if (isTrivial(fd)) {
                continue;
            }

            Set<String> closure = closureComputer.computeClosure(
                    fd.getLeftSide(), functionalDependencies);

            // The LHS is a superkey if its closure contains all attributes
            if (!closure.containsAll(attributes)) {
                return fd;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------
    // FD projection
    // ---------------------------------------------------------------

    /**
     * Projects a set of FDs onto a relation's attribute set.
     * Only FDs whose LHS and RHS are entirely within the relation are kept.
     */
    private void projectFdsOnto(Relation relation,
                                Set<FunctionalDependency> originalFds) {
        Set<String> attrs = relation.getAttributes();
        for (FunctionalDependency fd : originalFds) {
            if (attrs.containsAll(fd.getLeftSide())
                    && attrs.containsAll(fd.getRightSide())) {
                relation.addFunctionalDependency(fd);
            }
        }
    }

    private boolean isTrivial(FunctionalDependency fd) {
        return fd.getLeftSide().containsAll(fd.getRightSide());
    }

    // ---------------------------------------------------------------
    // Formatting helpers
    // ---------------------------------------------------------------

    private String formatRelation(Relation r) {
        return r.getName() + "("
                + String.join(",", new TreeSet<>(r.getAttributes()))
                + ")";
    }

    /**
     * Creates a decomposition step record (public API kept for backward
     * compatibility with existing callers).
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
