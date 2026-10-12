package com.dbms.analyzer.algorithm;

/**
 * Size limits for the analyses. Candidate-key search, sub-relation BCNF
 * checks and FD projection all enumerate attribute subsets, so their cost
 * grows exponentially with the number of attributes.
 */
public final class AnalysisLimits {

    /** Maximum number of attributes a relation may have. */
    public static final int MAX_ATTRIBUTES = 20;

    private AnalysisLimits() {
    }

    /**
     * @throws IllegalArgumentException if {@code attributeCount} exceeds
     *         {@link #MAX_ATTRIBUTES}
     */
    public static void requireWithinLimit(int attributeCount) {
        if (attributeCount > MAX_ATTRIBUTES) {
            throw new IllegalArgumentException(
                "Relations are limited to " + MAX_ATTRIBUTES
                    + " attributes (got " + attributeCount + ")");
        }
    }
}
