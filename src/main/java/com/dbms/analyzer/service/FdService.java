package com.dbms.analyzer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dbms.analyzer.model.FunctionalDependency;
import com.dbms.analyzer.algorithm.FdUtil;
import java.util.HashSet;
import java.util.Set;

@Service
public class FdService {

    @Autowired
    private RelationService relationService;

    /**
     * Adds a functional dependency
     */
    public void addFunctionalDependency(String fdString) throws IllegalArgumentException {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }

        FunctionalDependency fd = FdUtil.parseFD(fdString);

        if (!FdUtil.validateFdAttributes(
            fd, relationService.getAttributes())) {
            throw new IllegalArgumentException(
                "FD references undefined attributes");
        }

        relationService.getCurrentRelation().addFunctionalDependency(fd);
    }

    /**
     * Removes a functional dependency
     */
    public void removeFunctionalDependency(FunctionalDependency fd) {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }
        relationService.getCurrentRelation().removeFunctionalDependency(fd);
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

    /**
     * Clears all functional dependencies
     */
    public void clearDependencies() {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }
        relationService.getCurrentRelation().setFunctionalDependencies(
            new HashSet<>());
    }

    /**
     * Validates a FD string without adding it
     */
    public boolean validateFdString(String fdString) {
        try {
            FunctionalDependency fd = FdUtil.parseFD(fdString);
            return FdUtil.validateFdAttributes(
                fd, relationService.getAttributes());
        } catch (Exception e) {
            return false;
        }
    }
}
