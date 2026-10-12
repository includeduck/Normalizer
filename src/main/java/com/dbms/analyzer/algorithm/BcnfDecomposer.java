package com.dbms.analyzer.algorithm;

import java.util.ArrayList;
import java.util.Comparator;
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
 * All closures are taken under the ORIGINAL FD set F, never under FDs
 * copied onto sub-relations: a sub-relation Ri can be subject to FDs that
 * only follow from F⁺ through attributes outside Ri.
 *
 * <ul>
 *   <li>For the original relation, checking the given FDs is sufficient.</li>
 *   <li>For a sub-relation Ri, Ri violates BCNF iff some X ⊆ Ri has
 *       X⁺ ∩ Ri ⊋ X and X⁺ ∩ Ri ≠ Ri. Given FDs inside Ri are tried first,
 *       then attribute subsets smallest first.</li>
 *   <li>A violation X splits Ri into X⁺ ∩ Ri and X ∪ (Ri − X⁺).</li>
 * </ul>
 * Each final relation carries a minimal cover of the projection of F⁺.
 */
public class BcnfDecomposer {

    private final MinimalCoverComputer minimalCoverComputer;

    public BcnfDecomposer() {
        this.minimalCoverComputer = new MinimalCoverComputer();
    }

    /**
     * Decomposes a relation to BCNF recursively.
     *
     * @param relation The relation to decompose
     * @return Set of relations in BCNF
     */
    public Set<Relation> decomposeToBcnf(Relation relation) {
        return decomposeToBcnf(relation, new ArrayList<>());
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
        return decomposeToBcnf(relation, stepLog);
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
        for (FunctionalDependency fd : sortedFds(relation.getFunctionalDependencies())) {
            explanation.add("  " + fd);
        }
        explanation.add("");

        Set<Relation> result = decomposeToBcnf(relation, stepLog);

        for (DecompositionStep step : stepLog) {
            FunctionalDependency violation = step.getViolatingDependency();
            explanation.add("Step " + step.getStepNumber() + ":");
            explanation.add("  Decompose: "
                    + formatRelation(step.getOriginalRelation()));
            explanation.add("  Violating FD: " + violation
                    + "  (" + formatSet(violation.getLeftSide())
                    + " is not a superkey of "
                    + step.getOriginalRelation().getName() + ")");
            explanation.add("  Into:");
            for (Relation r : step.getDecomposedRelations()) {
                explanation.add("    " + formatRelation(r));
            }
            explanation.add("");
        }

        explanation.add("Final BCNF relations:");
        for (Relation r : result) {
            explanation.add("  " + formatRelation(r));
            for (FunctionalDependency fd : sortedFds(r.getFunctionalDependencies())) {
                explanation.add("    FD: " + fd);
            }
        }

        return explanation;
    }

    // ---------------------------------------------------------------
    // Core recursive decomposition
    // ---------------------------------------------------------------

    private Set<Relation> decomposeToBcnf(
            Relation relation, List<DecompositionStep> stepLog) {

        Set<String> attributes = relation.getAttributes();
        AnalysisLimits.requireWithinLimit(attributes.size());

        List<FunctionalDependency> fds =
                sortedFds(relation.getFunctionalDependencies());
        FdBitIndex index = new FdBitIndex(attributes, fds);

        Set<Relation> bcnfRelations = new LinkedHashSet<>();
        decompose(relation.getName(), index.mask(attributes), true,
                index, stepLog, bcnfRelations);
        return bcnfRelations;
    }

    private void decompose(
            String name,
            long schema,
            boolean isOriginal,
            FdBitIndex index,
            List<DecompositionStep> stepLog,
            Set<Relation> bcnfRelations) {

        long determinant = findViolatingDeterminant(schema, isOriginal, index);

        if (determinant == NO_VIOLATION) {
            Relation result = new Relation(name, index.toSet(schema));
            result.setFunctionalDependencies(projectFds(schema, index));
            bcnfRelations.add(result);
            return;
        }

        long closure = index.closure(determinant) & schema;
        long r1 = closure;                                // X⁺ ∩ Ri
        long r2 = determinant | (schema & ~closure);      // X ∪ (Ri − X⁺)

        Relation current = new Relation(name, index.toSet(schema));
        Set<Relation> parts = new LinkedHashSet<>();
        parts.add(new Relation(name + "_1", index.toSet(r1)));
        parts.add(new Relation(name + "_2", index.toSet(r2)));

        FunctionalDependency violation = new FunctionalDependency(
                index.toSet(determinant),
                index.toSet(closure & ~determinant));
        DecompositionStep step = new DecompositionStep(
                current, violation, stepLog.size() + 1);
        step.setDecomposedRelations(parts);
        stepLog.add(step);

        decompose(name + "_1", r1, false, index, stepLog, bcnfRelations);
        decompose(name + "_2", r2, false, index, stepLog, bcnfRelations);
    }

