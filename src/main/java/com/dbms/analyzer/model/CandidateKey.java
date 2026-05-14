package com.dbms.analyzer.model;

import java.util.HashSet;
import java.util.Set;

public class CandidateKey {
    private Set<String> attributes;

    public CandidateKey(Set<String> attributes) {
        this.attributes = new HashSet<>(attributes);
    }

    public Set<String> getAttributes() {
        return new HashSet<>(attributes);
    }

    public void setAttributes(Set<String> attributes) {
        this.attributes = new HashSet<>(attributes);
    }

    @Override
    public String toString() {
        return "{" + String.join(",", attributes) + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CandidateKey that = (CandidateKey) o;
        return attributes.equals(that.attributes);
    }

    @Override
    public int hashCode() {
        return attributes.hashCode();
    }
}
