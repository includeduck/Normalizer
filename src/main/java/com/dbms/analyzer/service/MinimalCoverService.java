package com.dbms.analyzer.service;

import org.springframework.stereotype.Service;
import com.dbms.analyzer.algorithm.MinimalCoverComputer;
import com.dbms.analyzer.model.FunctionalDependency;

import java.util.List;
import java.util.Set;

@Service
public class MinimalCoverService {

    private final RelationService relationService;
    private final FdService fdService;
    private final MinimalCoverComputer minimalCoverComputer;

    public MinimalCoverService(
            RelationService relationService,
            FdService fdService,
            MinimalCoverComputer minimalCoverComputer) {
        this.relationService = relationService;
        this.fdService = fdService;
        this.minimalCoverComputer = minimalCoverComputer;
    }

    /**
     * Computes the minimal cover of the current relation's FDs.
     */
    public Set<FunctionalDependency> computeMinimalCover() {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }
        return minimalCoverComputer.computeMinimalCover(
                fdService.getAllDependencies());
    }

    /**
     * Computes the minimal cover with step-by-step explanation.
     */
    public List<String> computeMinimalCoverWithSteps() {
        if (!relationService.hasRelation()) {
            throw new IllegalStateException("No relation defined");
        }
        return minimalCoverComputer.computeMinimalCoverWithSteps(
                fdService.getAllDependencies());
    }
}
