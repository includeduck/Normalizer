package com.dbms.analyzer.model;

import java.util.HashSet;
import java.util.Set;

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

    @Override
    public String toString() {
        return name + "(" + String.join(",", attributes) + ")";
    }
}
