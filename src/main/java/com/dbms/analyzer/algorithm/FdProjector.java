package com.dbms.analyzer.algorithm;

import com.dbms.analyzer.model.FunctionalDependency;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Projects F⁺ onto a sub-schema: the FDs X → A with X, A inside the schema
 * that follow from F, possibly through attributes outside the schema.
 * Returns a minimal cover of that projection.
 */
public class FdProjector {

    private final MinimalCoverComputer minimalCoverComputer;

    public FdProjector() {
        this(new MinimalCoverComputer());
    }

    public FdProjector(MinimalCoverComputer minimalCoverComputer) {
        this.minimalCoverComputer = minimalCoverComputer;
    }

    /**
     * @param schema the sub-schema to project onto
     * @param fds    the original FDs (may mention attributes outside the schema)
     * @return a minimal cover of the projection of F⁺ onto {@code schema}
     */
    public Set<FunctionalDependency> project(
            Set<String> schema, Set<FunctionalDependency> fds) {
        AnalysisLimits.requireWithinLimit(schema.size());
        FdBitIndex index = new FdBitIndex(schema, fds);
        return project(index.mask(schema), index);
    }

    /**
     * Subsets are visited smallest first; X → A is kept only if no smaller
     * determinant of A has been found inside X, so only minimal determinants
     * are passed on to the minimal-cover step.
     */
    Set<FunctionalDependency> project(long schema, FdBitIndex index) {
        List<Integer> bits = FdBitIndex.bitsOf(schema);
        int n = bits.size();
        List<long[]> minimalDeterminants = new ArrayList<>(); // {determinant, attribute}
        Set<FunctionalDependency> projected = new LinkedHashSet<>();

        for (int size = 1; size < n; size++) {
            long combination = (1L << size) - 1;
            while (combination < (1L << n)) {
                long candidate = FdBitIndex.expand(combination, bits);
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
                combination = FdBitIndex.nextCombination(combination);
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
}
