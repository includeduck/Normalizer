package com.dbms.analyzer.service;

import org.springframework.stereotype.Service;
import com.dbms.analyzer.model.FunctionalDependency;
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

        FunctionalDependency fd = FdUtil.parseFD(
            fdString, relationService.getAttributes());

        List<String> unknown =
            FdUtil.unknownAttributes(fd, relationService.getAttributes());
        if (!unknown.isEmpty()) {
            throw new IllegalArgumentException(FdUtil.unknownAttributesMessage(
                unknown, relationService.getAttributes()));
        }

        relationService.replaceCurrentRelation(
            relationService.getCurrentRelation().withFunctionalDependency(fd));
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
