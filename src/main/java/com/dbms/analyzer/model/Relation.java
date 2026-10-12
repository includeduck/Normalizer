package com.dbms.analyzer.model;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * An immutable relation schema with its functional dependencies.
 * "Changing" the FDs returns a new relation.
 */
public class Relation {
    private final String name;
    private final Set<String> attributes;
    private final Set<FunctionalDependency> functionalDependencies;

    public Relation(String name, Set<String> attributes) {
        this(name, attributes, Set.of());
    }

    public Relation(String name, Set<String> attributes,
                    Set<FunctionalDependency> functionalDependencies) {
        this.name = name;
        this.attributes = Set.copyOf(attributes);
        this.functionalDependencies = Set.copyOf(functionalDependencies);
    }

    public String getName() {
        return name;
    }

    public Set<String> getAttributes() {
        return new HashSet<>(attributes);
    }

    public Set<FunctionalDependency> getFunctionalDependencies() {
        return new HashSet<>(functionalDependencies);
    }

    /** Returns a copy of this relation with {@code fd} added. */
    public Relation withFunctionalDependency(FunctionalDependency fd) {
        Set<FunctionalDependency> fds = new HashSet<>(functionalDependencies);
        fds.add(fd);
        return new Relation(name, attributes, fds);
    }

    /** Returns a copy of this relation with {@code fd} removed. */
    public Relation withoutFunctionalDependency(FunctionalDependency fd) {
        Set<FunctionalDependency> fds = new HashSet<>(functionalDependencies);
        fds.remove(fd);
        return new Relation(name, attributes, fds);
    }

    /**
     * Returns a deterministic string representation with sorted attributes.
     */
    @Override
    public String toString() {
        return name + "(" + String.join(",", new TreeSet<>(attributes)) + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relation relation = (Relation) o;
        return name.equals(relation.name)
            && attributes.equals(relation.attributes)
            && functionalDependencies.equals(relation.functionalDependencies);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(name, attributes, functionalDependencies);
    }
}
