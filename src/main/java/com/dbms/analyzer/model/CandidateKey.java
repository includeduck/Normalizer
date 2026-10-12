package com.dbms.analyzer.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class CandidateKey {
    private final Set<String> attributes;

    public CandidateKey(Set<String> attributes) {
        this.attributes = Collections.unmodifiableSet(new HashSet<>(attributes));
    }

    /** Read-only view; copy it before modifying. */
    public Set<String> getAttributes() {
        return attributes;
    }

    /**
     * Returns a deterministic string with sorted attribute names.
     */
    @Override
    public String toString() {
        return "{" + String.join(",", new TreeSet<>(attributes)) + "}";
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