    // ---------------------------------------------------------------
    // Violation finding
    // ---------------------------------------------------------------

    private static final long NO_VIOLATION = -1L;

    /**
     * Returns a determinant X ⊆ schema with X⁺ ∩ schema ⊋ X and
     * X⁺ ∩ schema ≠ schema, or {@link #NO_VIOLATION}.
     */
    private long findViolatingDeterminant(
            long schema, boolean isOriginal, FdBitIndex index) {

        // Given FDs first: their determinants are the most natural to show.
        for (int i = 0; i < index.fdCount(); i++) {
            long lhs = index.lhs(i);
            if (FdBitIndex.isSubset(lhs, schema)
                    && violates(lhs, schema, index)) {
                return lhs;
            }
        }

        // On the original relation, checking the given FDs is sufficient.
        if (isOriginal) {
            return NO_VIOLATION;
        }

        List<Integer> bits = bitsOf(schema);
        int n = bits.size();
        for (int size = 1; size < n; size++) {
            long combination = (1L << size) - 1;
            while (combination < (1L << n)) {
                long candidate = expand(combination, bits);
                if (violates(candidate, schema, index)) {
                    return candidate;
                }
                combination = nextCombination(combination);
            }
        }
        return NO_VIOLATION;
    }

    private static boolean violates(long determinant, long schema, FdBitIndex index) {
        long closure = index.closure(determinant) & schema;
        return (closure & ~determinant) != 0 && closure != schema;
    }

    // ---------------------------------------------------------------
    // FD projection
    // ---------------------------------------------------------------

    /**
     * Projects F⁺ onto a schema and returns a minimal cover of the result.
     *
     * Subsets are visited smallest first; X → A is kept only if no smaller
     * determinant of A has been found inside X, so only minimal
     * determinants are passed on to the minimal-cover step.
     */
    private Set<FunctionalDependency> projectFds(long schema, FdBitIndex index) {
        List<Integer> bits = bitsOf(schema);
        int n = bits.size();
        List<long[]> minimalDeterminants = new ArrayList<>(); // {determinant, attribute}
        Set<FunctionalDependency> projected = new LinkedHashSet<>();

        for (int size = 1; size < n; size++) {
            long combination = (1L << size) - 1;
            while (combination < (1L << n)) {
                long candidate = expand(combination, bits);
                long gained = index.closure(candidate) & schema & ~candidate;

                for (int bit : bits) {
                    long attribute = 1L << bit;
                    if ((gained & attribute) != 0
                            && !hasSmallerDeterminant(
                                candidate, attribute, minimalDeterminants)) {
                        minimalDeterminants.add(new long[] {candidate, attribute});
                        projected.add(new FunctionalDependency(
                                index.toSet(candidate), index.toSet(attribute)));
                    }
                }
                combination = nextCombination(combination);
            }
        }

        return minimalCoverComputer.computeMinimalCover(projected);
    }

    private static boolean hasSmallerDeterminant(
            long candidate, long attribute, List<long[]> minimalDeterminants) {
        for (long[] entry : minimalDeterminants) {
            if (entry[1] == attribute && FdBitIndex.isSubset(entry[0], candidate)) {
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------
    // Bitmask helpers
    // ---------------------------------------------------------------

    private static List<Integer> bitsOf(long mask) {
        List<Integer> bits = new ArrayList<>();
        for (int bit = 0; bit < Long.SIZE - 1; bit++) {
            if ((mask & (1L << bit)) != 0) {
                bits.add(bit);
            }
        }
        return bits;
    }

    private static long expand(long combination, List<Integer> bits) {
        long mask = 0L;
        for (int i = 0; i < bits.size(); i++) {
            if ((combination & (1L << i)) != 0) {
                mask |= 1L << bits.get(i);
            }
        }
        return mask;
    }

    /** Gosper's hack: next integer with the same number of set bits. */
    private static long nextCombination(long combination) {
        long lowest = combination & -combination;
        long ripple = combination + lowest;
        return ripple | (((ripple ^ combination) >>> 2) / lowest);
    }

    // ---------------------------------------------------------------
    // Formatting helpers
    // ---------------------------------------------------------------

    private static List<FunctionalDependency> sortedFds(Set<FunctionalDependency> fds) {
        List<FunctionalDependency> sorted = new ArrayList<>(fds);
        sorted.sort(Comparator.comparing(FunctionalDependency::toString));
        return sorted;
    }

    private String formatRelation(Relation r) {
        return r.getName() + "("
                + String.join(",", new TreeSet<>(r.getAttributes()))
                + ")";
    }

    private String formatSet(Set<String> values) {
        return "{" + String.join(",", new TreeSet<>(values)) + "}";
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
