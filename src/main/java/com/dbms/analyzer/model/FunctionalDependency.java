package com.dbms.analyzer.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class FunctionalDependency {
    private final Set<String> leftSide;
    private final Set<String> rightSide;

    public FunctionalDependency(Set<String> leftSide, Set<String> rightSide) {
        this.leftSide = Collections.unmodifiableSet(new HashSet<>(leftSide));
        this.rightSide = Collections.unmodifiableSet(new HashSet<>(rightSide));
    }

    /** Read-only view; copy it before modifying. */
    public Set<String> getLeftSide() {
        return leftSide;
    }

    /** Read-only view; copy it before modifying. */
    public Set<String> getRightSide() {
        return rightSide;
    }

    /**
     * Returns a deterministic string with sorted attribute names.
     */
    @Override
    public String toString() {
        return String.join(",", new TreeSet<>(leftSide))
                + " -> "
                + String.join(",", new TreeSet<>(rightSide));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionalDependency that = (FunctionalDependency) o;
        return leftSide.equals(that.leftSide) && rightSide.equals(that.rightSide);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftSide, rightSide);
    }
}
