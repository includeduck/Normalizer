package com.dbms.analyzer.model;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class Relation {
    private String name;
    private Set<String> attributes;
    private Set<FunctionalDependency> functionalDependencies;

    public Relation(String name, Set<String> attributes) {
        this.name = name;
        this.attributes = new HashSet<>(attributes);
        this.functionalDependencies = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getAttributes() {
        return new HashSet<>(attributes);
    }

    public void setAttributes(Set<String> attributes) {
        this.attributes = new HashSet<>(attributes);
    }

    public Set<FunctionalDependency> getFunctionalDependencies() {
        return new HashSet<>(functionalDependencies);
    }

    public void addFunctionalDependency(FunctionalDependency fd) {
        functionalDependencies.add(fd);
    }

    public void removeFunctionalDependency(FunctionalDependency fd) {
        functionalDependencies.remove(fd);
    }

    public void setFunctionalDependencies(Set<FunctionalDependency> fds) {
        functionalDependencies = new HashSet<>(fds);
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
        return name.equals(relation.name) && attributes.equals(relation.attributes);
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31 + attributes.hashCode();
    }
}
