package com.dbms.analyzer.service;

import org.springframework.stereotype.Service;
import com.dbms.analyzer.model.Relation;
import com.dbms.analyzer.algorithm.FdUtil;
import java.util.HashSet;
import java.util.Set;

@Service
public class RelationService {

    private Relation currentRelation;

    /**
     * Creates a new relation from a schema string like "R(A,B,C)"
     */
    public Relation createRelation(String schemaString) throws IllegalArgumentException {
        schemaString = schemaString.trim();
        
        // Parse relation name and attributes
        int paren = schemaString.indexOf('(');
        if (paren == -1 || !schemaString.endsWith(")")) {
            throw new IllegalArgumentException(
                "Invalid schema format. Use 'R(A,B,C)'");
        }

        String relationName = schemaString.substring(0, paren).trim();
        String attributesStr = schemaString.substring(
            paren + 1, schemaString.length() - 1);

        Set<String> attributes = new HashSet<>();
        for (String attr : attributesStr.split(",")) {
            String trimmed = attr.trim();
            if (trimmed.isEmpty()) {
                throw new IllegalArgumentException("Empty attribute name");
            }
            if (!attributes.add(trimmed)) {
                throw new IllegalArgumentException(
                    "Duplicate attribute: " + trimmed);
            }
        }

        this.currentRelation = new Relation(relationName, attributes);
        return currentRelation;
    }

    /**
     * Gets the current relation
     */
    public Relation getCurrentRelation() {
        return currentRelation;
    }

    /**
     * Sets the current relation
     */
    public void setCurrentRelation(Relation relation) {
        this.currentRelation = relation;
    }

    /**
     * Validates if a relation exists
     */
    public boolean hasRelation() {
        return currentRelation != null;
    }

    /**
     * Gets all attributes of the current relation
     */
    public Set<String> getAttributes() {
        if (currentRelation == null) {
            throw new IllegalStateException("No relation defined");
        }
        return currentRelation.getAttributes();
    }

    /**
     * Clears the current relation
     */
    public void clear() {
        currentRelation = null;
    }
}
