package com.dbms.analyzer.service;

import org.springframework.stereotype.Service;
import com.dbms.analyzer.algorithm.MinimalCoverComputer;

import java.util.List;

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
