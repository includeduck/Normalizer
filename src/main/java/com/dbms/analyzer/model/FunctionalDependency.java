package com.dbms.analyzer.model;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class FunctionalDependency {
    private Set<String> leftSide;
    private Set<String> rightSide;

    public FunctionalDependency(Set<String> leftSide, Set<String> rightSide) {
        this.leftSide = new HashSet<>(leftSide);
        this.rightSide = new HashSet<>(rightSide);
    }

    public Set<String> getLeftSide() {
        return new HashSet<>(leftSide);
    }

    public void setLeftSide(Set<String> leftSide) {
        this.leftSide = new HashSet<>(leftSide);
    }

    public Set<String> getRightSide() {
        return new HashSet<>(rightSide);
    }

    public void setRightSide(Set<String> rightSide) {
        this.rightSide = new HashSet<>(rightSide);
    }

    /**
     * Returns a deterministic string with sorted attribute names.
     */
    @Override
    public String toString() {
        return String.join("", new TreeSet<>(leftSide))
                + " -> "
                + String.join("", new TreeSet<>(rightSide));
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
        return leftSide.hashCode() + rightSide.hashCode();
    }
}
