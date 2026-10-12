package com.dbms.analyzer.service;

import org.springframework.stereotype.Service;
import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.model.Relation;
import com.dbms.analyzer.algorithm.FdUtil;
import java.util.List;
import java.util.Set;

@Service
public class FdService {

    private final RelationService relationService;

    public FdService(RelationService relationService) {
        this.relationService = relationService;
    }

    /**
     * Adds a functional dependency
     */
    public void addFunctionalDependency(String fdString) throws IllegalArgumentException {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }

        FunctionalDependency fd =
            parseDependency(fdString, relationService.getAttributes());
        relationService.replaceCurrentRelation(
            relationService.getCurrentRelation().withFunctionalDependency(fd));
    }

    /**
     * Creates a relation with its FDs and makes it current only if the
     * schema and every FD are valid; otherwise the current relation is left
     * unchanged.
     */
    public Relation loadRelation(String schemaString, List<String> fdStrings) {
        Relation relation = relationService.parseRelation(schemaString);
        for (String fdString : fdStrings) {
            relation = relation.withFunctionalDependency(
                parseDependency(fdString, relation.getAttributes()));
        }
        relationService.installRelation(relation);
        return relation;
    }

    private static FunctionalDependency parseDependency(
            String fdString, Set<String> attributes) {
        FunctionalDependency fd = FdUtil.parseFD(fdString, attributes);
        List<String> unknown = FdUtil.unknownAttributes(fd, attributes);
        if (!unknown.isEmpty()) {
            throw new IllegalArgumentException(
                FdUtil.unknownAttributesMessage(unknown, attributes));
        }
        return fd;
    }

    /**
     * Removes a functional dependency
     */
    public void removeFunctionalDependency(FunctionalDependency fd) {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }
        relationService.replaceCurrentRelation(
            relationService.getCurrentRelation().withoutFunctionalDependency(fd));
    }

    /**
     * Gets all functional dependencies
     */
    public Set<FunctionalDependency> getAllDependencies() {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }
        return relationService.getCurrentRelation().getFunctionalDependencies();
    }
}
